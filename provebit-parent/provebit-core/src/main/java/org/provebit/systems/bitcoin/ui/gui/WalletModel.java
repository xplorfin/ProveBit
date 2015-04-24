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
