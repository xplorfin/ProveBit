package org.provebit.ui.main;

import java.util.Observable;

import org.provebit.proof.ProofManager;

public class MainModel extends Observable {
	public MainModel() {
		ProofManager.INSTANCE.toString();
	}
	
	@SuppressWarnings("unused")
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}
