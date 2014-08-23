package org.provebit.proof;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

import org.h2.util.IOUtils;
import org.provebit.proof.ProofParser.Argument;
import org.provebit.proof.keysys.RootKey;

import com.google.bitcoin.core.Utils;
import com.google.common.primitives.Bytes;

public class ProofExecutor {
	
	private static final int WORKING_LOCATION = -1;
	private static final int FRAME_WIDTH = 64;
	private static final int MEM_SIZE = 1024;
	private static final int MEM_CELL_SIZE = 1024;
	private static final int STACK_FRAMES = 128;
	public static final String MAIN_NAME = "__main";
		
	/** Disallow operations that require buffering for an entire stream */
	public boolean restrictArbitraryStreamBuffer = true;
	
	/** Byte order **/
	private boolean littleEndian = true;
	
	private final MemoryAccessor intMIO = new MemoryAccessor(true);
	private final MemoryAccessor basicMIO = new MemoryAccessor(false);
	
	public class MemoryAccessor {
		private boolean ue;
		private MemoryAccessor(boolean useEndianness) {
			ue = useEndianness;
		}
		public byte[] read(int location) {
			return itransform(mread(location));
		}
		
		public void write(int location, byte[] bytes) {
			mwrite(location, otransform(bytes));
		}
		public byte[] itransform(byte[] bytes) {
			return (ue && littleEndian) ? Utils.reverseBytes(bytes) : bytes; 
		}
		public byte[] otransform(byte[] bytes) {
			return itransform(bytes);
		}
	}
	
	public class ProgramDieException extends RuntimeException {
		private static final long serialVersionUID = -3144242523436248981L;
		public ProgramDieException(String msg) {
			super(msg);
		}
	}
	
	public class NoWorkingBufferException extends RuntimeException {
		private static final long serialVersionUID = -2036844703401966639L;
		public NoWorkingBufferException(String msg) {
			super(msg);
		}
	}
	
	public class StreamIOException extends RuntimeException {
		private static final long serialVersionUID = 8221525570347232640L;
		public StreamIOException(String msg) {
			super(msg);
		}
		public StreamIOException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
	
	public class MemoryCellException extends RuntimeException {
		private static final long serialVersionUID = 1177412913758019967L;
		public MemoryCellException(String msg) {
			super(msg);
		}
	}
	
	public class UnsupportedOperationException extends RuntimeException {
		private static final long serialVersionUID = -9059482865721580619L;
		public UnsupportedOperationException(String msg) {
			super(msg);
		}
	}
	
	private enum Wstate {
		NORM, OVERLOAD_STREAM;
	}
	/** Exec state is overloaded or replaced by file */
	Wstate working;
	
	InputStream workingStream;
	
	public class PFrame {
		public String fname;
		public byte[][] mem;
		public int execidx;
		
		public String getCurrentName() {
			return proof.opNameGet(fname, execidx);
		}
		
		public int getCurrentArgCount() {
			return proof.opArgCount(fname, execidx);
		}
		
		public void enforceArgcBounds(int min, int max) {
			int argc = getCurrentArgCount();
			if (argc < min || argc > max)
				throw new RuntimeException("too many arguments"); //TODO spec exception
		}
		
		public byte[] getCurrentArg(int i) {
			return get(fname, execidx, i, basicMIO);
		}
		
		public BigInteger getCurrentArgInt(int i) {
			return getInt(fname, execidx, i);
		}
		
		public String getCurrentArgStr(int i) {
			return getString(fname, execidx, i);
		}
		
		public void advance() {
			++execidx;
		}
		
		public boolean isDone() {
			return (execidx >= proof.funcLen(fname));
		}
	}
	
	Stack<PFrame> functionFrames = new Stack<PFrame>();
	
	byte[][] memory = new byte[MEM_SIZE][];
	byte[] workingData;
	
	ProofParser proof;
	boolean executed = false;
	
	public ProofExecutor(InputStream workingInitStream) {
		enteringFrameSetup();
		setWorking(workingInitStream);
	}
	
	public ProofExecutor(byte[] workingInitData) {
		this(new ByteArrayInputStream(workingInitData));
	}
	
	public ProofExecutor(String workingInitDataString) {
		this(workingInitDataString.getBytes());
	}
	
	public byte[] execute(ProofParser prg) {
		if (executed) return mread(WORKING_LOCATION);
		executed = true;
		proof = prg;
		
		while (executionIsUnfinished()) {
			PFrame frame = functionFrames.peek();
			if (frame.isDone()) {
				functionFrames.pop();
				continue;
			}
			String opname = frame.getCurrentName();
			int argc = frame.getCurrentArgCount();
			
			
			// function calling
			if (opname.startsWith("f_")) {
				if (functionFrames.size() > (STACK_FRAMES - 1))
					throw new RuntimeException("proof executor stack frame past max");
				String fname = opname.substring(2);

				//byte[][] nmem = functionFrames.peek().mem;
				byte[][] stuff = new byte[argc][];
				for (int i = 0; i < argc; i++) {
					byte[] cur = frame.getCurrentArg(i+1);
					stuff[i] = Arrays.copyOf(cur, cur.length);
				}
				frame.advance();
				addFrame(fname, stuff);
				continue;
			}
			
			byte[] wbytes, out;
			// TODO Handle each op
			switch (opname) {
				case "op_func":
					if (functionFrames.size() > 1)
						throw new RuntimeException("can't declare func inside func");
					break;
					
				case "op_sha256":
					frame.enforceArgcBounds(0, 3);
					Digester d = new Digester("SHA-256");
					if (argc == 0) {
						try {
							wbytes = getWorking();
							d.addBytes(wbytes);
						} catch (NoWorkingBufferException ne) {
							try {
								d.addStream(workingStream);
							} catch (IOException e) {
								throw new StreamIOException(e.getMessage(), e.getCause());
							}
						}
					}
					for (int i = 0; i < argc; i++) {
						helperHash(frame, d, i+1);
					}
					out = d.digest();
					intMIO.write(WORKING_LOCATION, out);
					break;
					
				case "op_cat":
					frame.enforceArgcBounds(1, 2);
					byte[] l, r;
					if (argc == 1) {
						l = mread(WORKING_LOCATION);
						r = frame.getCurrentArg(1);
					}
					else {
						l = frame.getCurrentArg(1);
						r = frame.getCurrentArg(2);
					}
					byte[] res = new byte[l.length + r.length];
					System.arraycopy(l, 0, res, 0, l.length);
					System.arraycopy(r, 0, res, l.length, r.length);
					
					mwrite(WORKING_LOCATION, res);
					break;
					
				case "op_compose":
				case "op_compose_ext":
					frame.enforceArgcBounds(1, 1);
					byte[] subarr = mread(WORKING_LOCATION);
					byte[] mainarr;
					if (opname.equals("op_compose"))
							mainarr = frame.getCurrentArg(1);
					else // op_compose_ext
							mainarr = RootKey.keyLookup(frame.getCurrentArgStr(1));
					int sub = Bytes.indexOf(mainarr, subarr);
					if (sub == -1)
						throw new ProgramDieException("Byte subarray fail in op_compose");
					mwrite(WORKING_LOCATION, mainarr);
					break;
					
				case "op_store":
					frame.enforceArgcBounds(1, 1);
					int wloc = helperMemFromBigInt(frame.getCurrentArgInt(1));
					mwrite(wloc, mread(WORKING_LOCATION));
					break;
					
				case "op_load":
					frame.enforceArgcBounds(1, 1);
					int rloc = helperMemFromBigInt(frame.getCurrentArgInt(1));
					mwrite(WORKING_LOCATION, mread(rloc));
					break;
					
				case "op_set":
					frame.enforceArgcBounds(1, 1);
					mwrite(WORKING_LOCATION, frame.getCurrentArg(1));
					break;
					
				case "op_swap":
					frame.enforceArgcBounds(1, 1);
					int mloc = helperMemFromBigInt(frame.getCurrentArgInt(1));
					byte[] mdata = mread(mloc);
					mwrite(mloc, mread(WORKING_LOCATION));
					mwrite(WORKING_LOCATION, mdata);
					break;
					
				case "op_rev":
					frame.enforceArgcBounds(0, 0);
					wbytes = mread(WORKING_LOCATION);
					res = Utils.reverseBytes(wbytes);
					mwrite(WORKING_LOCATION, res);
					break;
					
				case "op_size":
					frame.enforceArgcBounds(0, 1);
					if (argc == 0)
						wbytes = mread(WORKING_LOCATION);
					else
						wbytes = mread(frame.getCurrentArgInt(1).intValue());
					writeInt(WORKING_LOCATION, BigInteger.valueOf(wbytes.length));
					break;
					
				case "op_intbe":
					frame.enforceArgcBounds(0, 0);
					littleEndian = false;
					break;
					
				case "op_intle":
					frame.enforceArgcBounds(0, 0);
					littleEndian = true;
					break;
					
				default:
					throw new UnsupportedOperationException(
							"op not implemented: " + opname);
			}
			frame.advance();
		}
		
		return mread(WORKING_LOCATION);
	}
	
	private void helperHash(PFrame p, Digester d, int argn) {
		try {
			byte[] bytes =  p.getCurrentArg(argn);
			d.addBytes(bytes);
		} catch (NoWorkingBufferException ne) {
			try {
				d.addStream(workingStream);
			} catch (IOException e) {
				throw new StreamIOException(e.getMessage(), e.getCause());
			}
		}
	}
	
	private int helperMemFromBigInt(BigInteger i) {
		if (i.compareTo(BigInteger.valueOf(MEM_SIZE)) > 0 ||
				i.compareTo(BigInteger.valueOf(WORKING_LOCATION)) < 0 )
				throw new MemoryCellException("location: " + i + " out of bounds");
		return i.intValue();
	}
	
	private boolean executionIsUnfinished() {
		return (functionFrames.size() > 0);
	}
	
	private byte[] getWorking() {
		if (working == Wstate.NORM)
			return workingData;
		else if (working == Wstate.OVERLOAD_STREAM) {
			if (restrictArbitraryStreamBuffer)
				throw new NoWorkingBufferException("for working value read");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				IOUtils.copy(workingStream, bos);
				return bos.toByteArray();
			} catch (IOException e) {
				throw new StreamIOException(e.getMessage(), e.getCause());
			}
		}
		throw new RuntimeException("Unhandled Working variable state");
	}
	
	private void setWorking(byte[] data) {
		workingData = data;
		working = Wstate.NORM;
	}
	
	private void setWorking(InputStream is) {
		workingStream = is;
		working = Wstate.OVERLOAD_STREAM;
	}
	
	private byte[] get(String fname, int op, int arg, MemoryAccessor mr) {
		Argument a = proof.opArgGet(fname, op, arg);
		switch (a.type) {
		case LITERAL:
			return a.bin;
		case MEMORY:
			return mr.read(a.mem);
		case UNSUPPORTED:
		default:
			throw new RuntimeException("unsupported type");
		}
	}
	
	private byte[] mread(int location) {
		if (location < WORKING_LOCATION || location > MEM_SIZE)
			throw new MemoryCellException("location: " + location + " out of bounds");
		if (location == WORKING_LOCATION)
			return getWorking();
		if (location < FRAME_WIDTH)
			return functionFrames.peek().mem[location];
		else
			return memory[location];
	}
	
	private void mwrite(int location, byte[] data) {
		if (location < WORKING_LOCATION || location > MEM_SIZE)
			throw new MemoryCellException("location: " + location + " out of bounds");
		if (data.length > MEM_CELL_SIZE)
			throw new MemoryCellException("data size: " + data.length
					+ " can't fit in size: " + MEM_CELL_SIZE);
		if (location == WORKING_LOCATION)
			setWorking(data);
		else if (location < FRAME_WIDTH)
			functionFrames.peek().mem[location] = data;
		else
			memory[location] = data;
	}
	
	private String getString(String fname, int op, int arg) {
		try {
			byte[] bytes = get(fname, op, arg, basicMIO);
			if (bytes == null) bytes = new byte[0]; // default memory
			return new String(bytes, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void writeString(int location, String str) {
		try {
			basicMIO.write(location, str.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private BigInteger getInt(String fname, int op, int arg) {
		byte[] bytes = get(fname, op, arg, intMIO);
		if (bytes == null) bytes = new byte[] {0};
		return new BigInteger(bytes);
	}
	

	private void writeInt(int location, BigInteger i) {
		byte[] intdata = i.toByteArray();
		intMIO.write(location, intdata);
	}
	
	private void enteringFrameSetup() {
		if (functionFrames.size() > 0)
			throw new RuntimeException("attempting frame setup not during init");
		addFrame(MAIN_NAME, null);
	}
	
	private void addFrame(String name, byte[][] initData) {
		PFrame frame = new PFrame();
		frame.mem = new byte[FRAME_WIDTH][];
		if (initData != null)
			for (int i = 0; i < initData.length; i++)
				frame.mem[i+1] = initData[i];
		frame.fname = name;
		frame.execidx = 0;
		functionFrames.push(frame);
		writeString(0, name);
	}
}
