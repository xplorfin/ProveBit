package org.provebit.proof;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.bitcoinj.core.Utils;

public class ProofParser {
	private Object[][] ops;
	public Map<String, Integer> funcmap = new HashMap<String, Integer>();
	
	public enum Type {
		MEMORY, LITERAL, UNSUPPORTED;
	}
	
	public class Argument {
		public Type type;
		public int mem;
		public byte[] bin;
	}
	
	public ProofParser(Object[] ops) {
		process(ops);
	}
	
	public ProofParser() {}
	
	private void process(Object[] obj) {
		ops = new Object[obj.length][];
		for (int i = 0; i < obj.length; i++) {
			opCheck(obj[i]);
			Object[] obji = (Object[]) obj[i];
			ops[i] = obji;
			String s = (String) obji[0];
			if (s.equals("op_func")) {
				if (obji.length < 3)
					throw new RuntimeException("op_func not in proper form");
				if (!(obji[1] instanceof String))
					throw new RuntimeException("func name not provided");
				if (!(obji[2] instanceof Object[]))
					throw new RuntimeException("func ops not in array");
				Object[] sub = (Object[]) obji[2];
				for (int j = 0; j < sub.length; j++)
					opCheck(sub[j]);
				String funcname = (String) obji[1];
				funcmap.put(funcname, i);
			}
		}
	}
	
	private void opCheck(Object opi) {
		if (!(opi instanceof Object[]) )
			throw new RuntimeException("invalid operation");
		Object[] op = (Object[]) opi;
		if (op.length == 0)
			throw new RuntimeException("op does not have any elements");
		if (!(op[0] instanceof String))
			throw new RuntimeException("op does not have name");
	}
	
	public Object[] get(int op) {
		return ops[op];
	}
	
	public int funcLen(String fname) {
		if (fname.equals(ProofExecutor.MAIN_NAME))
			return ops.length;
		Integer fi = funcmap.get(fname);
		if (fi == null) throw new RuntimeException("func does not exist");
		return ((Object[]) ops[fi][2]).length;
	}
	
	private Object[] opArrayGet(String fname, int op) {
		if (fname.equals(ProofExecutor.MAIN_NAME))
			return get(op);
		Integer fi = funcmap.get(fname);
		if (fi == null) throw new RuntimeException("func does not exist");
		return (Object[]) ((Object[]) ops[fi][2])[op];
	}
	
	public String opNameGet(String fname, int op) {
		return (String) opArrayGet(fname, op)[0]; // we already did sanity checking
	}
	
	public int opArgCount(String fname, int op) {
		return opArrayGet(fname, op).length - 1;
	}
	
	public Argument opArgGet(String fname, int op, int argn) {
		Object arg = opArrayGet(fname, op)[argn];
		Argument res = new Argument();
		if (arg instanceof Integer || arg instanceof Long) {
			res.bin = new BigInteger(arg.toString()).toByteArray();
			res.type = Type.LITERAL;
		}
		else if (arg instanceof String) {
			String s = (String) arg;
			if (s.startsWith("h:") || s.startsWith("H:")) {
				String hex = s.substring(2);
				if (hex.length() % 2 != 0)
					throw new RuntimeException("hex parse on uneven hex string");
				try {
					res.bin = new BigInteger(hex, 16).toByteArray();
					if (res.bin[0] == 0) { // unsigned as number
					    byte[] tmp = new byte[res.bin.length - 1];
					    System.arraycopy(res.bin, 1, tmp, 0, tmp.length);
					    res.bin = tmp;
					}
					if (s.startsWith("H:"))
						res.bin = Utils.reverseBytes(res.bin);
				} catch (NumberFormatException e) {
					throw new RuntimeException("hex data parse failure");
				}
				res.type = Type.LITERAL;
			}
			else if (s.startsWith("m:")) {
				String maddr = s.substring(2);
				try {
					res.mem = Integer.parseInt(maddr);
				} catch (NumberFormatException e) {
					throw new RuntimeException("memory address parse failure");
				}
				res.type = Type.MEMORY;
			}
			else if (s.startsWith("s:")) {
				String str = s.substring(2);
				try {
					res.bin = str.getBytes("US-ASCII");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				res.type = Type.LITERAL;
			} else {
				res.type = Type.UNSUPPORTED;
			}
		} else {
			res.type = Type.UNSUPPORTED;
		}
		return res;
	}
}
