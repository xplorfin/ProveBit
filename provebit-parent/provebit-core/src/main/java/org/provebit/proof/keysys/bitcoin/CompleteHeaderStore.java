package org.provebit.proof.keysys.bitcoin;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.bitcoinj.core.*;
import org.bitcoinj.store.*;
import org.bitcoinj.utils.Threading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompleteHeaderStore implements BlockStore {
	private static final Logger log = LoggerFactory.getLogger(CompleteHeaderStore.class);

	protected NetworkParameters params;
	
	protected ReentrantLock lock = Threading.lock("CompleteHeaderStore");
	
	
	
	protected Map<Sha256Hash, StoredBlock> cache = new LinkedHashMap<Sha256Hash, StoredBlock>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Sha256Hash, StoredBlock> entry) {
			return size() > 4000;
		}
	};
	
	private static final Object MISS = new Object();
	protected Map<Sha256Hash, StoredBlock> missCache = new LinkedHashMap<Sha256Hash, StoredBlock>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Sha256Hash, StoredBlock> entry) {
			return size() > 200;
		}
	};
	
	
	public static final String MAGIC = "CHST";
	
	protected static final long FILE_HEADER_BYTES = 1024;
	protected static final long INIT_SIZE = FILE_HEADER_BYTES;
	protected static final long ENTRY_SIZE = 128;
	
	protected FileLock fLock;
	protected volatile RandomAccessFile randomAccess;
	//protected volatile MappedByteBuffer buf;
	
	protected StoredBlock lastChainHead = null;
	
	public CompleteHeaderStore(NetworkParameters params, File file) throws BlockStoreException {
		boolean fileexisted = file.exists();
		try {
			randomAccess = new RandomAccessFile(file, "rw");
			long csize;
			if (!fileexisted) {
				log.info("Creating new CompleteHeaderStore file " + file);
				randomAccess.setLength(INIT_SIZE);
				csize = INIT_SIZE;
			} else {
				csize = randomAccess.length();
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
			throw new BlockStoreException(e);
		}
		
	}

	private void initFile() throws Exception {
		byte[] filemagic;
		filemagic = MAGIC.getBytes("US-ASCII");
		randomAccess.write(filemagic);
		
		Block genesis = params.getGenesisBlock().cloneAsHeader();
        StoredBlock storedGenesis = new StoredBlock(genesis, genesis.getWork(), 0);
		put(storedGenesis);
		setChainHead(storedGenesis);
	}
	
	private int getBlockCount() throws BlockStoreException {
		try {
			return (int) ((randomAccess.length() - FILE_HEADER_BYTES) / ENTRY_SIZE);
		} catch (IOException e) {
			throw new BlockStoreException(e);
		}
	}

	@Override
	public void close() throws BlockStoreException {
		// TODO Auto-generated method stub
		try {
			randomAccess.close();
		} catch(IOException e) {
			throw new BlockStoreException(e);
		}
	}

	@Override
	public StoredBlock get(Sha256Hash hash) throws BlockStoreException {
		// TODO
		
		return null;
	}

	@Override
	public StoredBlock getChainHead() throws BlockStoreException {
		final RandomAccessFile f = randomAccess;
		if (f == null) 
			throw new BlockStoreException("Store closed");
		
        lock.lock();
        try {
            if (lastChainHead == null) {
                byte[] chainHeadHash = new byte[96];
                randomAccess.seek(4);
                randomAccess.read(chainHeadHash);
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
            long oldlen = randomAccess.length();
            randomAccess.setLength(oldlen + ENTRY_SIZE);
            
            randomAccess.seek(oldlen);
            
            Sha256Hash hash = block.getHeader().getHash();
            missCache.remove(hash);
            randomAccess.write(hash.getBytes());
            ByteBuffer bb = ByteBuffer.allocate(96); // size
            block.serializeCompact(bb);
            randomAccess.write(bb.array());
            cache.put(hash, block);
        } catch (IOException e) {
			throw new BlockStoreException(e);
		} finally {
			lock.unlock(); 
		}
		
	}

	@Override
	public void setChainHead(StoredBlock chainHead) throws BlockStoreException {
		// TODO Auto-generated method stub
		
	}

}
