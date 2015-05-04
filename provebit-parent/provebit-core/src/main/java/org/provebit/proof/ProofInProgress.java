package org.provebit.proof;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

public class ProofInProgress implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3344485544246630480L;

	public File fileToProve;
	public Sha256Hash txid;
	public Date time = new Date();
	public byte[] fileHash;
	public boolean isDone = false;
	
	public ProofInProgress(File fileToProve, Sha256Hash txid, byte[] fileHash) {
		this.fileToProve = fileToProve;
		this.txid = txid;
		this.fileHash = fileHash;
	}
}
