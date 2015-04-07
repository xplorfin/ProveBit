package org.provebit.proof.keysys.bitcoin;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import org.provebit.proof.keysys.AbstractKeyNode;
import org.provebit.proof.keysys.KeyNotFoundException;
import org.provebit.systems.bitcoin.wallet.BlockchainManager;
import org.provebit.systems.bitcoin.wallet.CompleteHeaderStore;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Utils;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;

public class Blockchain extends AbstractKeyNode {
	private static Blockchain instance = getInstance();
	
	private Blockchain() { }
	private static Blockchain getInstance() {
		if (instance != null) return instance;
		else return new Blockchain();
	}
	
	public BlockStore store = BlockchainManager.INSTANCE.getChain().getBlockStore();

	public PeerGroup peers;	
	
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
			if (blk == null) {
				throw new BlockStoreException("block not found");
			}
			return examineBlock(vals, i+1, blk);
		} catch (NumberFormatException e) {
			throw new KeyNotFoundException("invalid block hash " + blockHashStr + " in " + Arrays.toString(vals));
		} catch (BlockStoreException e) {
			throw new KeyNotFoundException("block hash not found: " + blockHashStr + " in " + Arrays.toString(vals));
		}
	}

}
