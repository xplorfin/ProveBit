package org.provebit.daemon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.provebit.daemon.DaemonProtocol.DaemonMessage.DaemonMessageType;
import org.provebit.merkle.Merkle;
import org.simplesockets.server.SimpleServer;

public class MerkleDaemon extends Thread {
	private int maxPort = 65535, minPort = 1024;
	private int period;
	private List<FileAlterationObserver> observers;
	private FileMonitor listener;
	private SimpleServer server;

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
		listener = new FileMonitor(mTree);
		this.period = period;
		setDaemon(false);
		setName("MerkleDaemon");
		int serverPort = minPort + (int)(Math.random() * ((maxPort - minPort) + 1));
		DaemonProtocol protocol = setupProtocol();
		server = new SimpleServer(serverPort, protocol);
	}
	
	private DaemonProtocol setupProtocol() {
		return new DaemonProtocol() {
			@Override
			public DaemonMessage<?> handleMessage(DaemonMessage<?> request) {
				DaemonMessage<String> reply = null;
				switch(request.type) {
					case START:
						// Is this needed?
						break;
					case STOP:
						Thread.currentThread().interrupt();
						break;
					case ADDFILES:
						// Extract list of file strings, convert to file objects, pass to merkle
						break;
					case REMOVEFILES:
						// Extract list of file strings, convert to file objects, pass to merkle
						break;
					case SETPERIOD:
						period = (int) request.data;
						break;
					case GETLOG:
						reply = new DaemonMessage<String>(DaemonMessageType.REPLY, getLog());
						break;
					case REPLY:
						// Ignore
						break;
					default:
						break;
				}
				return reply;
			}
			
		};
	}

	/**
	 * Initializes the observer and starts monitoring the directory
	 */
	public void run() {
		if (Thread.currentThread().isDaemon()) {
			return;
		}
		
		createObservers();
		
		for (FileAlterationObserver observer : observers) {
			observer.addListener(listener);
		}

		try {
			for (FileAlterationObserver observer : observers) {
				observer.initialize();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		server.startServer();
		monitorDirectory();
	}

	private void createObservers() {
		for (File directory : listener.getTree().getTrackedDirs()) {
			if (listener.getTree().isDirRecursive(directory)) {
				observers.add(new FileAlterationObserver(directory));
			} else {
				IOFileFilter filter = FileFileFilter.FILE;
				for (File file : directory.listFiles()) {
					if (!file.isDirectory()) {
						filter = FileFilterUtils.or(new NameFileFilter(file.getName()), filter);
					}
				}
				observers.add(new FileAlterationObserver(directory, filter));
			}
		}
		
		Map<File, List<File>> uniqueFilesDirs = new HashMap<File, List<File>>();
		for (File file : listener.getTree().getTrackedFiles()) {
			File parentKey = file.getParentFile();
			if (!uniqueFilesDirs.containsKey(parentKey)) {
				uniqueFilesDirs.put(parentKey, new ArrayList<File>());
			}
			uniqueFilesDirs.get(parentKey).add(file);
		}
		
		for (File uniqueFileDirectory : uniqueFilesDirs.keySet()) {
			List<File> files = uniqueFilesDirs.get(uniqueFileDirectory);
			IOFileFilter filter = FileFileFilter.FILE;
			for (File file : files) {
				filter = FileFilterUtils.or(new NameFileFilter(file.getName()), filter);
			}
			observers.add(new FileAlterationObserver(uniqueFileDirectory, filter));
		}
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
			listener.log.addEntry("Daemon interrupted, exiting...");
			try {
				for (FileAlterationObserver observer : observers) {
					observer.destroy();
				}
			} catch (Exception e) {
				listener.log.addEntry("Observer destruction failed, messy exit...");
				e.printStackTrace();
			}
		} finally {
			// Future cleanup
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
	 * Get number of events detected since daemon launched
	 * 
	 * @return number of events since launch
	 */
	public int getEvents() {
		return listener.getNumEvents();
	}

	public String getLog() {
		return listener.getLogEntries();
	}
	
	public Log getLogActual() {
		return listener.log;
	}
	
	/**
	 * Changes the refresh period
	 * @param newPeriod - New refresh period to use
	 */
	public void updatePeriod(int newPeriod) {
		period = newPeriod;
	}
}
