package org.provebit.ui.daemon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.provebit.daemon.DaemonProtocol;
import org.provebit.daemon.DaemonProtocol.DaemonMessage;
import org.provebit.daemon.DaemonProtocol.DaemonMessage.DaemonMessageType;
import org.provebit.ui.daemon.DaemonController.DaemonNotification;
import org.provebit.utils.ServerUtils;
import org.simplesockets.client.SimpleClient;

public class DaemonModel extends Observable {
	private DaemonStatus daemonStatus;
	private enum DaemonStatus{ACTIVE, SUSPENDED, OFFLINE, TRACKING};
	private SimpleClient daemonClient;
	private int port;
	private final String hostname = "localhost";
	private DaemonProtocol clientProtocol;
	private boolean daemonConnected;

	public DaemonModel() {
		clientProtocol = getProtocol();
		connectToDaemon();
		
		if (!daemonConnected) { // No daemon running, start a new one
			try {
				launchNewDaemon();
			} catch (IOException | InterruptedException e) {
				System.out.println("Failed to launch new JVM with daemon");
				e.printStackTrace();
			}
			System.out.println("LAUNCHED");
		}
		
		if (!daemonConnected) {
			System.exit(0);
			throw new RuntimeException("Cannot connect to local daemon server!");
			
		}
	}
	
	private void createDaemonClient() {
		daemonClient = new SimpleClient(hostname, port, clientProtocol);
	}
	
	/**
	 * EARLY CODE
	 * Need to ensure robustness across platforms
	 * Found: http://stackoverflow.com/questions/1229605/is-this-really-the-best-way-to-start-a-second-jvm-from-java-code
	 * 
	 * Launches a new JVM that executes the daemon
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void launchNewDaemon() throws IOException, InterruptedException {
		String separator = System.getProperty("file.separator");
		String classpath = System.getProperty("java.class.path");
		String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
		ProcessBuilder processBuilder =  new ProcessBuilder(path, "-cp", classpath, LaunchDaemon.class.getName(), "true");
		processBuilder.start();
		waitOnDaemon();
	}
	
	/**
	 * Continue to retry connection to newly launched daemon process
	 * until connection succeeds
	 */
	private void waitOnDaemon() {
		while(!daemonConnected) {
			connectToDaemon();
		}
	}
	
	/**
	 * Attempts to reconnect to an existing daemon server
	 * 
	 * If connection is successful the port member will be set to the servers port
	 * @return true if connection succeeds, false o/w
	 */
	private void connectToDaemon() {
		int testPort = ServerUtils.getPort(), attempts = 10;
		SimpleClient heartbeat = new SimpleClient(hostname, testPort, clientProtocol);
		boolean connected = false;
		while (!connected && attempts > 0) {
			heartbeat.sendRequest(new DaemonMessage(DaemonMessageType.HEARTBEAT, null));
			DaemonMessage reply = (DaemonMessage) heartbeat.getReply();
			if (reply != null) {
				System.out.println("Daemon server found on port " + testPort);
				port = testPort;
				connected = true;
				createDaemonClient();
			}
			attempts--;
		}
		daemonConnected = connected;
	}
	
	/**
	 * Creates and returns a defined DaemonProtocol
	 * @return DaemonProtocol
	 */
	private DaemonProtocol getProtocol() {
		return new DaemonProtocol() {
			@Override
			public DaemonMessage handleMessage(DaemonMessage request) {
				return request;
			}
		};
	}
	/**
	 * Add the parameter file to the current Merkle tree
	 * @param file - the file to be added
	 * @param recursive
	 */
	public void addFileToTree(File file, boolean recursive) {
		Map<String,Boolean> fileMap = new HashMap<String, Boolean>();
		fileMap.put(file.getAbsolutePath(), recursive);
		DaemonMessage addFileRequest = new DaemonMessage(DaemonMessageType.ADDFILES, fileMap);
		daemonClient.sendRequest(addFileRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		if (reply != null) {
			notifyChange(DaemonNotification.UPDATETRACKING);
		}
	}
	/**
	 * Remove the parameter file from the current Merkle tree
	 * @param file - file to be removed
	 */
	public void removeFileFromTree(File file) {
		List<String> fileList = new ArrayList<String>();
		fileList.add(file.getAbsolutePath());
		DaemonMessage removeFileRequest = new DaemonMessage(DaemonMessageType.REMOVEFILES, fileList);
		daemonClient.sendRequest(removeFileRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		if (reply != null) {
			notifyChange(DaemonNotification.UPDATETRACKING);
		}
	}
	/**
	 * Sets the Period of the Daemon and then starts it using SETPERIOD, START DaemonMessages
	 * @param period
	 */
	public void startDaemon(int period) {
		if (!daemonConnected) {
			try {
				launchNewDaemon();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		DaemonMessage startRequest;
		startRequest = new DaemonMessage(DaemonMessageType.START, null);
		daemonClient.sendRequest(startRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		if (reply != null) {
			getDaemonStatus();
			notifyChange(DaemonNotification.DAEMONSTATUS);
			notifyChange(DaemonNotification.UPDATETRACKING);
		}
	}
	/**
	 * Requests if the parameter file is currently being tracked by the Daemon
	 * @param file
	 * @return true if the file is being tracked, false otherwise
	 */
	public boolean isTracking(File file) {
		DaemonMessage isTrackedRequest = new DaemonMessage(DaemonMessageType.ISTRACKED, file.getAbsolutePath());
		daemonClient.sendRequest(isTrackedRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		if (reply != null) {
			return (boolean) reply.data;
		}
		return false;
	}
	
	/**
	 * Suspends the Daemon using a SUSPEND Daemon Message
	 * 
	 */
	public void stopDaemon() {
		DaemonMessage suspendRequest = new DaemonMessage(DaemonMessageType.SUSPEND, null);
		daemonClient.sendRequest(suspendRequest);
		getDaemonStatus();
		notifyChange(DaemonNotification.DAEMONSTATUS);		
	}
	
	/**
	 * Kills the Daemon using the KILL Daemon Message
	 */
	public void killDaemon() {
		DaemonMessage killRequest = new DaemonMessage(DaemonMessageType.KILL, null);
		daemonClient.sendRequest(killRequest);
		daemonStatus = DaemonStatus.OFFLINE;
		daemonConnected = false;
		notifyChange(DaemonNotification.DAEMONSTATUS);
		notifyChange(DaemonNotification.UPDATETRACKING);
	}
	/**
	 * Fetches log from Daemon Process using GETLOG DaemonMessage
	 * @return String representation of Daemon Log
	 */
	public String getDaemonLog() {
		DaemonMessage logRequest = new DaemonMessage(DaemonMessageType.GETLOG, null);
		daemonClient.sendRequest(logRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		if (reply != null) {
			return (String)reply.data;
		}
		return null;
	}
	
	/**
	 * Requests the Daemons current status using the GETSTATE DaemonMessage
	 * @return String of current daemon status
	 */
	public String getDaemonStatus() {
		DaemonMessage stateRequest = new DaemonMessage(DaemonMessageType.GETSTATE, null);
		daemonClient.sendRequest(stateRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		if (reply == null || !daemonConnected) {
			daemonConnected = false;
			daemonStatus = DaemonStatus.OFFLINE;
		} else {
			daemonStatus = ((int)reply.data == 0) ? DaemonStatus.SUSPENDED : DaemonStatus.ACTIVE;
		}
		return daemonStatus.toString();
	}
	
	/**
	 * Requests the number of file currently being tracked by the Daemon
	 * @return Number of files tracked by Daemon
	 */
	public int getNumTracked() {
		String[] trackedFiles = getTrackedFileStrings();
		if (trackedFiles == null) {
			return 0;
		}
		return trackedFiles.length;
	}
	
	/**
	 * Updates the daemon's operating period using SETPERIOD DaemonMessage
	 * @param newPeriod
	 */
	public void updatePeriod(int newPeriod) {
		DaemonMessage periodUpdateRequest = new DaemonMessage(DaemonMessageType.SETPERIOD, newPeriod);
		daemonClient.sendRequest(periodUpdateRequest);
	}
	
	/**
	 * Requests a list of Tracked Files and directories from the Daemon using GETTRACKED DaemonMessage
	 * @return String[] of Tracked Files and directories
	 */
	@SuppressWarnings("unchecked")
	public String[] getTrackedFileStrings() {
		if (!daemonConnected) return null;
		DaemonMessage getTrackedRequest = new DaemonMessage(DaemonMessageType.GETTRACKED, null);
		daemonClient.sendRequest(getTrackedRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		if (reply != null) {
			List<String> trackingList = ((List<List<String>>) reply.data).get(1); // Get directories first
			trackingList.addAll(((List<List<String>>) reply.data).get(0));
			return (String[]) trackingList.toArray(new String[trackingList.size()]);
		}
		return null;
	}
	
	private void notifyChange(DaemonNotification type) {
		setChanged();
		notifyObservers(type);
	}
}
