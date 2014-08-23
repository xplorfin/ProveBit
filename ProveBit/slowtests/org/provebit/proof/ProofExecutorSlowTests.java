package org.provebit.proof;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import com.google.bitcoin.core.Utils;

public class ProofExecutorSlowTests {

	@Test
	public void testComposeExt() {
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// Because of ring buffer of SPV block storage currently being used, this test will expire about Sep 16 2014
		Object[] cats = {
				new Object[] {"op_set", "h:00"},
				new Object[] {"op_compose_ext", "s:bitcoin:blockchain:00000000000000001d8aa68e6862dc94268277e947e09a75584572eee913db19:header"},
		};
		ProofParser pp = new ProofParser(cats);
		ProofExecutor pe = new ProofExecutor("test");
		byte[] res = pe.execute(pp);
		byte[] time = Utils.reverseBytes(Arrays.copyOfRange(res, 68, 72)); // timestamp
		long msSinceEpoch = new BigInteger(1, time).longValue() * 1000;
		Date outDate = new Date(msSinceEpoch);
		try {
			Date correct = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z", Locale.ENGLISH).parse("2014-08-23 19:27:40 GMT");
			assertEquals(correct, outDate);
		} catch (ParseException e) {
			fail("parse exception");
		}
	}

}
