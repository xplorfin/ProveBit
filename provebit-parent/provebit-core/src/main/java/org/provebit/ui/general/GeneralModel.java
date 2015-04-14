package org.provebit.ui.general;

import java.util.Observable;

import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.provebit.systems.bitcoin.wallet.ApplicationWallet;

public class GeneralModel extends Observable {
	
	private final static ApplicationWallet appwallet = ApplicationWallet.INSTANCE;
	
	public GeneralModel() {
		// TODO
	}
	
	public Transaction proofTX(byte[] hash) throws InsufficientMoneyException {
		return appwallet.proofTX(hash);
	}
	
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}
