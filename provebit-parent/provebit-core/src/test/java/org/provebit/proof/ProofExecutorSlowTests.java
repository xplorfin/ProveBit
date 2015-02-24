package org.provebit.proof;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import org.bitcoinj.core.Utils;

public class ProofExecutorSlowTests {

	@Test
	public void testComposeExt() {
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// Because of ring buffer of SPV block storage currently being used, this test will expire about Sep 16 2014
		Object[] cats = {
				new Object[] {"op_set", "h:dd3b373e76d8b46e164017526e459c2d17b0255614398dcf6bd76605b42fe9db"},
				new Object[] {"op_rev"},
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
	
	@Test
	public void testExectuteBlockCmpComposeExt() {
		Object[] shax = {
				new Object[] {"op_func", "hash256", new Object[] {
						new Object[] {"op_sha256"},
						new Object[] {"op_sha256"},
				}},
				new Object[] {"op_func", "lnode", new Object[] {
						new Object[] {"op_cat", "m:-1", "m:1"},
						new Object[] {"f_hash256"}
				}},
				new Object[] {"op_func", "rnode", new Object[] {
						new Object[] {"op_cat", "m:1", "m:-1"},
						new Object[] {"f_hash256"}
				}},
				// the TX itself
				new Object[] {"op_set", "h:01000000018f0277a08fd4e2db5699fb389a1ae3d6526942f02812b477415e38e2634ba72b"
						+ "010000008a4730440220406df872186e6e13fe3f7a3b04f0f363147f54886bd1298f9d525dd70775b806022074"
						+ "a2563994ef6eaec1baa7faf90a6aece4fa85c79c375e39fa848f7229bf75770141041b58980ba39fd1d48d99c7"
						+ "f3ddd362667bc2f1dadfd59d65d4bfae58ec460c4ea8c3efa8bd3085a0f6ec2d3c28168fc18b438d32e1d6ca5e"
						+ "2d2c207e47124d2affffffff02a0860100000000001976a9143534af93b0ac9d01ddabeb6a7878f697b1e7a1b2"
						+ "88acfed21200000000001976a91471e65db7051d2e42f32910a94a2d498f90ce331488ac00000000"},
				new Object[] {"f_hash256"},
				
				// the below is a TXID, a LE form of a TX hash in hex, which is translated by H: to BE (alt to above)
				//new Object[] {"op_set", "H:483977aa5d33d79e1ddc2414a63199c69ccf4ba77a965a4416a9cc7f606a3a0c"},
				
				// merkle tree build
				new Object[] {"f_lnode", "H:6eac8b99612f3b056f853d5dc23958c8962fd713e035f3a2130380dcf0850d88"},
				new Object[] {"f_rnode", "H:1b49f61a09902a848db5588f4e802763a2e1b8f1b91ff176ade1dcb0cd695ca2"},
				new Object[] {"f_rnode", "H:c161f8edd924cac55c04d39c4e55d5313a00abd6ceb8bb9f6c9e0a79e8879117"},
				// BE result here is actually understood as LE/reverse of 'real' hash output (directly in header)
				new Object[] {"op_compose_ext", "s:bitcoin:blockchain:0000000000000000256d9dadd7ac229b1c2530cfa989b8686162ba3184436581:header"},
		};
		ProofParser pp = new ProofParser(shax);
		ProofExecutor pe = new ProofExecutor("");
		byte[] res = pe.execute(pp);
		byte[] time = Utils.reverseBytes(Arrays.copyOfRange(res, 68, 72)); // timestamp
		long msSinceEpoch = new BigInteger(1, time).longValue() * 1000;
		Date outDate = new Date(msSinceEpoch);
		try {
			Date correct = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z", Locale.ENGLISH).parse("2014-08-24 03:29:06 GMT");
			assertEquals(correct, outDate);
		} catch (ParseException e) {
			fail("parse exception");
		}
	}

}
