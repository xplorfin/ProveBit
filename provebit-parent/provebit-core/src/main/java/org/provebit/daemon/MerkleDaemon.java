package org.provebit.daemon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.monitor.FileAlterationObserver;
import org.provebit.merkle.Merkle;

public class MerkleDaemon extends Thread {
	private int period;
	private List<FileAlterationObserver> observers;
	private DirectoryMonitor listener;

	/**
	 * Daemon constructor,
	 * 
	 * @param dir
	 *            - Directory to run daemon on
	 * @param period
	 *            - Daemon polling period (msec)
	 */
	public MerkleDaemon(Merkle mTree, int period) {
		observers = new ArrayList<FileAlterationObserver>();
		for (File directory : mTree.getTrackedDirs()) {
			observers.add(new FileAlterationObserver(directory.getAbsolutePath()));
		}
		for (File file : mTree.getTrackedFiles()) {
			observers.add(new FileAlterationObserver(file.getAbsolutePath()));
		}
		listener = new DirectoryMonitor(mTree);
		
		for (FileAlterationObserver observer : observers) {
			observer.addListener(listener);
		}
		this.period = period;
		setDaemon(false);
		setName("MerkleDaemon");
	}

	/**
	 * Initializes the observer and starts monitoring the directory
	 */
	public void run() {
		if (Thread.currentThread().isDaemon()) {
			return;
		}

		try {
			for (FileAlterationObserver observer : observers) {
				observer.initialize();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		monitorDirectory();
	}

	/**
	 * Main periodic method that checks for modifications in the desired
	 * directory
	 */
	private void monitorDirectory() {
		try {
			while (true) {
				for (FileAlterationObserver observer : observers) {
					observer.checkAndNotify();
				}
				Thread.sleep(period);
			}
		} catch (InterruptedException ie) {
			System.out.println("Monitor interrupted, exiting...");
			try {
				for (FileAlterationObserver observer : observers) {
					observer.destroy();
				}
			} catch (Exception e) {
				System.out
						.println("Observer destruction failed, messy exit...");
				e.printStackTrace();
			}
		} finally {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Get the current merkle tree
	 * 
	 * @return Currently constructed merkle tree
	 */
	public Merkle getTree() {
		return listener.getTree();
	}

	/**
	 * Get number of changes detected since daemon launched
	 * 
	 * @return number of changes since launch
	 */
	public int getChanges() {
		return listener.getChanges();
	}

	public Log getLog() {
		return listener.log;
	}
}
