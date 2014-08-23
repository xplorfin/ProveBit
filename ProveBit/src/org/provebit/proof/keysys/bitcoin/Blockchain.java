package org.provebit.proof.keysys.bitcoin;

import java.math.BigInteger;
import java.util.Arrays;

import org.provebit.proof.keysys.AbstractKeyNode;
import org.provebit.proof.keysys.KeyNotFoundException;

import com.google.bitcoin.core.Sha256Hash;

public class Blockchain extends AbstractKeyNode {
	private static Blockchain instance = new Blockchain();
	
	public static byte[] keyLookup(String[] vals, int i) {
		return instance.keyLookupRecurse(vals, i);
	}

	@Override
	protected byte[] keyLookupRecurse(String[] vals, int i) {
		String blockHashStr = vals[i];;
		try {
			if (blockHashStr.length() != 64)
				throw new KeyNotFoundException("invalid block hash " + blockHashStr + " in " + Arrays.toString(vals));
			new BigInteger(vals[i], 16); // test parsing

			Sha256Hash blockHash = new Sha256Hash(blockHashStr);
			
			// TODO connect to real wallet
		} catch (NumberFormatException e) {
			throw new KeyNotFoundException("invalid block hash " + blockHashStr + " in " + Arrays.toString(vals));
		}
		return null;
	}

}
