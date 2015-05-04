package org.provebit.ui.main;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.provebit.proof.ProofInProgress;
import org.provebit.proof.ProofManager;
import org.provebit.proof.ProofManager.ProofManagerEventHandler;

public class MainModel extends Observable implements Observer {
	private ProofManagerEventHandler eventh;

	public MainModel() {
		ProofManager.INSTANCE.toString();
		eventh = ProofManager.INSTANCE.new ProofManagerEventHandler();
		eventh.addObserver(this);
	}
	
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
	
	public int getStatusCount() {
		List<ProofInProgress> proofs = eventh.getProofs();
		int ip = 0;
		for (ProofInProgress p : proofs) {
			if (!p.isDone) {
				ip++;
			}
		}
		return ip;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// pass up
		notifyChange();
	}
}
