package org.provebit.proof.keysys.bitcoin;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import org.provebit.Config;
import org.provebit.proof.keysys.AbstractKeyNode;
import org.provebit.proof.keysys.KeyNotFoundException;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.SPVBlockStore;

public class Blockchain extends AbstractKeyNode {
	private static Blockchain instance = getInstance();
	
	private Blockchain() { init(); }
	private static Blockchain getInstance() {
		if (instance != null) return instance;
		else return new Blockchain();
	}
	
	public boolean initialized = false;
	public SPVBlockStore store;
	public BlockChain chain;
	public PeerGroup peers;
	public NetworkParameters params = Config.getBitcoinNet();
	
	public void init() {
		File folder = Config.getDirectory();
		File chainFile = new File(folder, "bitcoin.spvchain");

		
		try {
			store = new SPVBlockStore(params, chainFile);
			chain = new BlockChain(params, store);
			peers = new PeerGroup(params, chain);
			// TODO set peers / user agent
			peers.addPeerDiscovery(new DnsDiscovery(params));
			peers.setMaxConnections(15);
			// 4 hours ago
			peers.setFastCatchupTimeSecs((new Date().getTime() / 1000) - (4 * 60 * 60));
			peers.startAsync();
			peers.awaitRunning();
			peers.downloadBlockChain();
		} catch (BlockStoreException e) {
			e.printStackTrace();
			throw new RuntimeException("failed to initialized block store");
		}
	}
	
	public void shutdown() {
		peers.stopAsync();
		peers.awaitTerminated();
		try {
			store.close();
		} catch (BlockStoreException e) {
			e.printStackTrace();
			throw new RuntimeException("failed to close block store");
		}
	}
	
	public static byte[] keyLookup(String[] vals, int i) {
		return instance.keyLookupRecurse(vals, i);
	}

	public static byte[] examineBlock(String[] vals, int i, StoredBlock block) {
		Block b = block.getHeader();

		switch (vals[i]) {
			case "header":
				return b.bitcoinSerialize();
		
			case "merkle_root":
				return Utils.reverseBytes(b.getMerkleRoot().getBytes());
				
			case "nonce":
				throw new KeyNotFoundException("unimplemented block subkey " + vals[i]);
				//break;
			
			case "timestamp":
				throw new KeyNotFoundException("unimplemented block subkey " + vals[i]);
				//break;
				
			case "tx":
				throw new KeyNotFoundException("unimplemented block subkey " + vals[i]);
				//break;
				
			default:
				throw new KeyNotFoundException("invalid block subkey" + vals[i] + " in" + Arrays.toString(vals));
		}
	}
	
	@Override
	protected byte[] keyLookupRecurse(String[] vals, int i) {
		String blockHashStr = vals[i];;
		try {
			if (blockHashStr.length() != 64)
				throw new KeyNotFoundException("invalid block hash " + blockHashStr + " in " + Arrays.toString(vals));
			new BigInteger(vals[i], 16); // test parsing

			Sha256Hash blockHash = new Sha256Hash(blockHashStr);
			StoredBlock blk = store.get(blockHash);
			return examineBlock(vals, i+1, blk);
		} catch (NumberFormatException e) {
			throw new KeyNotFoundException("invalid block hash " + blockHashStr + " in " + Arrays.toString(vals));
		} catch (BlockStoreException e) {
			throw new KeyNotFoundException("block hash not found: " + blockHashStr + " in " + Arrays.toString(vals));
		}
	}

}
