package org.provebit.proof;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.google.bitcoin.core.Utils;

public class ProofExecutorTests {

	@Test
	public void testExectuteCat() {
		Object[] cats = {
				new Object[] {"op_cat", "s:a", "s:b"},
				new Object[] {"op_cat", "s:c", "m:-1"},
				new Object[] {"op_cat", "s:bag"},
				new Object[] {"op_cat", "h:65"}
		};
		ProofParser pp = new ProofParser(cats);
		InputStream dummy = new ByteArrayInputStream(new byte[] {});
		ProofExecutor pe = new ProofExecutor(dummy);
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("cabbage", out);
	}
	
	@Test
	public void testExectuteSHA2() {
		Object[] shax = {
				new Object[] {"op_sha256", "s:test", "m:-1", "s:stuff"},
		};
		ProofParser pp = new ProofParser(shax);
		String in = "ing ";
		InputStream dummy = null;
		try {
			dummy = new ByteArrayInputStream(in.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ProofExecutor pe = new ProofExecutor(dummy);
		byte[] res = Utils.reverseBytes(pe.execute(pp));
		String out = Utils.bytesToHexString(res);
		assertEquals("3367a47f48cd5948e68ed649f74d6fcc60725a881593543ae646a9b63f55fe19", out);
	}
	
	@Test
	public void testRev() {
		Object[] rev = {
				new Object[] {"op_rev"},
		};
		ProofParser pp = new ProofParser(rev);
		String in = "this is a test";
		InputStream dummy = null;
		try {
			dummy = new ByteArrayInputStream(in.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ProofExecutor pe = new ProofExecutor(dummy);
		pe.restrictArbitraryStreamBuffer = false;
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("tset a si siht", out);
	}

}
