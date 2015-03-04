package org.provebit.ui.wallet;

import java.util.Observable;

public class WalletModel extends Observable {
	public WalletModel() {
		
	}
	
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}
