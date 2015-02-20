package org.provebit.merkle.tests;

import static org.junit.Assert.*;

import org.apache.commons.codec.binary.Hex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.provebit.merkle.Merkle;

public class MerkleTests {
    static String COMPLETEDIR = "/tests/org/provebit/merkle/tests/testCompleteDir";
    static String INCOMPLETEDIR = "/tests/org/provebit/merkle/tests/testIncompleteDir";
    static String RECURSIVEDIR = "/tests/org/provebit/merkle/tests/testRecursiveDir";
    static String RECURSIVEDIR2 = "/tests/org/provebit/merkle/tests/testRecursiveDir2";
    static String EMPTYDIR = "/tests/org/provebit/merkle/tests/testEmptyDir";
    static String completeDirPath;
    static String incompleteDirPath;
    static String recursiveDirPath;
    static String recursiveDir2Path;
    static String emptyDirPath;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        completeDirPath = new java.io.File( "." ).getCanonicalPath() + COMPLETEDIR;
        incompleteDirPath = new java.io.File( "." ).getCanonicalPath() + INCOMPLETEDIR;
        recursiveDirPath = new java.io.File( "." ).getCanonicalPath() + RECURSIVEDIR;
        recursiveDir2Path = new java.io.File( "." ).getCanonicalPath() + RECURSIVEDIR2;
        emptyDirPath = new java.io.File( "." ).getCanonicalPath() + EMPTYDIR;
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
        assertTrue(mTree.getTreeSize() == 29);
    }
    
    @Test
    public void testNoTree() {
    	Merkle mTree = new Merkle(incompleteDirPath);
    	assertNull(mTree.getTree());
    }

    @Test
    public void testLeafPositions() {
        Merkle mTree = new Merkle(completeDirPath);
        mTree.makeTree();
        byte[][] tree = mTree.getTree();
        int i = (int) Math.pow(2, mTree.getHeight()) - 1;
        for (; i < Math.pow(2, mTree.getHeight()+1) - 1; i++) {
            assertTrue(Hex.encodeHexString(tree[i]).length() == 64);
        }
    }

    @Test
    public void testSortedLeaves() {
        Merkle mTree = new Merkle(completeDirPath);
        mTree.makeTree();
        byte[][] tree = mTree.getTree();
        int i = (int) Math.pow(2, mTree.getHeight()) - 1;
        String last = Hex.encodeHexString(tree[i]);
        for (; i < Math.pow(2, mTree.getHeight()) - 1 + mTree.getNumLeaves(); i++) {
            String curr = Hex.encodeHexString(tree[i]);
            assertTrue(curr.compareTo(last) >= 0);
            last = curr;
        }
    }

    @Test
    public void testRecursiveSearch() {
        Merkle mTree = new Merkle(recursiveDirPath);
        mTree.setRecursive(true);
        mTree.makeTree();
        assertTrue(mTree.getNumLeaves() == 8);
    }

    @Test
    public void testRecursiveSearch2() {
        Merkle mTree = new Merkle(recursiveDir2Path);
        mTree.setRecursive(true);
        mTree.makeTree();
        assertTrue(mTree.getNumLeaves() == 8);
    }
    
    @Test
    public void testFalseRecursiveSearch() {
    	Merkle mTree = new Merkle(recursiveDirPath);
    	mTree.setRecursive(false);
    	mTree.makeTree();
    	assertTrue(mTree.getNumLeaves() == 4);
    }
    
    @Test
    public void testFalseRecursiveSearch2() {
    	Merkle mTree = new Merkle(recursiveDir2Path);
    	mTree.setRecursive(false);
    	mTree.makeTree();
    	assertTrue(mTree.getNumLeaves() == 2);
    }

    @Test
    public void testRootHashEquivalence() {
        Merkle mTree1 = new Merkle(recursiveDirPath);
        Merkle mTree2 = new Merkle(recursiveDir2Path);
        mTree1.setRecursive(true);
        mTree2.setRecursive(true);
        mTree1.makeTree();
        mTree2.makeTree();
        String tree1Root = Hex.encodeHexString(mTree1.getRootHash());
        String tree2Root = Hex.encodeHexString(mTree2.getRootHash());
        assertTrue(tree1Root.compareTo(tree2Root) == 0);
    }

    @Test
    public void testEmptyDirectory() {
        Merkle mTree = new Merkle(emptyDirPath);
        assertTrue(mTree.makeTree() == null);
    }
}
