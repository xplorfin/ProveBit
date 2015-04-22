package org.provebit.ui.main;

import java.util.Observable;

public class MainModel extends Observable {
	public MainModel() {
		// Empty for now
	}
	
	@SuppressWarnings("unused")
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}
