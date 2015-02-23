package org.provebit.proof.keysys;

import java.util.Arrays;

public abstract class AbstractKeyNode {
	
	protected abstract byte[] keyLookupRecurse(String[] vals, int i);

}
