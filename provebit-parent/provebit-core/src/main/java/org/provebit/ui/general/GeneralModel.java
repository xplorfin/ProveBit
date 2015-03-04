package org.provebit.ui.general;

import java.util.Observable;

public class GeneralModel extends Observable {
	public GeneralModel() {
		
	}
	
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}
