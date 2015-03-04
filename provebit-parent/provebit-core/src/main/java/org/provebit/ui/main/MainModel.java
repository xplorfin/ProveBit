package org.provebit.ui.main;

import java.util.Observable;

public class MainModel extends Observable {
	public MainModel() {
		// Empty for now
	}
	
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}
