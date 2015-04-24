package org.provebit.systems.bitcoin.wallet;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.bitcoinj.core.*;
import org.bitcoinj.store.*;
import org.bitcoinj.utils.Threading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This BlockStore stores all headers in Bitcoin, all the way back to the Genesis block
 * It grows about 6.7MiB per year of Bitcoin's existence.
 * 
 * @author Stephen Halm
 */
public class CompleteHeaderStore implements BlockStore {
	private static final Logger log = LoggerFactory.getLogger(CompleteHeaderStore.class);
	protected NetworkParameters params;
	protected ReentrantLock lock = Threading.lock("CompleteHeaderStore");
	
	protected Map<Sha256Hash, StoredBlock> cache = new LinkedHashMap<Sha256Hash, StoredBlock>() {
		private static final long serialVersionUID = 6336050897666612846L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Sha256Hash, StoredBlock> entry) {
			return size() > 4000;
		}
	};
	
	private static final Object MISS = new Object();
	protected Map<Sha256Hash, Object> missCache = new LinkedHashMap<Sha256Hash, Object>() {
		private static final long serialVersionUID = 6138042289841899182L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Sha256Hash, Object> entry) {
			return size() > 200;
		}
	};
	
	
	public static final String MAGIC = "CHST";
	
	protected static final long FILE_HEADER_BYTES = 1024;
	protected static final long INIT_SIZE = FILE_HEADER_BYTES;
	protected static final long ENTRY_SIZE = 128;
	protected static final int HASH_SIZE = 32;
	protected static final int BLOCK_SIZE = 96;
	protected static final long COUNT_LOC = 4;
	protected static final long HEAD_LOC = 8;
	
	protected FileLock fLock;
	protected volatile RandomAccessFile randomAccess;
	//protected volatile MappedByteBuffer buf;
	
	protected StoredBlock lastChainHead = null;
	
	/**
	 * Use existing (or create) a file backed complete header store.
	 * @param params
	 * 		The network parameters (Bitcoin network or Testnet)
	 * @param file
	 * 		What file the sore goes in
	 * @throws BlockStoreException usually a kind of file related execption case
	 */
	public CompleteHeaderStore(NetworkParameters params, File file) throws BlockStoreException {
		this.params = params;
		boolean fileexisted = file.exists();
		try {
			randomAccess = new RandomAccessFile(file, "rw");
			if (!fileexisted) {
				log.info("Creating new CompleteHeaderStore file " + file);
				randomAccess.setLength(INIT_SIZE);
			}
			
			FileChannel ch = randomAccess.getChannel();
			fLock = ch.tryLock();
			if (fLock == null)
				throw new Exception("File is already locked");
				
			//buf = ch.map(FileChannel.MapMode.READ_WRITE, 0, csize);
			
			if (fileexisted) {
				byte[] filemagic = new byte[4];
                randomAccess.read(filemagic);
                if (!new String(filemagic, "US-ASCII").equals(MAGIC))
                    throw new Exception("File does not have magic code " + MAGIC);
			} else {
				initFile();
			}
			
		} catch (Exception e) {
			try {
				if (randomAccess != null)
					randomAccess.close();
			} catch (IOException e1) {
				throw new BlockStoreException(e1);
			}
			e.printStackTrace();
			throw new BlockStoreException(e);
		}
		
	}

	private void initFile() throws Exception {
		byte[] filemagic;
		filemagic = MAGIC.getBytes("US-ASCII");
		randomAccess.write(filemagic);

        setSize(0);
		Block genesis = params.getGenesisBlock().cloneAsHeader();
        StoredBlock storedGenesis = new StoredBlock(genesis, genesis.getWork(), 0);
		put(storedGenesis);
		setChainHead(storedGenesis);
	}

	/**
	 * Get the number of blocks (height of chain) currently stored
	 * @param newSize
	 * 		new number of blocks
	 * @throws IOException
	 */
	private void setSize(int newSize) throws IOException {
		randomAccess.seek(COUNT_LOC);
		randomAccess.writeInt(newSize);
	}
	
	/**
	 * Set the number of blocks (height of chain) currently stored
	 * @return
	 * 		number of blocks
	 * @throws IOException
	 */
	private int getSize() throws IOException {
		randomAccess.seek(COUNT_LOC);
		return randomAccess.readInt();
	}

	@Override
	public void close() throws BlockStoreException {
		try {
			randomAccess.close();
		} catch(IOException e) {
			throw new BlockStoreException(e);
		}
	}

	@Override
	public StoredBlock get(Sha256Hash hash) throws BlockStoreException {
		final RandomAccessFile f = randomAccess;
		if (f == null) 
			throw new BlockStoreException("Store closed");
		
        lock.lock();
        try {
        	StoredBlock cacheAttempt = cache.get(hash);
        	if (cacheAttempt != null)
        		return cacheAttempt;
        	if (missCache.get(hash) != null)
        		return null;
        		
        	final byte[] hashBytesToFind = hash.getBytes();
        	byte[] hashTest = new byte[HASH_SIZE];
        	long blocksEnd = FILE_HEADER_BYTES + (ENTRY_SIZE * getSize());
        	
        	f.seek(FILE_HEADER_BYTES);
        	while(f.getFilePointer() < blocksEnd) {
        		f.read(hashTest);
        		
        		if (Arrays.equals(hashBytesToFind, hashTest)) {
        			byte[] rawblock = new byte[BLOCK_SIZE];
        			f.read(rawblock);
        			StoredBlock block = StoredBlock.deserializeCompact(params, ByteBuffer.wrap(rawblock));
        			cache.put(hash, block);
        			return block;
        		}
        		
        		f.skipBytes(BLOCK_SIZE);
        	}
        	
        	missCache.put(hash, MISS);
            return null;
        } catch (IOException e) {
        	throw new BlockStoreException(e);
		} finally {
        	lock.unlock();
    	}
	}

	@Override
	public StoredBlock getChainHead() throws BlockStoreException {
		final RandomAccessFile f = randomAccess;
		if (f == null) 
			throw new BlockStoreException("Store closed");
		
        lock.lock();
        try {
            if (lastChainHead == null) {
                byte[] chainHeadHash = new byte[HASH_SIZE];
                f.seek(HEAD_LOC);
                f.read(chainHeadHash);
                Sha256Hash hash = new Sha256Hash(chainHeadHash);
                StoredBlock block = get(hash);
                if (block == null)
                    throw new BlockStoreException("Corrupted block store. Could not find block " + hash);
                lastChainHead = block;
            }
            return lastChainHead;
        } catch (IOException e) {
        	throw new BlockStoreException(e);
		} finally {
        	lock.unlock();
    	}
		
	}

	@Override
	public void put(StoredBlock block) throws BlockStoreException {
		final RandomAccessFile f = randomAccess;
		if (f == null) 
			throw new BlockStoreException("Store closed");
		
        lock.lock();
        try {
            long oldlen = f.length();
            
            long nextPtr = FILE_HEADER_BYTES + (ENTRY_SIZE * getSize());
            
            if (nextPtr >= oldlen) {
            	f.setLength(oldlen + 8 * 1024 * ENTRY_SIZE);
            }
            
            f.seek(nextPtr);
            
            Sha256Hash hash = block.getHeader().getHash();
            missCache.remove(hash);
            f.write(hash.getBytes());
            ByteBuffer bb = ByteBuffer.allocate(BLOCK_SIZE); // size
            block.serializeCompact(bb);
            f.write(bb.array());
            cache.put(hash, block);
            
            int sizeOld = getSize();
            setSize(sizeOld + 1);
        } catch (IOException e) {
			throw new BlockStoreException(e);
		} finally {
			lock.unlock(); 
		}
		
	}

	@Override
	public void setChainHead(StoredBlock chainHead) throws BlockStoreException {
		final RandomAccessFile f = randomAccess;
		if (f == null) 
			throw new BlockStoreException("Store closed");
		
        lock.lock();
        try {
            f.seek(HEAD_LOC);
            byte[] headerhash = chainHead.getHeader().getHash().getBytes();
            f.write(headerhash);
        } catch (IOException e) {
			throw new BlockStoreException(e);
		} finally {
			lock.unlock(); 
		}
		
	}

}
