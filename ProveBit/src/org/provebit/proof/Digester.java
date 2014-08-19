package org.provebit.proof;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digester {

	MessageDigest md;
	public Digester(String digestName) {
		try {
			md = MessageDigest.getInstance(digestName);
		} catch (NoSuchAlgorithmException e) {
			// We have a problem with a coded digest type
			e.printStackTrace();
		}
	}
	
	public void addStream(InputStream stream) throws IOException {
		DigestInputStream dis = new DigestInputStream(stream, md);
		while (dis.read() != -1);
	}
	
	public void addBytes(byte[] bytes) {
		md.update(bytes);
	}
	
	public byte[] digest() {
		return md.digest();
	}
}
