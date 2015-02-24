package org.provebit.proof.keysys;

import java.lang.reflect.InvocationTargetException;

public class RootKey extends AbstractKeyNode {
	private static RootKey instance = new RootKey();
	
	public static byte[] keyLookup(String[] vals) {
		return instance.keyLookupRecurse(vals, 0);
	}
	
	public static byte[] keyLookup(String val) {
		String[] keys = val.split(":");
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].length() == 0)
				throw new RuntimeException("Key length cannot be 0");
		}
		return keyLookup(keys);
	}

	@Override
	protected byte[] keyLookupRecurse(String[] vals, int i) {
		String thisPackage = getClass().getPackage().getName();
		String nameCap = Character.toUpperCase(vals[i].charAt(0)) + vals[i].substring(1); 
		String subClass = thisPackage + "." + vals[i].toLowerCase() + "." + nameCap;
		try {
			return (byte[]) Class.forName(subClass).getMethod("keyLookup", String[].class, int.class).invoke(null, (Object) vals, i + 1);
		} catch (ClassNotFoundException e) {
			throw new KeyNotFoundException("key " + vals[i] + "not found in root");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new RuntimeException("method needed in class " + subClass + " not found");
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException("method needed in class " + subClass + " not accessible");
		} catch (IllegalAccessException|IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException("failure to exec method in class " + subClass);
		} catch (InvocationTargetException e) {
			Throwable originator = e.getCause();
			if (originator instanceof RuntimeException) {
				throw (RuntimeException) originator;
			}
			else
				throw new RuntimeException(originator);
		}
	}
}
