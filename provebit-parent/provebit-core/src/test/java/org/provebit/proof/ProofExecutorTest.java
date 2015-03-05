package org.provebit.proof;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Utils;
import org.junit.Test;

public class ProofExecutorTest {

	@Test
	public void testExecuteCat() {
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
	public void testExecuteSHA2() {
		Object[] shax = {
				new Object[] {"op_sha256", "s:test", "m:-1", "s:stuff"},
		};
		ProofParser pp = new ProofParser(shax);
		String in = "ing ";
		InputStream text = null;
		try {
			text = new ByteArrayInputStream(in.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ProofExecutor pe = new ProofExecutor(text);
		byte[] res = pe.execute(pp);
		String out = Hex.encodeHexString(res);
		assertEquals("3367a47f48cd5948e68ed649f74d6fcc60725a881593543ae646a9b63f55fe19", out);
	}
	
	@Test
	public void testExectuteBlockCmp() {
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
				// initial tx hash
				new Object[] {"op_set", "H:02167a3edeabb6e154c124196b58bd709b7f702af84a484eadb7c379e0593435"},
				// merkle tree build
				new Object[] {"f_rnode", "H:e18eaa19633f9cf6a5da71b2037ed8132c6c2f62c95a906489de992fd2f16866"},
				new Object[] {"f_lnode", "H:71709900808db3ec5ab92cbb359b1e1b0ebbf31194bdeda47f0dd5e7567838ef"},
				new Object[] {"f_rnode", "H:c161f8edd924cac55c04d39c4e55d5313a00abd6ceb8bb9f6c9e0a79e8879117"}
		};
		ProofParser pp = new ProofParser(shax);
		ProofExecutor pe = new ProofExecutor("");
		//byte[] res = pe.execute(pp);
		//System.out.println(new String(res));
		byte[] res = Utils.reverseBytes(pe.execute(pp));
		String out = Hex.encodeHexString(res);
		//assertEquals("1b49f61a09902a848db5588f4e802763a2e1b8f1b91ff176ade1dcb0cd695ca2", out);
		assertEquals("22bc66be6c79df92304f85a2b93d561e047e2fee71ce4708443c86d30c84d556", out);
	}
	
	@Test
	public void testRev() {
		Object[] rev = {
				new Object[] {"op_rev"},
		};
		ProofParser pp = new ProofParser(rev);
		String in = "this is a test";
		InputStream text = null;
		try {
			text = new ByteArrayInputStream(in.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ProofExecutor pe = new ProofExecutor(text);
		pe.restrictArbitraryStreamBuffer = false;
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("tset a si siht", out);
	}
	
	@Test
	public void testFileInput() {
		Object[] shan = {
				new Object[] {"op_sha256"},
		};
		ProofParser pp = new ProofParser(shan);
		InputStream file = getClass().getResourceAsStream("btc-logo.png");
		
		ProofExecutor pe = new ProofExecutor(file);
		byte[] res = pe.execute(pp);
		String out = Hex.encodeHexString(res);
		assertEquals("11961d79a8fde725e878473bd3497adff1fb6d362c1378e9eb182c870a617a2a", out);
		
		Object[] shan2 = {
				new Object[] {"op_sha256", "s:a", "m:-1", "s:b"},
		};
		ProofParser pp2 = new ProofParser(shan2);
		InputStream file2 = getClass().getResourceAsStream("btc-logo.png");
		
		ProofExecutor pe2 = new ProofExecutor(file2);
		byte[] res2 = pe2.execute(pp2);
		String out2 = Hex.encodeHexString(res2);
		// (echo -n a; cat btc-logo.png; echo -n b) | sha256sum
		assertEquals("99788d6eb2d9938ed6d455f9af4e9a8ac7e1fc3b5d146a27ee20dc10b22e9ccb", out2);
	}
	
	@Test
	public void testFunctions() {
		Object[] shan = {
				new Object[] {"op_func", "cats", new Object[] {
						new Object[] {"op_cat", "m:1", "s:s"}
				}},
				new Object[] {"op_func", "words", new Object[] {
						new Object[] {"op_cat", "s:This ", "m:1"}, // 'This test'
						new Object[] {"f_cats", "m:-1"}, // 'This tests'
						new Object[] {"op_store", 32},
						new Object[] {"f_cats", "m:2"}, // 'things'
						new Object[] {"op_cat", "s: ", "m:-1"}, // ' things'
						new Object[] {"op_cat", "m:32", "m:-1"}, // 'This tests things'
						new Object[] {"op_cat", "s:."} // 'This tests things.'
				}},
				new Object[] {"f_words", "s:test", "s:thing"}
		};
		ProofParser pp = new ProofParser(shan);		
		ProofExecutor pe = new ProofExecutor("");
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("This tests things.", out);
	}
	
	@Test
	public void testStoreLoadSetSwap() {
		Object[] cats = {
				new Object[] {"op_store", 35},
				new Object[] {"op_set", "s:bb"},
				new Object[] {"op_store", 36},
				new Object[] {"op_load", 35},
				new Object[] {"op_store", 37},
				new Object[] {"op_set", "s:c"},
				new Object[] {"op_cat", "m:35"},
				new Object[] {"op_cat", "m:36"},
				new Object[] {"op_cat", "m:37"},
				new Object[] {"op_swap", 36},
				new Object[] {"op_cat", "m:36"}
		};
		ProofParser pp = new ProofParser(cats);
		ProofExecutor pe = new ProofExecutor("aaa");
		pe.restrictArbitraryStreamBuffer = false;
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("bbcaaabbaaa", out);
	}
	
	@Test
	public void testCompose() {
		Object[] cats = {
				new Object[] {"op_cat", "s:ing"},
				new Object[] {"op_compose", "s:We are testing..."},
				new Object[] {"op_cat", "s: things."}
		};
		ProofParser pp = new ProofParser(cats);
		ProofExecutor pe = new ProofExecutor("test");
		pe.restrictArbitraryStreamBuffer = false;
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("We are testing... things.", out);
		
		Object[] cats2 = {
				new Object[] {"op_cat", "s:ing"},
				new Object[] {"op_compose", "s:We are tes?ting..."},
				new Object[] {"op_cat", "s: things."}
		};
		try {
			ProofParser pp2 = new ProofParser(cats2);
			ProofExecutor pe2 = new ProofExecutor("test");
			pe2.restrictArbitraryStreamBuffer = false;
			@SuppressWarnings("unused")
			byte[] res2 = pe2.execute(pp2);
			fail("We should not have a successful execution");
		} catch (ProofExecutor.ProgramDieException e) {
			// We should make it here
		}
	}
	
}
