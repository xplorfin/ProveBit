package org.provebit.merkle;

/**
 * This class is just a data class used to describe a step of a tree traversal
 * @author Noah Malmed
 * @organization ProveBit
 * @version 0.1
 */
public class MerklePathStep {
	private boolean onLeft;
	private byte[] fullHash;
	
	public MerklePathStep(boolean onLeft, byte[] fullHash){
		this.onLeft = onLeft;
		this.fullHash = fullHash;
	}

	public boolean onLeft() {
		return onLeft;
	}

	public void setOnLeft(boolean onLeft) {
		this.onLeft = onLeft;
	}

	public byte[] getFullHash() {
		return fullHash;
	}

	public void setFullHash(byte[] fullHash) {
		this.fullHash = fullHash;
	}
	
	
}
