package org.provebit.proof.keysys;

public class KeyNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8778843652588857666L;

	public KeyNotFoundException(String msg) {
		super(msg);
	}
	
	public KeyNotFoundException(Throwable cause) {
		super(cause);
	}

}
