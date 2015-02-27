package org.provebit.daemon;

import java.io.File;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.provebit.merkle.Merkle;

public class DirectoryMonitor implements FileAlterationListener {
	public enum LogVerbosity{NONE, LOW, HIGH};
	private Merkle tree;
	private int changes;
	private LogVerbosity logLevel;
	private Log log;
	
	/**
	 * Constructor, configures the tree that will represent the argument file/directory
	 * @param dir - directory to watch
	 * @param recursive - whether or not to enable recursive directory changes
	 */
	public DirectoryMonitor(Merkle mTree) {
		tree = mTree;
		changes = 0;
		logLevel = LogVerbosity.NONE;
	}
	
	public void setLogLevel(LogVerbosity level) {
		logLevel = level;
	}
	
	@Override
	public void onStart(FileAlterationObserver observer) {
		if (!tree.exists()) {
			tree.makeTree();
		}
	}

	@Override
	public void onDirectoryCreate(File directory) {
		System.out.println("New directory: " + directory.getPath() + " created");
		updateTree(directory);
	}

	@Override
	public void onDirectoryChange(File directory) {
		System.out.println("Directory: " + directory.getPath() + " changed");
		updateTree(directory);
	}

	@Override
	public void onDirectoryDelete(File directory) {
		System.out.println("Directory: " + directory.getPath() + " deleted");
		updateTree(directory);
	}

	@Override
	public void onFileCreate(File file) {
		System.out.println("File: " + file.getPath() + " created");
		updateTree(file);
	}

	@Override
	public void onFileChange(File file) {
		System.out.println("File " + file.getPath() + " changed");
		updateTree(file);
	}

	@Override
	public void onFileDelete(File file) {
		System.out.println("File: " + file.getPath() + " deleted");
		updateTree(file);
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		// Do nothing
	}
	
	/**
     * Get the current merkle tree
     * @return Currently constructed merkle tree
     */
    public Merkle getTree() {
        return tree;
    }
    
    /**
     * Get number of changes detected since daemon launched
     * @return number of changes since launch
     */
    public int getChanges() {
        return changes;
    }

    /**
     * Update wrapper that makes sure the detected change is relevant before
     * spending the time to reconstruct the merkle tree
     */
	private void updateTree(File file) {
		if (tree.isRecursive() || file.getParent().compareTo(tree.getDir().getAbsolutePath()) == 0) {
			reconstructTree();
		}
	}
	
	/**
	 * Reconstructs the merkle tree and increments the change counter
	 */
	private void reconstructTree() {
		changes++;
		System.out.println("Old root: " + Hex.encodeHexString(tree.getRootHash()));
		tree.makeTree();
		System.out.println("New root: " + Hex.encodeHexString(tree.getRootHash()));
	}
}
