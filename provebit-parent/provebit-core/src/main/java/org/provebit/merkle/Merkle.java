package org.provebit.merkle;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

/**
 * This class constructs a Merkle tree from files within a given directory
 * @author Daniel Boehm, Matthew Knox
 * @organization ProveBit
 * @version 0.1
 * 
 * @TODO: Add method to flip the endian-ness of the hashes (to conform to Bitcoin)
 * @TODO: Add saving/loading trees from files
 * @TODO: Figure out fancy indexing scheme for last two levels so tree doesn't have
 *        to be allocated as if it were complete
 *        Current allocation wastes O(N) tree nodes
 */
public class Merkle {
    private byte[][] tree;
    private int height; // Root is level 0
    private int numLeaves;
    private int totalNodes;
    private boolean exists;
    private List<File> trackedFiles; // List of files being tracked
    private Map<File, Boolean> trackedDirectories; // List of directories being tracked and whether
    											   // or not they are recursively tracked

    /**
     * Default constructor
     */
    public Merkle() {
    	trackedFiles = new ArrayList<File>();
    	trackedDirectories = new HashMap<File, Boolean>();
        tree = null;
        exists = false;
    }
    
    /**
     * Add a file or directory to the list of tracked files in this tree
     * @param file - File/Directory to track
     * @param recursive - If file is a directory, specify whether or not the directory should be recursively searched
     */
    public void addTracking(File file, boolean recursive) {
    	if (file.isDirectory()) {
    		trackedDirectories.put(file, recursive);
    	} else {
    		if (!trackedFiles.contains(file)) {
        		trackedFiles.add(file);
    		}
    	}
    }
    
    /**
     * Remove file/directory from tracking
     * @param file - file/directory to remove from tracking
     */
    public void removeTracking(File file) {
    	if (file.isDirectory()) {
    		trackedDirectories.remove(file);
    	} else {
    		trackedFiles.remove(file);
    	}
    }

    /**
     * Make the merkle tree from the files in the specified directory and
     * return the root hash after construction
     * @return byte[] root hash of constructed tree
     */
    public byte[] makeTree() {
        List<File> fileList = getFiles();
        if (fileList.size() == 0) {
            numLeaves = 0;
            tree = null;
            return getRootHash();
        }
        
        List<byte[]> hashList = getFilesHashes(fileList);
        if (hashList.size() % 2 == 1) { // If odd number of hashes, duplicate last
            hashList.add(hashList.get(hashList.size()-1));
        }
        numLeaves = hashList.size();
        
        allocateTree();
        makeLeaves(hashList);
        try {
            makeInternalNodes();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        exists = true;
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

    /**
     * Get the top level hash of the merkle tree
     * @return byte[] of top level hash, or all 0's if emtry tree
     */
    public byte[] getRootHash() {
    	byte[] root = null;
        try {
			root = (tree != null) ? tree[0] : Hex.decodeHex("0000000000000000000000000000000000000000000000000000000000000000".toCharArray());
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
        return root;
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
     * Get the list of directories being tracked
     * @return List<File> of directories being tracked, may be empty
     */
    public List<File> getTrackedDirs() {
        List<File> directoryList = new ArrayList<File>();
        directoryList.addAll(trackedDirectories.keySet());
        return directoryList;
    }
    
    /**
     * Get the list of files being tracked
     * @return List<File> of specific files being tracked, may be empty
     */
    public List<File> getTrackedFiles() {
    	return trackedFiles;
    }
    
    /**
     * Returns whether or not a tree is currently being stored
     * @return - true if a tree has been created, false otherwise
     */
    public boolean exists() {
    	return exists;
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
     * Wrapper that gets all files being tracked in all tracked directories
     * @return List<File> of all files in tracking
     */
    private List<File> getFiles() {
    	List<File> fileList = new ArrayList<File>();
        fileList.addAll(trackedFiles);
        for (File directory : trackedDirectories.keySet()) {
        	fileList.addAll(getFiles(directory, trackedDirectories.get(directory)));
        }
        return fileList;
    }
    
    /**
     * Check to see whether the specific file is being tracked by the tree
     * @param file - File to check for tracking
     * @return true if file is being tracked, false o/w
     */
    public Boolean isTracking(File file) {
    	if (trackedFiles.contains(file) || trackedDirectories.containsKey(file) || isTrackedRecursively(file)) {
    		System.out.println(file + " is tracked");
    		return true;
    	}
    	System.out.println(file + " not tracked");
    	return false;
    }
    
    /**
     * A file is being tracked recursively if it is contained within a directory
     * that is being recursively tracked
     * 
     * This helper method checks for that condition
     * @param file - File to check for recursive tracking
     * @return true if file is recursively tracked, false o/w
     */
    private Boolean isTrackedRecursively(File file) {
    	for (File directory : trackedDirectories.keySet()) {
    		try {
				if ((!file.isDirectory() && file.getParentFile().equals(directory)) || 
					(trackedDirectories.get(directory) && FileUtils.directoryContains(directory, file))) {
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
    	}
    	return false;
    }
    
    /**
     * Check to see whether directory is being recursively tracked
     * @param dir -Tracked directory 
     * @return true if directory is recursively tracked, false if not a directory or not recursively tracked
     */
    public Boolean isTrackingRecursive(File dir) {
    	return (dir.isDirectory() && trackedDirectories.get(dir));
    }

    /**
     * Get list of all files in directory, recursively if need be
     * @param directory - Directory to check for files
     * @param recursive - true if sub-directories should be searched, false o/w
     * @return List<File> of all files in directory
     */
    private List<File> getFiles(File directory, boolean recursive) {
        List<File> fileList = new ArrayList<File>();
        for (File file : directory.listFiles()) {
            if (!file.isDirectory()) {
                fileList.add(file);
            } else if (recursive){
                fileList.addAll(getFiles(file, recursive));
            }

        }
        return fileList;
    }

    /**
     * Get SHA-256 hash of each file in argument list
     * @param fileList - ArrayList<File> of files to get hashes of
     * @return ArrayList<byte[]> of corresponding SHA-256 file hashes or null if error occurred
     */
    private List<byte[]> getFilesHashes(List<File> fileList) {
        List<byte[]> hashList = new ArrayList<byte[]>();
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

    /**
     * Minimal custom comparator subclass for sorting hashes
     */
    private class FileHashComparator implements Comparator<byte[]> {
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
        // At this time I cannot determine a closed form for this sum
        int curr = numLeaves;
        int total = numLeaves;
        while (curr != 1) {
            curr = (curr/2);
            if (curr % 2 != 0) {
                if (curr != 1) {
                    curr += 1;
                }
            }
            total += curr;
        }
        return total;
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
     * Wrapper that builds each level of the tree (except last)
     * @throws NoSuchAlgorithmException 
     */
    private void makeInternalNodes() throws NoSuchAlgorithmException {
        int level = height-1; // Leaf level built already
        for (; level >= 0; level--) {
            buildLevel(level);
        }
    }
    
    /**
     * Helper function to build internal nodes of tree ensuring there are
     * an even number of nodes on each level
     * @param level - Level to build nodes of
     * @throws NoSuchAlgorithmException 
     */
    private void buildLevel(int level) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        int nodeIndex = (int) Math.pow(2, level) - 1; // Leftmost node at level 'level'
        int lastNodeIndex = (int) Math.pow(2, level+1) - 2; // Last (rightmost) node at level 'level'
        int nodesBuilt = 0;
        for (; nodeIndex <= lastNodeIndex; nodeIndex++, nodesBuilt++) {
            md.reset(); 
            byte[] leftChildHash = tree[getLeftChild(nodeIndex)];
            byte[] rightChildHash = tree[getRightChild(nodeIndex)];
            if (leftChildHash == null) { // Implies right child is also null by merkle construction
                if (nodesBuilt % 2 != 0) { // Odd number of nodes on this level, copy left sibling
                    tree[nodeIndex] = tree[nodeIndex-1];
                    return; // Reached last non empty node at this level, we are done
                }
            }
            byte[] newHash = concatHashes(leftChildHash, rightChildHash);
            md.update(newHash);
            tree[nodeIndex] = md.digest();
        }
    }

    /**
     * Concatenates argument hashes L + R and returns result
     * @param leftHash
     * @param rightHash
     * @return leftHash + rightHash
     */
    private byte[] concatHashes(byte[] leftHash, byte[] rightHash) {
        byte[] newHash = new byte[leftHash.length + rightHash.length];
        System.arraycopy(leftHash, 0, newHash, 0, leftHash.length);
        System.arraycopy(rightHash, 0, newHash, leftHash.length, rightHash.length);
        return newHash;
    }

    /**
     * Helper function that makes the leaf nodes of the tree
     * @param hashList - Hashes of files that make up the leaf nodes
     */
    private void makeLeaves(List<byte[]> hashList) {
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
