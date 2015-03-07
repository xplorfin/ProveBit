package org.provebit.daemon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.provebit.merkle.Merkle;

public class FileMonitor implements FileAlterationListener {
	public enum MonitorEvent {
		FCREATE, FDELETE, FCHANGE, DCREATE, DDELETE, DCHANGE
	};

	private Merkle tree;
	private int changes;
	protected Log log;

	/**
	 * Initializes file monitor
	 */
	public FileMonitor(Merkle mTree) {
		tree = mTree;
		changes = 0;
		log = new Log();
	}

	@Override
	public void onStart(FileAlterationObserver observer) {
		// Do nothing
	}

	@Override
	public void onDirectoryCreate(File directory) {
		updateTree(MonitorEvent.DCREATE, directory);
	}

	@Override
	public void onDirectoryChange(File directory) {
		updateTree(MonitorEvent.DCHANGE, directory);
	}

	@Override
	public void onDirectoryDelete(File directory) {
		tree.removeTracking(directory);
		updateTree(MonitorEvent.DDELETE, directory);
	}

	@Override
	public void onFileCreate(File file) {
		updateTree(MonitorEvent.FCREATE, file);
	}

	@Override
	public void onFileChange(File file) {
		updateTree(MonitorEvent.FCHANGE, file);
	}

	@Override
	public void onFileDelete(File file) {
		tree.removeTracking(file);
		updateTree(MonitorEvent.FDELETE, file);
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		// Do nothing
	}

	/**
	 * Get the current merkle tree
	 * 
	 * @return Currently constructed merkle tree
	 */
	public Merkle getTree() {
		return tree;
	}

	/**
	 * Get number of changes detected since daemon launched
	 * 
	 * @return number of changes since launch
	 */
	public int getChanges() {
		return changes;
	}

	/**
	 * Update wrapper that makes sure the detected change is relevant before
	 * spending the time to reconstruct the merkle tree
	 */
	private void updateTree(MonitorEvent event, File file) {
		if (tree.isTracking(file) || tree.isTracking(file.getParentFile())) {
			logEvent(event, file);
			reconstructTree();
		}
	}

	/**
	 * Helper function that adds the event to the local log
	 * 
	 * @param event
	 *            - Event that occurred
	 * @param file
	 *            - File related to event
	 */
	private void logEvent(MonitorEvent event, File file) {
		log.addEntry(event.toString() + " : " + file.getAbsolutePath());
	}

	/**
	 * Reconstructs the merkle tree and increments the change counter
	 */
	private void reconstructTree() {
		changes++;
		System.out.println("Old root: "
				+ Hex.encodeHexString(tree.getRootHash()));
		tree.makeTree();
		System.out.println("New root: "
				+ Hex.encodeHexString(tree.getRootHash()));
	}
}
