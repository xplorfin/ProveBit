package org.provebit.merkle.tests;

import static org.junit.Assert.*;

import org.apache.commons.codec.binary.Hex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.provebit.merkle.Merkle;

public class merkleTests {
	static String COMPLETEDIR = "/tests/org/provebit/merkle/tests/testCompleteDir";
	static String INCOMPLETEDIR = "/tests/org/provebit/merkle/tests/testIncompleteDir";
	static String completeDirPath;
	static String incompleteDirPath;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		completeDirPath = new java.io.File( "." ).getCanonicalPath() + COMPLETEDIR;
		incompleteDirPath = new java.io.File( "." ).getCanonicalPath() + INCOMPLETEDIR;
		System.out.println(completeDirPath);
		System.out.println(incompleteDirPath);
	}

	@Test
	public void testTreeHeightEvenLeaves() {
		Merkle mTree = new Merkle(completeDirPath);
		mTree.makeTree();
		assertTrue(mTree.getHeight() == 3);
	}
	
	@Test
	public void testTreeHeightOddLeaves() {
		Merkle mTree = new Merkle(incompleteDirPath);
		mTree.makeTree();
		assertTrue(mTree.getHeight() == 4);
	}

	@Test
	public void testNumLeavesEven() {
		Merkle mTree = new Merkle(completeDirPath);
		mTree.makeTree();
		assertTrue(mTree.getNumLeaves() == 8);
	}
	
	@Test
	public void testNumLeavesOdd() {
		Merkle mTree = new Merkle(incompleteDirPath);
		mTree.makeTree();
		assertTrue(mTree.getNumLeaves() == 14);
	}
	
	@Test
	public void testTreeSizeEven() {
		Merkle mTree = new Merkle(completeDirPath);
		mTree.makeTree();
		assertTrue(mTree.getTreeSize() == 15);
	}
	
	@Test
	public void testTreeSizeOdd() {
		Merkle mTree = new Merkle(incompleteDirPath);
		mTree.makeTree();
		assertTrue(mTree.getTreeSize() == 28);
	}
	
	@Test
	public void testLeafPositions() {
		Merkle mTree = new Merkle(completeDirPath);
		mTree.makeTree();
		byte[][] tree = mTree.getTree();
		int i = (int) Math.pow(2, mTree.getHeight()) - 1;
		for (; i < Math.pow(2, mTree.getHeight()) - 1 + mTree.getNumLeaves(); i++) {
			assertTrue(Hex.encodeHexString(tree[i]).length() == 64);
		}
	}
}
