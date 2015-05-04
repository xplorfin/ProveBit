package org.provebit.systems.bitcoin.ui.gui;

import java.util.List;
import java.util.Observable;

import javax.swing.SwingUtilities;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.script.Script;
import org.provebit.systems.bitcoin.wallet.ApplicationWallet;

public class WalletModel extends Observable {

	private final static ApplicationWallet appwallet = ApplicationWallet.INSTANCE;
	//TODO: Find a good location for this.
	private Coin lastBalance = null;
	private Address lastAddress = null;
	
	/**
	 * Default constructor: instantiates the application wallet with the controller
	 */
	public WalletModel() {
		appwallet.addEventListener(new WalletEventHandler());
	}

	
	/**
	 * Wrapper function to send coins using the application wallet
	 * @param amount
	 * @param destAddress
	 * @throws AddressFormatException
	 * @throws InsufficientMoneyException
	 */
	public void simpleSendCoins(Coin amount, String destAddress)
			throws AddressFormatException, InsufficientMoneyException {
		appwallet.simpleSendCoins(amount, destAddress);
	}
	
	/**
	 * Wrapper function to put a hash into a transaction and send the transaction
	 * @param hash
	 * @return
	 * @throws InsufficientMoneyException
	 */
	public Transaction proofTX(byte[] hash) throws InsufficientMoneyException {
		return appwallet.proofTX(hash);
	}
	
	/**
	 * Get balance of wallet
	 * @return wallet balance
	 */
	public Coin getBalance() {
		return appwallet.getBalance();
	}
	
	/**
	 * Get current wallet receiving address
	 * @return bitcoin address
	 */
	public Address getReceivingAddress() {
		return appwallet.getReceivingAddress();
	}
	
	/**
	 * Small class to handle Wallet events
	 * @author noahmalmed
	 *
	 */
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
