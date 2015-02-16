package org.provebit.merkle.tests;

import static org.junit.Assert.*;

import org.apache.commons.codec.binary.Hex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.provebit.merkle.Merkle;

public class merkleTests {
	static String COMMONSPATH = "/commons-io-2.4";
	static String DOCSPATH = COMMONSPATH + "/docs";
	static String commonsDirPath;
	static String docsDirPath;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		commonsDirPath = new java.io.File( "." ).getCanonicalPath() + COMMONSPATH;
		docsDirPath = new java.io.File( "." ).getCanonicalPath() + DOCSPATH;
	}

	@Test
	public void testTreeHeightEvenLeaves() {
		Merkle mTree = new Merkle(commonsDirPath);
		mTree.makeTree();
		assertTrue(mTree.getHeight() == 3);
	}
	
	@Test
	public void testTreeHeightOddLeaves() {
		Merkle mTree = new Merkle(docsDirPath);
		mTree.makeTree();
		assertTrue(mTree.getHeight() == 4);
	}

	@Test
	public void testNumLeavesEven() {
		Merkle mTree = new Merkle(commonsDirPath);
		mTree.makeTree();
		assertTrue(mTree.getNumLeaves() == 8);
	}
	
	@Test
	public void testNumLeavesOdd() {
		Merkle mTree = new Merkle(docsDirPath);
		mTree.makeTree();
		assertTrue(mTree.getNumLeaves() == 14);
	}
	
	@Test
	public void testTreeSizeEven() {
		Merkle mTree = new Merkle(commonsDirPath);
		mTree.makeTree();
		assertTrue(mTree.getTreeSize() == 15);
	}
	
	@Test
	public void testTreeSizeOdd() {
		Merkle mTree = new Merkle(docsDirPath);
		mTree.makeTree();
		assertTrue(mTree.getTreeSize() == 28);
	}
	
	@Test
	public void testLeafPositions() {
		Merkle mTree = new Merkle(commonsDirPath);
		mTree.makeTree();
		byte[][] tree = mTree.getTree();
		int i = (int) Math.pow(2, mTree.getHeight()) - 1;
		for (; i < Math.pow(2, mTree.getHeight()) - 1 + mTree.getNumLeaves(); i++) {
			assertTrue(Hex.encodeHexString(tree[i]).length() == 64);
		}
	}
}
