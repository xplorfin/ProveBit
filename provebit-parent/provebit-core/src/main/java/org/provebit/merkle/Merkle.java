package org.provebit.merkle;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.provebit.merkle.MerkleUtils.FileHashComparator;

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
    private List<byte[]> recentLeaves; // -Sorted- list of leaves in most recent tree construction

    /**
     * Default constructor
     */
    public Merkle() {
    	trackedFiles = new ArrayList<File>();
    	trackedDirectories = new HashMap<File, Boolean>();
    	recentLeaves = new ArrayList<byte[]>();
        tree = null;
        exists = false;
    }
    
    /**
     * Add a file or directory to the list of tracked files in this tree if the file is not already tracked
     * @param file - File/Directory to track
     * @param recursive - If file is a directory, specify whether or not the directory should be recursively searched
     */
    public void addTracking(File file, boolean recursive) {
    	if (!file.isDirectory() && !isTracking(file)) {
    		trackedFiles.add(file);
    	} else if ((file.isDirectory() && !isTracking(file)) || trackedDirectories.containsKey(file)) {
    		trackedDirectories.put(file, recursive);
    		// If we add a directory, we want to make sure that any files that were in that directory but are also
    		// in the trackedFiles list are removed from the trackedFiles list since they are duplicated by the coverage
    		// provided by the trackedDirectories list
    		List<File> duplicates = new ArrayList<File>();
    		for (File existingFile : trackedFiles) {
    			if (MerkleUtils.isTrackedRecursively(existingFile, trackedDirectories)) {
    				duplicates.add(existingFile);
    			}
    		}
    		trackedFiles.removeAll(duplicates);
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
        List<File> fileList = MerkleUtils.getFiles(trackedFiles, trackedDirectories);
        if (fileList.size() == 0) {
            numLeaves = 0;
            tree = null;
            return getRootHash();
        }
        
        List<byte[]> hashList = MerkleUtils.getFilesHashes(fileList);
        if (hashList.size() % 2 == 1) { // If odd number of hashes, duplicate last
            hashList.add(hashList.get(hashList.size()-1));
        }
        numLeaves = hashList.size();
        recentLeaves = hashList;
        
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
     * Get total number of tracked discrete files and directories
     * @return total discrete files + total directories
     */
    public int getNumTracked() {
    	return trackedFiles.size() + trackedDirectories.keySet().size();
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
			e.printStackTrace();
		}
 
        return root;
    }
    
    public boolean existsAsLeaf(byte[] hash) {
    	int index = Collections.binarySearch(recentLeaves, hash, new FileHashComparator());
    	return (index >= 0) ? true : false;
    }
    
    public List<byte[]> getLeaves() {
    	return recentLeaves;
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
     * Check to see whether the specific file is being tracked by the tree
     * @param file - File to check for tracking
     * @return true if file is being tracked, false o/w
     */
    public Boolean isTracking(File file) {
    	if (trackedFiles.contains(file) || trackedDirectories.containsKey(file) || MerkleUtils.isTrackedRecursively(file, trackedDirectories)) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Check to see whether directory is being recursively tracked
     * @param dir -Tracked directory 
     * @return true if directory is recursively tracked, false if not a directory or not recursively tracked
     */
    public Boolean isDirRecursive(File dir) {
    	return (dir.isDirectory() && trackedDirectories.get(dir));
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
                    nodesBuilt++;
                }
                return; // Reached last non empty node at this level, we are done
            }
            byte[] newHash = MerkleUtils.concatHashes(leftChildHash, rightChildHash);
            md.update(newHash);
            tree[nodeIndex] = md.digest();
        }
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
