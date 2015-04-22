package org.provebit.ui.wallets;

import java.util.Observable;

public class WalletModel extends Observable {
	public WalletModel() {
		
	}
	
	@SuppressWarnings("unused")
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}
