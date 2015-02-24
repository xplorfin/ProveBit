package org.provebit.proof.keysys.bitcoin;

import java.util.Arrays;

import org.provebit.proof.keysys.AbstractKeyNode;
import org.provebit.proof.keysys.KeyNotFoundException;

public class Bitcoin extends AbstractKeyNode {
	private static Bitcoin instance = new Bitcoin();

	public static byte[] keyLookup(String[] vals, int i) {
		return instance.keyLookupRecurse(vals, i);
	}

	@Override
	protected byte[] keyLookupRecurse(String[] vals, int i) {
		if (vals[i].equals("blockchain"))
			return Blockchain.keyLookup(vals, i + 1);
		throw new KeyNotFoundException(vals[i] + " in " + Arrays.toString(vals));
	}

}
