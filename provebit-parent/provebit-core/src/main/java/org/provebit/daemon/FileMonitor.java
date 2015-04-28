package org.provebit.daemon;

import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.provebit.merkle.FileMerkle;
import org.provebit.merkle.HashType;
import org.provebit.merkle.SerialMerkleUtils;
import org.provebit.utils.ApplicationDirectory;
import org.provebit.utils.Log;

public class FileMonitor implements FileAlterationListener {
	public enum MonitorEvent {
		FCREATE, FDELETE, FCHANGE, DCREATE, DDELETE, DCHANGE
	};

	private FileMerkle tree;
	private int events;
	protected Log log;
	private File logFile, merkleFile;
	private boolean allowRecovery;

	/**
	 * Initializes file monitor with a specific merkle tree
	 * Sets up logging instance that attempts to log events to local file
	 * @throws IOException 
	 */
	public FileMonitor(FileMerkle mTree) {
		tree = mTree;
		events = 0;
		logFile = new File(ApplicationDirectory.INSTANCE.getRoot(), "daemon.log");
		merkleFile = new File(ApplicationDirectory.INSTANCE.getRoot(), "daemon.merkle");
		log = new Log();
		
		FileUtils.deleteQuietly(logFile); // If an old log already exists, delete it
		try {
			log = new Log(logFile);
		} catch (IOException e) {
			System.err.println("Failed to create log file, only logging internally");
			e.printStackTrace();
			log = new Log();
			logFile = null;
		}
	}

	/**
	 * Initializes file monitor that will attempt to recover existing merkle tree
	 * and log files if the exist, or create new ones if recovery failes
	 * 
	 * Sets up logging instance that attempts to log events to local file
	 * @throws IOException 
	 */
	public FileMonitor(boolean recover) {
		allowRecovery = recover;
		events = 0;
		logFile = new File(ApplicationDirectory.INSTANCE.getRoot(), "daemon.log");
		merkleFile = new File(ApplicationDirectory.INSTANCE.getRoot(), "daemon.merkle");
		if (allowRecovery) {
			recoverLog();
			recoverTree();
		} else {
			tree = new FileMerkle(HashType.SHA256);
			FileUtils.deleteQuietly(logFile); // If an old log already exists, delete it
			try {
				log = new Log(logFile);
			} catch (IOException e) {
				log = new Log();
				System.err.println("Failed to create log file, only logging internally");
				logFile = null;
				e.printStackTrace();
			}
		}
		
		setShutdownHook();
	}
	
	/**
	 * If running in recovery mode set up shutdown hook that will dump
	 * merkle tree and log to the application directory in the event that the
	 * daemon is closed, allows daemon to recover and pickup where it left off
	 * when it was shutdown on next start
	 */
	private void setShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				save();
			}
		});
	}
	
	/**
	 * Force save all state to disk
	 */
	public void save() {
		System.out.println("Monitor saving state");
		log.setLogFile(logFile);
		log.writeToFile();
		SerialMerkleUtils.writeToFile(tree, merkleFile);
	}
	
	/**
	 * Recover an existing daemon log file if it exists, if it doesn't
	 * create a new log and set it up to dump to the application directory
	 */
	private void recoverLog() {
		if (logFile.exists()) {
			log = new Log();
			log.recoverLog(logFile);
			log.addEntry("Recovered previous log");
		} else {
			try {
				log = new Log(logFile);
				log.addEntry("No previous log found, creating new log");
			} catch (IOException e) {
				e.printStackTrace();
				log = new Log();
				logFile = null;
				log.addEntry("Failed to recover log");
			}
		}
	}
	
	/**
	 * Recover existing merkle tree if it exists, if it doesn't then create
	 * a new tree
	 */
	private void recoverTree() {
		if (merkleFile.exists()) {
			tree = SerialMerkleUtils.readFileMerkleFromFile(merkleFile);
			log.addEntry("Recovered previous merkle tree");
		} else {
			tree = new FileMerkle(HashType.SHA256);
			log.addEntry("No previous merkle tree found, creating new tree");
		}
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
		log.writeToFile();
	}

	/**
	 * Get the current merkle tree
	 * 
	 * @return Currently constructed merkle tree
	 */
	public FileMerkle getTree() {
		return tree;
	}

	/**
	 * Get number of events detected since daemon launched
	 * 
	 * @return number of events since launch
	 */
	public int getNumEvents() {
		return events;
	}

	/**
	 * Update wrapper that makes sure the detected change is relevant before
	 * spending the time to reconstruct the merkle tree
	 */
	private void updateTree(MonitorEvent event, File file) {
		if (event == MonitorEvent.DDELETE || event == MonitorEvent.DCREATE || event == MonitorEvent.DCHANGE) { // Directories aren't part of merkle tree
			events++;
			logEvent(event, file);
		} else if (tree.isTracking(file) || tree.isTracking(file.getParentFile())) {
			events++;
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
		String oldRoot = Hex.encodeHexString(tree.getRootHash());
		tree.makeTree();
		String newRoot = Hex.encodeHexString(tree.getRootHash());
		log.addEntry("Hash update: " + oldRoot + " -> " + newRoot);
	}
	
	/**
	 * Get readable string representation of all log entries
	 * @return String of log entries
	 */
	public String getLogEntries() {
		return log.toString();
	}
}
