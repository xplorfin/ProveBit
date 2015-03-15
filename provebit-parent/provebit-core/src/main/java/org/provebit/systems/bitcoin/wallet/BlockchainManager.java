package org.provebit.systems.bitcoin.wallet;

import java.io.File;
import java.util.Date;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.provebit.systems.bitcoin.BitcoinDirectory;

public enum BlockchainManager {
	INSTANCE;
	
	private BlockStore store;
	private BlockChain chain;
	private PeerGroup peers;
	private NetworkParameters params = MainNetParams.get();
	
	private BlockchainManager() {
		File btcroot = BitcoinDirectory.INSTANCE.getRoot();
		File chainFile = new File(btcroot, "bitcoin.headerchain");

		try {
			store = new CompleteHeaderStore(params, chainFile);
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
	
	public BlockChain getChain() {
		return chain;
	}

}
