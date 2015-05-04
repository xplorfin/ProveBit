package org.provebit.proof;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
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

/**
 * The ProofManager creates and tracks in progress proofs.
 * When the proofs are completed the are written to disk with file name of the 
 * original file, plus the .dproof extension.
 * 
 * @author Steve Halm
 */
public enum ProofManager implements WalletEventListener {
	INSTANCE;

	private final Logger log = LoggerFactory.getLogger(CompleteHeaderStore.class);
	
	private ProofManagerState state;
	private File loc;
	private final String FILENAME = "proofstate.bin";
	private ApplicationWallet appwallet = ApplicationWallet.INSTANCE;
	private List<ProofManagerEventHandler> eventh = new ArrayList<ProofManagerEventHandler>();

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
			log.info("New proof tracker");
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

	/**
	 * Persist the status to disk
	 */
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
		log.info("Tracking proof TX {} for file {}", tx.getHashAsString(), file.getAbsolutePath());
		saveState();
		updated();
	}
	
	/**
	 * Write a completed proof to disk
	 * @param tx - transaction involved in the proof
	 * @param proof - proof to complete
	 */
	public synchronized void completeProof(Transaction tx, ProofInProgress proof) {
		
		byte[] txid = tx.getHash().getBytes();
		
		BlockStore store = BlockchainManager.INSTANCE.getChain().getBlockStore();
		Map<Sha256Hash, Integer> blocksin = tx.getAppearsInHashes();
		
		if (blocksin.size() == 0) {
			log.warn("TX {} is in no blocks, but is should be.\n", Hex.encodeHexString(txid));
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
				return;
			}
		}
		if (optimalBlockHash == null) {
			log.warn("couldn't find the block in the store");
			optimalBlockHash = blocksin.keySet().iterator().next();
		}
		
		StoredBlock optblock = null;
		try {
			optblock = store.get(optimalBlockHash);
		} catch (BlockStoreException e1) {
			e1.printStackTrace();
			return;
		}
		
		TransactionMerkleSerializer tms = new TransactionMerkleSerializer();
		byte[] path = tms.SerializedPathUpMerkle(txid, optimalBlockHash.getBytes());
		
		if (path == null) {
			log.error("requested path failure {} {}\n", 
					Hex.encodeHexString(txid), optimalBlockHash.toString());
			return;
		}
		
		Proof completeProof = new Proof(proof.fileToProve.getName(), proof.time, optblock.getHeader().getTime(),
				path, tx.bitcoinSerialize(), proof.fileHash, optimalBlockHash.getBytes(), proof.fileHash);
		
		
		completeProof.writeProofToFile(proof.fileToProve.getAbsolutePath() + ".dproof");
		
		state.lookup.remove(tx);
		proof.isDone = true;
		saveState();
		log.info("Proof is complete for file {}", proof.fileToProve.getAbsolutePath());
		updated();
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
		
		log.info("Trying to look up confirmed TX: {}", tx.getHashAsString());
		
		ProofInProgress proof = state.lookup.get(tx);
		if (proof == null) {
			return;
		}
		
		if (proof.isDone) {
			return;
		}
		
		log.info("Trying to complete proof for TX: {}", tx.getHashAsString());
		completeProof(tx, proof);
	}

	/**
	 * Container for the serializable state
	 */
	private static class ProofManagerState implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2507079320356349621L;

		private List<ProofInProgress> proofs;
		transient private Map<Transaction, ProofInProgress> lookup;
		transient private ApplicationWallet appwallet;
		transient private Logger log;
		
		public ProofManagerState() {
			proofs = new ArrayList<ProofInProgress>();
			prepareTransient();
		}
		
		private void prepareTransient() {
			lookup = new HashMap<Transaction, ProofInProgress>();
		    appwallet = ApplicationWallet.INSTANCE;
			log = LoggerFactory.getLogger(CompleteHeaderStore.class);
		}

		private void initialize() {
			prepareTransient();
			Wallet w = appwallet.getWallet();
			for (ProofInProgress proof : proofs) {
				Transaction reftx = w.getTransaction(proof.txid);
				if (reftx == null) {
					log.error("TXID: %s is not included in wallet", proof.txid.toString());
				} else if (!proof.fileToProve.exists()) {
					log.error("File " + proof.fileToProve.getAbsolutePath() + " to be proved not found.");
				} else  {
					lookup.put(reftx, proof);
					log.info("Tracking progress of proof for {} : {}", reftx.getHashAsString(), proof.fileToProve.getAbsolutePath());
				}
			}
		}

	}

	/**
	 * Mark an update to the proof manager state, and broadcast the event
	 */
	private void updated() {
		for (ProofManagerEventHandler e : eventh) {
			e.updated();
		}
	}

	/**
	 * Observable component for tracking ProofManager events
	 */
	public class ProofManagerEventHandler extends Observable {
		
		public ProofManagerEventHandler() {
			eventh.add(this);
		}
		
		/**
		 * Get all the managed proofs
		 * @return list of the proofs (safe to modify)
		 */
		public List<ProofInProgress> getProofs() {
			return new ArrayList<ProofInProgress>(state.proofs);
		}
		
		public void updated() {
			setChanged();
			notifyObservers();
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
