package org.provebit.merkle;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

public class Merkle {
	private File dir;
	private byte[][] tree;
	private int height; // Root is level 0
	private int numLeaves;
	private int totalNodes;
	
	/**
	 * Constructor
	 * @param directoryPath - String path to directory to build tree from
	 */
	public Merkle(String directoryPath) {
		dir = new File(directoryPath);
		tree = null;
	}
	
	/**
	 * Make the merkle tree from the files in the specified directory and
	 * return the root hash after construction
	 * @return byte[] root hash of constructed tree
	 */
	public byte[] makeTree() {
		ArrayList<File> fileList = getFiles();
		ArrayList<byte[]> hashList = getFilesHashes(fileList);
		numLeaves = (fileList.size() % 2 == 0) ? fileList.size() : fileList.size() + 1;
		allocateTree();
		
		makeLeaves(hashList);
		try {
			makeInternalNodes();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return getRootHash();
	}
	
	/**
	 * Get height of tree, root is level 0
	 * @return height of tree
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Get number of non empty leaves in tree
	 * @return number of leaves
	 */
	public int getNumLeaves() {
		return numLeaves;
	}
	
	public byte[] getRootHash() {
		return (tree != null) ? tree[0] : null;
	}
	
	/**
	 * Get the total number of nodes in the merkle tree
	 * @return number of nodes in the tree
	 */
	public int getTreeSize() {
		return totalNodes;
	}
	
	/**
	 * Get entire representation of the current tree
	 * @return ArrayList<byte[]> representation of tree, root is ?? element
	 */
	public byte[][] getTree() {
		return tree;
	}
	
	/**
	 * Computes log base 2 of the argument value
	 * @param value - double to compute log_2 of
	 * @return log_2(value)
	 */
	private double logBase2(double value) {
		return (Math.log(value)/Math.log(2.0));
	}
	
	/**
	 * Get list of all files in directory
	 * 
	 * @TODO This list needs to be sorted in an exact way, since the tree will be
	 * 		 built from the list and the order of the contatenations matters
	 * @TODO Add recursive directory exploration if desired
	 * @return ArrayList of files in the directory
	 */
	private ArrayList<File> getFiles() {
		ArrayList<File> fileList = new ArrayList<File>();
		for (File file : dir.listFiles()) {
			if (!file.isDirectory()) { // Will need to change when we start recursing into directories
				fileList.add(file);
			}
		}
		return fileList;
	}
	
	/**
	 * Get SHA-256 hash of each file in argument list
	 * @param fileList - ArrayList<File> of files to get hashes of
	 * @return ArrayList<byte[]> of corresponding SHA-256 file hashes or null if error occurred
	 */
	private ArrayList<byte[]> getFilesHashes(ArrayList<File> fileList) {
		ArrayList<byte[]> hashList = new ArrayList<byte[]>();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
			for (File file : fileList) {
				md.reset();
				byte[] fileBytes = FileUtils.readFileToByteArray(file);
				md.update(fileBytes);
				hashList.add(md.digest());
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			return null;
		}
		
		Collections.sort(hashList, new FileHashComparator());
		return hashList;
	}
	
	protected class FileHashComparator implements Comparator<byte[] >{
		public int compare(byte[] hash1, byte[] hash2) {
			String hash1Hex = Hex.encodeHexString(hash1);
			String hash2Hex = Hex.encodeHexString(hash2);
			return hash1Hex.compareTo(hash2Hex);
		}
	}
	
	
	/**
	 * Calculates the total number of actual nodes in the tree
	 * @return number of nodes in the tree
	 */
	private int getNumNodes() {
		if (numLeaves == 0) {
			return 0;
		}
		else if (numLeaves == 2 || numLeaves == 1) {
			return 3;
		}
	
		return (int) (Math.pow(2, height-1) - 1.0 + numLeaves + Math.ceil((double)numLeaves / 2.0));
	}
	
	private int getParent(int index) {
		return (index-1)/2;
	}
	
	private int getLeftChild(int index) {
		return 2*(index) + 1;
	}
	
	private int getRightChild(int index) {
		return 2*(index+1);
	}
	
	/**
	 * Helper function that makes the internal nodes of the tree
	 * @throws NoSuchAlgorithmException 
	 */
	private void makeInternalNodes() throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		int i = (int) Math.pow(2, height) - 2; // Rightmost node on 2nd to last level
		for (; i >= 0; i--) {
			md.reset();
			byte[] leftHash = tree[getLeftChild(i)];
			byte[] rightHash = tree[getRightChild(i)];
			if (leftHash == null && rightHash == null) { // 'Empty' node
				tree[i] = null;
			} else if (leftHash != null && rightHash == null) { // Partial node
				tree[i] = leftHash;
			} else if (leftHash != null && rightHash != null) { // H(left + right)
				byte[] newHash = new byte[leftHash.length + rightHash.length];
				System.arraycopy(leftHash, 0, newHash, 0, leftHash.length);
				System.arraycopy(rightHash, 0, newHash, leftHash.length, rightHash.length);
				md.update(newHash);
				tree[i] = md.digest();
			} else { // Tree construction broken
				throw new RuntimeException("Tree construction failed");
			}
		}
	}

	/**
	 * Helper function that makes the leaf nodes of the tree
	 * @param hashList - Hashes of files that make up the leaf nodes
	 */
	private void makeLeaves(ArrayList<byte[]> hashList) {
		int i = (int) Math.pow(2, height) - 1; // Leftmost leaf node
		for (byte[] hash : hashList) {
			tree[i] = hash;
			i++;
		}
	}
	
	/**
	 * Helper function that allocates space for tree and initializes
	 * all elements to null
	 */
	private void allocateTree() {
		height = (int) Math.ceil(logBase2(numLeaves));
		totalNodes = getNumNodes();
		tree = new byte[(int) (Math.pow(2, height+1) - 1)][]; // Allocate space as if tree is complete (easier representation)
		for (int i = 0; i < tree.length; i++) {
			tree[i] = null;
		}
	}
}
