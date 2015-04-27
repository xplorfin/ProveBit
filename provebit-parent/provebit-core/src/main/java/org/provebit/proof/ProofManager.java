package org.provebit.proof;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.provebit.systems.bitcoin.wallet.ApplicationWallet;
import org.provebit.systems.bitcoin.wallet.BlockchainManager;
import org.provebit.systems.bitcoin.wallet.CompleteHeaderStore;
import org.provebit.utils.ApplicationDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ProofManager implements WalletEventListener {
	INSTANCE;

	private final Logger log = LoggerFactory.getLogger(CompleteHeaderStore.class);
	
	private ProofManagerState state;
	private File loc;
	private final String FILENAME = "proofstate.bin";
	private ApplicationWallet appwallet = ApplicationWallet.INSTANCE;

	private ProofManager() {
		loc = new File(ApplicationDirectory.INSTANCE.getRoot(), FILENAME);
		if (loc.exists()) {
			try (InputStream file = new FileInputStream(loc);
					InputStream buffer = new BufferedInputStream(file);
					ObjectInput input = new ObjectInputStream(buffer)) {
				state = (ProofManagerState) input.readObject();
				state.initialize();
			} catch (Exception e) {
				log.error("Cound not recover proof progress file");
				e.printStackTrace();
			}
		} else {
			state = new ProofManagerState();
		}

		// auto save on shutdown !
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				saveState();
			}
		});
		// allocate a thread to handling tx updates
		appwallet.getWallet().addEventListener(this, Executors.newSingleThreadExecutor());
	}

	private void saveState() {
		try (OutputStream file = new FileOutputStream(loc);
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);) {
			output.writeObject(state);
		} catch (IOException e) {
			log.error("Could not save proof progress file");
			e.printStackTrace();
		}
	}

	/**
	 * Register a proof to track
	 * @param tx - Transaction that proves the file
	 * @param file - File that is being proved
	 * @param fileHash - Pre-calculated hash of file being proved
	 */
	public synchronized void addProof(Transaction tx, File file, byte[] fileHash) {
		ProofInProgress pp = new ProofInProgress(file, tx.getHash(), fileHash);
		state.proofs.add(pp);
		state.lookup.put(tx, pp);
	}
	
	public synchronized void completeProof(Transaction tx, ProofInProgress proof) {
		
		byte[] txid = tx.getHash().getBytes();
		
		BlockStore store = BlockchainManager.INSTANCE.getChain().getBlockStore();
		Map<Sha256Hash, Integer> blocksin = tx.getAppearsInHashes();
		
		if (blocksin.size() == 0) {
			log.warn("TX %s is in no blocks, but is should be.\n", Hex.encodeHexString(txid));
			return;
		}
		
		Sha256Hash optimalBlockHash = null;
		for (Entry<Sha256Hash, Integer> e : blocksin.entrySet()) {
			try {
				if (store.get(e.getKey()) != null) {
					optimalBlockHash = e.getKey();
					break;
				}
			} catch (BlockStoreException e1) {
				log.error("Block store problem");
				e1.printStackTrace();
			}
		}
		if (optimalBlockHash == null) {
			log.warn("couldn't find the block in the store");
			optimalBlockHash = blocksin.keySet().iterator().next();
		}
		
		
		TransactionMerkleSerializer tms = new TransactionMerkleSerializer();
		byte[] path = tms.SerializedPathUpMerkle(txid, optimalBlockHash.getBytes());
		
		if (path == null) {
			log.error("requested path failure %s %s\n", 
					Hex.encodeHexString(txid), optimalBlockHash.toString());
			return;
		}
		
		Proof completeProof = new Proof(proof.fileToProve.getName(), proof.time, tx.getUpdateTime(),
				path, txid, proof.fileHash, optimalBlockHash.getBytes(), proof.fileHash);
		
		
		completeProof.writeProofToFile(proof.fileToProve.getAbsolutePath() + ".dproof");
		
		state.lookup.remove(tx);
	}
	
	/**
	 * Checks if a TX is related to a proved file AND ready
	 * @param tx
	 */
	public synchronized void checkTx(Transaction tx) {
		int confirmations = tx.getConfidence().getDepthInBlocks();
		if (confirmations < 1) {
			return;
		}
		ProofInProgress proof = state.lookup.get(tx);
		if (proof == null) {
			return;
		}
		
		completeProof(tx, proof);
	}

	private class ProofManagerState implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2507079320356349621L;

		private List<ProofInProgress> proofs;
		transient private Map<Transaction, ProofInProgress> lookup = new HashMap<Transaction, ProofInProgress>();

		public ProofManagerState() {
			proofs = new ArrayList<ProofInProgress>();
		}

		private void initialize() {
			Wallet w = appwallet.getWallet();
			for (ProofInProgress proof : proofs) {
				Transaction reftx = w.getTransaction(proof.txid);
				if (reftx == null) {
					log.error("TXID: %s is not included in wallet", proof.txid.toString());
				} else if (!proof.fileToProve.exists()) {
					log.error("File " + proof.fileToProve.getAbsolutePath() + " to be proved not found.");
				} else  {
					lookup.put(reftx, proof);
				}
			}
		}

	}

	@Override
	public void onKeysAdded(List<ECKey> keys) {
		// do nothing
	}

	@Override
	public void onCoinsReceived(Wallet wallet, Transaction tx,
			Coin prevBalance, Coin newBalance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance,
			Coin newBalance) {
		// do nothing
	}

	@Override
	public void onReorganize(Wallet wallet) {
		// do nothing
	}

	@Override
	public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
		checkTx(tx);
	}

	@Override
	public void onWalletChanged(Wallet wallet) {
		// do nothing
	}

	@Override
	public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
		// do nothing
	}
}
