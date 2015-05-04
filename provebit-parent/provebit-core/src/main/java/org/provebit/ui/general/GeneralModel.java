package org.provebit.ui.general;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.provebit.proof.ProofInProgress;
import org.provebit.proof.ProofManager;
import org.provebit.systems.bitcoin.wallet.ApplicationWallet;

public class GeneralModel extends Observable implements Observer {
	
	private final static ApplicationWallet appwallet = ApplicationWallet.INSTANCE;
	
	private ProofManager.ProofManagerEventHandler eventh;
	private ProofStartComparator pst = new ProofStartComparator();
	
	public GeneralModel() {
		eventh = ProofManager.INSTANCE.new ProofManagerEventHandler();
		eventh.addObserver(this);
	}
	
	public Transaction proofTX(byte[] hash) throws InsufficientMoneyException {
		return appwallet.proofTX(hash);
	}
	
	public String[][] getStatusList() {
		List<ProofInProgress> proofs = eventh.getProofs();
		proofs.sort(pst);
		
		String[][] out = new String[proofs.size()][];
		int i = 0;
		for (ProofInProgress p : proofs) {
			String[] row = new String[2];
			row[0] = p.fileToProve.getAbsolutePath();
			row[1] = p.isDone ? "Complete" : "In progress";
			out[i] = row;
			i++;
		}
		
		return out;
	}
	
	private static class ProofStartComparator implements Comparator<ProofInProgress> {
		@Override
		public int compare(ProofInProgress arg0, ProofInProgress arg1) {
			return arg1.time.compareTo(arg0.time);

		}
	}
	
	@SuppressWarnings("unused")
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}

	@Override
	public void update(Observable o, Object arg) {
		// pass up event
		notifyChange();
	}
}
