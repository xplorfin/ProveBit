package org.provebit.systems.bitcoin.ui.gui;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.script.Script;
import org.provebit.proof.Proof;
import org.provebit.systems.bitcoin.wallet.ApplicationWallet;

public class WalletModel extends Observable {

	private final static ApplicationWallet appwallet = ApplicationWallet.INSTANCE;
	//TODO: Find a good location for this.
	private final String PROOF_DIRECTORY = "src/main/resources/org/provebit/proofs/";
	
	public WalletModel() {
		appwallet.addEventListener(new WalletEventHandler());
	}
	
	private Coin lastBalance = null;
	
	public void simpleSendCoins(Coin amount, String destAddress)
			throws AddressFormatException, InsufficientMoneyException {
		appwallet.simpleSendCoins(amount, destAddress);
	}
	
	public Transaction proofTX(byte[] hash) throws InsufficientMoneyException {
		return appwallet.proofTX(hash);
	}
	
	/**
	 * Note returns null if any exception is caught while trying to generate proof
	 * @param fileToProve
	 * @return
	 */
	public Proof generateProofFromFile(File fileToProve){
		MessageDigest md;
		Proof proof = null;
		if(fileToProve.isFile()){
	        try {
	        	// Hash File
				md = MessageDigest.getInstance("SHA-256");
				byte[] fileBytes = FileUtils.readFileToByteArray(fileToProve);
		        md.update(fileBytes);
		        byte[] fileHash = (md.digest());
		        //String fileName, Timestamp iTime, byte[] transID, byte[] merkleRoot, byte[] fileHash
		        
		        // Generate current time Timestamp
		        Calendar cal = Calendar.getInstance();
		        Date currentTime = cal.getTime();
		        Timestamp idealTime = new Timestamp(currentTime.getTime());
		        
		        // Submit hash to be put into a transaction
		        Transaction trans = appwallet.proofTX(fileHash);
		        
		        //Generate Proof (For one file, the merkle root is the file hash)
		        proof = new Proof(fileToProve.getName(), idealTime, trans.getHash().getBytes(), fileHash, fileHash);
		        
		        //Write proof to a file
		        String proofFile = PROOF_DIRECTORY + proof.getFileName() + ".proof";
		        // Touch the file
		        File pFile = new File(proofFile);
		        pFile.createNewFile();
		        
		        proof.writeProofToFile(proofFile);
		        
		        
			} catch (NoSuchAlgorithmException | IOException | InsufficientMoneyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
        return proof;
       

	}
	
	public Coin getBalance() {
		return appwallet.getBalance();
	}
	
	private Address lastAddress = null;
	
	public Address getReceivingAddress() {
		return appwallet.getReceivingAddress();
	}
	
	private class WalletEventHandler implements WalletEventListener {

		@Override
		public void onKeysAdded(List<ECKey> keys) {
			notifyChange();
		}

		@Override
		public void onCoinsReceived(Wallet wallet, Transaction tx,
				Coin prevBalance, Coin newBalance) {
			// caught by onWalletChanged
		}

		@Override
		public void onCoinsSent(Wallet wallet, Transaction tx,
				Coin prevBalance, Coin newBalance) {
			// caught by onWalletChanged

		}

		@Override
		public void onReorganize(Wallet wallet) {
			// caught by onWalletChanged

		}

		@Override
		public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
			// caught by onWalletChanged

		}

		@Override
		public void onWalletChanged(Wallet wallet) {
			notifyChange();
		}

		@Override
		public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
			notifyChange();
		}
		
	}
	
	private void notifyChange() {
		// for performance reasons we only set changed when a value has actually changed
		boolean diff = false;
		
		Address newAddress = getReceivingAddress();
		if (!newAddress.equals(lastAddress)) {
			diff = true;
		}
		lastAddress = newAddress;
		
		Coin newBalance = getBalance();
		if (!newBalance.equals(lastBalance)) {
			diff = true;
		}
		lastBalance = newBalance;
		
		if (!diff) {
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setChanged();
				notifyObservers();
			}
		});

	}

}
