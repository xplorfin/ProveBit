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
	private enum DaemonStatus {ACTIVE, SUSPENDED};
	private int maxPort = 65535, minPort = 1024;
	private int period;
	private List<FileAlterationObserver> observers;
	private FileMonitor listener;
	private SimpleServer server;
	private Merkle mTree;
	private DaemonStatus state;

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
		this.mTree = mTree;
		setDaemon(false);
		setName("MerkleDaemon");
		int serverPort = minPort + (int)(Math.random() * ((maxPort - minPort) + 1)); // Should this be random or static?
		DaemonProtocol protocol = setupProtocol();
		server = new SimpleServer(serverPort, protocol);
		state = DaemonStatus.SUSPENDED;
	}
	
	/**
	 * Setup the DaemonProtocol
	 * Amounts to defining the message handler method and operations to
	 * perform for each message type
	 * @return DaemonProtocol
	 */
	private DaemonProtocol setupProtocol() {
		return new DaemonProtocol() {
			@SuppressWarnings("unchecked")
			@Override
			public DaemonMessage<?> handleMessage(DaemonMessage<?> request) {
				DaemonMessage<?> reply = null;
				listener.log.addEntry("Network request '" + request.type.toString() + "' received");
				switch(request.type) {
					case START:
						startMonitoring();
						break;
					case SUSPEND:
						suspendMonitoring();
						break;
					case ADDFILES:
						Map<File, Boolean> pathFileMap = getPathFileMap((Map<String, Boolean>) request.data);
						mTree.addAllTracking(pathFileMap);
						break;
					case REMOVEFILES:
						List<File> filesToRemove = pathsToFiles((List<String>) request.data);
						mTree.removeAllTracking(filesToRemove);
						break;
					case SETPERIOD:
						period = (int) request.data;
						break;
					case GETLOG:
						reply = new DaemonMessage<String>(DaemonMessageType.REPLY, getLog());
						break;
					case HEARTBEAT:
						reply = new DaemonMessage<String>(DaemonMessageType.REPLY, null);
						break;
					case GETTRACKED:
						List<List<String>> tracking = new ArrayList<List<String>>();
						tracking.add(new ArrayList<String>());
						tracking.add(new ArrayList<String>());
						for (File file : mTree.getTrackedFiles()) {
							tracking.get(0).add(file.getAbsolutePath());
						}
						for (File file : mTree.getTrackedDirs()) {
							tracking.get(1).add(file.getAbsolutePath());
						}
						reply = new DaemonMessage<List<List<String>>>(DaemonMessageType.REPLY, tracking);
						break;
					case ISTRACKED:
						String filePath = (String) request.data;
						reply = new DaemonMessage<Boolean>(DaemonMessageType.REPLY, mTree.isTracking(new File(filePath)));
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
	 * Simple helper that takes file absolute path strings and turns them into file objects
	 * If the file exists add to a list of file objects
	 * @param filePathStrings - List of absolute file path strings
	 * @return List of files corresponding to the file path strings if the file exists
	 * 
	 */
	private List<File> pathsToFiles(List<String> filePathStrings) {
		List<File> fileList = new ArrayList<File>();
		
		for (String path : filePathStrings) {
			File file = new File(path);
			if (file.exists()) {
				fileList.add(file);
			}
			fileList.add(new File(path));
		}
		
		return fileList;
	}
	
	/**
	 * Takes the Map<String, Boolean> map supplied by the ADDFILES network event
	 * and converts it into the Map<File, Boolean> map that the merkle class expects
	 * @param pathStringMap
	 * @return
	 */
	private Map<File, Boolean> getPathFileMap(Map<String, Boolean> pathStringMap) {
		Map<File, Boolean> addFileMap = new HashMap<File, Boolean>();
		for (String pathString : pathStringMap.keySet()) {
			File file = new File(pathString);
			if (file.exists()) {
				addFileMap.put(file, pathStringMap.get(pathString));
			}
		}
		
		return addFileMap;
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
		/**
		 * After the server has started and has successfully bound a port we need to
		 * write that port to some file in the application directory so the provebit application
		 * knows what port to connect to
		 */
		monitorDirectory();
	}

	/**
	 * Private helper function that creates all the file alteration observers
	 * Attempts to consolidate observer creation as much as possible by using 
	 * IOFileFilters and directory observers instead of unique observers of each
	 * individual file
	 */
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
		System.out.println("Daemon server running on port " + server.getPort());
		state = DaemonStatus.ACTIVE;
		try {
			while (true) {
				if (state == DaemonStatus.ACTIVE) {
					for (FileAlterationObserver observer : observers) {
						observer.checkAndNotify();
					}
				}
				Thread.sleep(period);
			}
		} catch (InterruptedException ie) {
			listener.log.addEntry("Daemon interrupted, exiting...");
			try {
				server.stopServer();
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
	 * Suspend file monitoring
	 */
	public void suspendMonitoring() {
		state = DaemonStatus.SUSPENDED;
	}
	
	/**
	 * Start file monitoring if state is suspended
	 */
	public void startMonitoring() {
		state = DaemonStatus.ACTIVE;
	}
	
	/**
	 * Changes the refresh period
	 * @param newPeriod - New refresh period to use
	 */
	public void updatePeriod(int newPeriod) {
		period = newPeriod;
	}
}
