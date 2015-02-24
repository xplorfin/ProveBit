package org.provebit.proof;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProofParserTests {

	@Test
	public void testProcess() {
		
		Object[] ks = 	{
						new Object[] {"op_func", "test1", new Object[] {new Object[] {"op_sha256", "m:-1", "m:1"}}},
						new Object[] {"op_func", "test2", new Object[] {new Object[] {"op_cat", "m:-1"}}},
						new Object[] {"op_sha256"},
						new Object[] {"op_func", "test3", new Object[] {new Object[] {"op_cat", "m:-1"}}},
						new Object[] {"op_compose_ext", "system.os.type"}
						};
		
		ProofParser pp = new ProofParser(ks);
		assertTrue(pp.funcmap.get("test1") == 0);
		assertTrue(pp.funcmap.get("test2") == 1);	
		assertTrue(pp.funcmap.get("test3") == 3);
	
		ks[3] = new Object[] {"op_func", "test4", "op_cat"};
		try {
			ProofParser pp2 = new ProofParser(ks);
		} catch (RuntimeException e) {
			assertEquals("func ops not in array", e.getMessage());
			//e.printStackTrace();
		}
	}
}
