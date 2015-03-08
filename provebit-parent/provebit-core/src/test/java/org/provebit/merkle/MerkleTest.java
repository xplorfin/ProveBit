package org.provebit.merkle;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.provebit.merkle.Merkle;

public class MerkleTest {
    final public static String COMPLETEDIR = "testCompleteDir";
    final public static String INCOMPLETEDIR = "testIncompleteDir";
    final public static String RECURSIVEDIR = "testRecursiveDir";
    final public static String RECURSIVEDIR2 = "testRecursiveDir2";
    
    @ClassRule
    public static TemporaryFolder emptyFolder = new TemporaryFolder();
    
    public static File completeDirPath;
    public static File incompleteDirPath;
    public static File recursiveDirPath;
    public static File recursiveDir2Path;
    public static File emptyDirPath;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        completeDirPath = new File(MerkleTest.class.getResource(COMPLETEDIR).getPath());
        incompleteDirPath = new File(MerkleTest.class.getResource(INCOMPLETEDIR).getPath());
        recursiveDirPath = new File(MerkleTest.class.getResource(RECURSIVEDIR).getPath());
        recursiveDir2Path = new File(MerkleTest.class.getResource(RECURSIVEDIR2).getPath());
        emptyDirPath = emptyFolder.getRoot();
    }

    @Test
    public void testTreeHeightEvenLeaves() {
        Merkle mTree = new Merkle();
        mTree.addTracking(completeDirPath, false);
        mTree.makeTree();
        assertTrue(mTree.getHeight() == 3);
    }

    @Test
    public void testTreeHeightOddLeaves() {
        Merkle mTree = new Merkle();
        mTree.addTracking(incompleteDirPath, false);
        mTree.makeTree();
        assertTrue(mTree.getHeight() == 4);
    }

    @Test
    public void testNumLeavesEven() {
        Merkle mTree = new Merkle();
        mTree.addTracking(completeDirPath, false);
        mTree.makeTree();
        assertTrue(mTree.getNumLeaves() == 8);
    }

    @Test
    public void testNumLeavesOdd() {
    	Merkle mTree = new Merkle();
        mTree.addTracking(incompleteDirPath, false);
        mTree.makeTree();
        assertTrue(mTree.getNumLeaves() == 14);
    }

    @Test
    public void testTreeSizeEven() {
    	Merkle mTree = new Merkle();
        mTree.addTracking(completeDirPath, false);
        mTree.makeTree();
        assertTrue(mTree.getTreeSize() == 15);
    }

    @Test
    public void testTreeSizeOdd() {
    	Merkle mTree = new Merkle();
        mTree.addTracking(incompleteDirPath, false);
        mTree.makeTree();
        assertTrue(mTree.getTreeSize() == 29);
    }
    
    @Test
    public void testNoTree() {
    	Merkle mTree = new Merkle();
        mTree.addTracking(incompleteDirPath, false);
    	assertNull(mTree.getTree());
    }
    
    @Test
    public void testLeafExists() {
    	Merkle m = new Merkle();
    	m.addTracking(completeDirPath, false);
    	m.makeTree();
    	assertEquals(true, m.existsAsLeaf(m.getLeaves().get(0)));
    }
    
    @Test public void testLeafNoExist() throws DecoderException {
    	Merkle m = new Merkle();
    	m.addTracking(completeDirPath, false);
    	m.makeTree();
    	assertEquals(false, m.existsAsLeaf(Hex.decodeHex("0000000000000000000000000000000000000000000000000000000000000000".toCharArray())));
    }

    @Test
    public void testLeafPositions() {
    	Merkle mTree = new Merkle();
        mTree.addTracking(completeDirPath, false);
        mTree.makeTree();
        byte[][] tree = mTree.getTree();
        int i = (int) Math.pow(2, mTree.getHeight()) - 1;
        for (; i < Math.pow(2, mTree.getHeight()+1) - 1; i++) {
            assertTrue(Hex.encodeHexString(tree[i]).length() == 64);
        }
    }

    @Test
    public void testSortedLeaves() {
    	Merkle mTree = new Merkle();
        mTree.addTracking(completeDirPath, false);
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
        Merkle mTree = new Merkle();
        mTree.addTracking(recursiveDirPath, true);
        mTree.makeTree();
        assertTrue(mTree.getNumLeaves() == 8);
    }

    @Test
    public void testRecursiveSearch2() {
    	Merkle mTree = new Merkle();
        mTree.addTracking(recursiveDir2Path, true);
        mTree.makeTree();
        assertTrue(mTree.getNumLeaves() == 8);
    }
    
    @Test
    public void testFalseRecursiveSearch() {
    	Merkle mTree = new Merkle();
        mTree.addTracking(recursiveDirPath, false);
    	mTree.makeTree();
    	assertTrue(mTree.getNumLeaves() == 4);
    }
    
    @Test
    public void testFalseRecursiveSearch2() {
    	Merkle mTree = new Merkle();
        mTree.addTracking(recursiveDir2Path, false);
    	mTree.makeTree();
    	assertTrue(mTree.getNumLeaves() == 2);
    }

    @Test
    public void testRootHashEquivalence() {
    	Merkle mTree1 = new Merkle();
        mTree1.addTracking(recursiveDirPath, true);
        Merkle mTree2 = new Merkle();
        mTree2.addTracking(recursiveDir2Path, true);
        mTree1.makeTree();
        mTree2.makeTree();
        String tree1Root = Hex.encodeHexString(mTree1.getRootHash());
        String tree2Root = Hex.encodeHexString(mTree2.getRootHash());
        assertTrue(tree1Root.compareTo(tree2Root) == 0);
    }

    @Test
    public void testEmptyDirectory() {
        Merkle mTree = new Merkle();
        mTree.addTracking(emptyDirPath, false);
        assertTrue(mTree.makeTree() != null);
    }
    
    @Test
    public void testTrackSpecificFile() throws IOException {
    	File tempFile = new File(emptyDirPath.getAbsolutePath() + "/tempFile");
    	FileUtils.write(tempFile, "temp data");
    	Merkle mTree = new Merkle();
    	mTree.addTracking(tempFile, false);
    	mTree.makeTree();
    	assertTrue(mTree.getRootHash() != null);
    }
    
    @Test
    public void testTrackMultipleFiles() throws IOException {
    	File tempFile = new File(emptyDirPath.getAbsolutePath() + "/tempFile");
    	File tempFile2 = new File(emptyDirPath.getAbsolutePath() + "/tempFile2");
    	FileUtils.write(tempFile, "temp data");
    	FileUtils.write(tempFile2, "temp data 2");
    	Merkle mTree = new Merkle();
    	Merkle mTreeNoTemp2 = new Merkle();
    	mTree.addTracking(tempFile, false);
    	mTree.addTracking(tempFile2, false);
    	mTreeNoTemp2.addTracking(tempFile, false);
    	assertTrue(!Hex.encodeHex(mTree.getRootHash()).equals(Hex.encodeHex(mTreeNoTemp2.getRootHash())));
    }
    
    @Test
    public void testNoDuplicateTrack() throws IOException {
    	File tempFile = new File(emptyDirPath.getAbsolutePath() + "/tempFile");
    	File tempFile2 = new File(emptyDirPath.getAbsolutePath() + "/tempFile2");
    	FileUtils.write(tempFile, "temp data");
    	FileUtils.write(tempFile2, "temp data 2");
    	Merkle mTree = new Merkle();
    	mTree.addTracking(tempFile, false);
    	mTree.addTracking(emptyDirPath, false);
    	assertEquals(0, mTree.getTrackedFiles().size());
    	assertEquals(1, mTree.getTrackedDirs().size());
    }
    
    @Test
    public void testNoDuplicateTrackRecursive() {
    	File testLevel2 = new File(MerkleTest.class.getResource(RECURSIVEDIR2).getPath() + "/testLevel1/testLevel2");
    	Merkle mTree = new Merkle();
    	mTree.addTracking(testLevel2, false);
    	mTree.addTracking(recursiveDir2Path, true);
    	assertEquals(1, mTree.getTrackedDirs().size());
    }
    
    @Test
    public void testMultipleDirTracking() {
    	Merkle mTree = new Merkle();
    	mTree.addTracking(completeDirPath, false);
    	mTree.addTracking(incompleteDirPath, false);
    	mTree.makeTree();
    	assertEquals(22, mTree.getNumLeaves());
    	assertEquals(5, mTree.getHeight());
    }
    
    @Test
    public void testRemoveTracking() throws IOException {
    	Merkle mTree = new Merkle();
    	File tempFile = new File(emptyDirPath.getAbsolutePath() + "/tempFile");
    	File tempFile2 = new File(emptyDirPath.getAbsolutePath() + "/tempFile2");
    	FileUtils.write(tempFile, "temp data");
    	FileUtils.write(tempFile2, "temp data 2");
    	mTree.addTracking(tempFile, false);
    	mTree.addTracking(tempFile2, false);
    	String twoFileRoot = Hex.encodeHexString(mTree.makeTree());
    	mTree.removeTracking(tempFile2);
    	String oneFileRoot = Hex.encodeHexString(mTree.makeTree());
    	assertNotEquals(null, oneFileRoot, twoFileRoot);
    }
}
