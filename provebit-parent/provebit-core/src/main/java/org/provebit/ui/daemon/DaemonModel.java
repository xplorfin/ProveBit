package org.provebit.ui.daemon;

import java.util.Observable;

public class DaemonModel extends Observable {
	
	public DaemonModel() {
		
	}
	
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}
