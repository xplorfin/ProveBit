package org.provebit.ui.daemon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.provebit.daemon.DaemonProtocol;
import org.provebit.daemon.DaemonProtocol.DaemonMessage;
import org.provebit.daemon.DaemonProtocol.DaemonMessage.DaemonMessageType;
import org.provebit.daemon.MerkleDaemon;
import org.provebit.merkle.Merkle;
import org.provebit.ui.daemon.DaemonController.DaemonNotification;
import org.simplesockets.client.SimpleClient;

public class DaemonModel extends Observable {
	private DaemonStatus daemonStatus;
	private enum DaemonStatus{ACTIVE, SUSPENDED, TRACKING};
	private SimpleClient daemonClient;
	private int port;
	private final String hostname = "localhost";
	private DaemonProtocol clientProtocol;
	private boolean daemonConnected;
	private int defaultPeriod = 100;
	
	/**
	 * With the addition of socket based IPC we have the following considerations
	 * 
	 * When DaemonModel is instantiated, we need to see if there is already a MerkleDaemon running
	 * somewhere on the system, this should be done with a heartbeat to hostname localhost, on a known port
	 * 
	 * If the heartbeat fails, then the server is not running, thus we need to start it via startDaemon
	 * If the heartbeat succeeds, then we do not need to launch a daemon via startDaemon
	 * All interactions need to be updated to use the network based model instead of directly accessing the daemon
	 * as the daemon is not necessarily running in the scope of the jvm currently running this application
	 */
	
	/** @TODO Change to use network model */
	public DaemonModel() {
		clientProtocol = getProtocol();
		connectToDaemon();
		
		if (!daemonConnected) { // No daemon running, start a new one
			new MerkleDaemon(new Merkle(), defaultPeriod).start();
			connectToDaemon();
		}
		
		if (daemonConnected) {
			daemonClient = new SimpleClient(hostname, port, clientProtocol);
			daemonStatus = DaemonStatus.ACTIVE;
		} else {
			throw new RuntimeException("Cannot connect to local daemon server!");
		}
	}
	
	/**
	 * Attempts to reconnect to an existing daemon server
	 * 
	 * If connection is successful the port member will be set to the servers port
	 * @return true if connection succeeds, false o/w
	 */
	@SuppressWarnings("unchecked")
	private void connectToDaemon() {
		// Get last known port form well known (application folder config file) location
		// For now the daemon starts the server on a known port (9999)
		/** @TODO Remove hardcoded port */
		int testPort = 9999, attempts = 10;
		SimpleClient heartbeat = new SimpleClient(hostname, testPort, clientProtocol);
		boolean connected = false;
		while (!connected && attempts > 0) {
			heartbeat.sendRequest(new DaemonMessage(DaemonMessageType.HEARTBEAT, null));
			DaemonMessage reply = (DaemonMessage) heartbeat.getReply();
			if (reply != null) {
				System.out.println("Daemon server found on port " + testPort);
				port = testPort;
				connected = true;
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
		DaemonMessage setPeriodRequest;
		DaemonMessage startRequest;
		setPeriodRequest = new DaemonMessage(DaemonMessageType.SETPERIOD, period);
		startRequest = new DaemonMessage(DaemonMessageType.START, null);
		daemonClient.sendRequest(setPeriodRequest);
		daemonClient.sendRequest(startRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		if (reply != null) {
			daemonStatus = DaemonStatus.ACTIVE;
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
		return (boolean) reply.data;
	}
	
	/**
	 * Suspends the Daemon using a SUSPEND Daemon Message
	 * 
	 */
	public void stopDaemon() {
		if (getDaemonStatus().equals(DaemonStatus.SUSPENDED.toString())) return;
		
		DaemonMessage suspendRequest = new DaemonMessage(DaemonMessageType.SUSPEND, null);
		daemonClient.sendRequest(suspendRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		if((boolean)reply.data == true) {
			daemonStatus = DaemonStatus.SUSPENDED;
		}
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
		daemonStatus = ((int)reply.data == 0) ? DaemonStatus.SUSPENDED : DaemonStatus.ACTIVE;
		return daemonStatus.toString();
	}
	
	/**
	 * Requests the number of file currently being tracked by the Daemon
	 * @return Number of files tracked by Daemon
	 */
	public int getNumTracked() {
		return getTrackedFileStrings().length;
	}
	
	/**
	 * Updates the daemon's operating period using SETPERIOD DaemonMessage
	 * @param newPeriod
	 */
	public void updatePeriod(int newPeriod) {
		DaemonMessage periodUpdateRequest = new DaemonMessage(DaemonMessageType.SETPERIOD, newPeriod);
		daemonClient.sendRequest(periodUpdateRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
				
	}
	
	/**
	 * Requests a list of Tracked Files from the Daemon using GETTRACKED DaemonMessage
	 * @return String[] of Tracked Files
	 */
	@SuppressWarnings("unchecked")
	public String[] getTrackedFileStrings() {
		DaemonMessage getTrackedRequest = new DaemonMessage(DaemonMessageType.GETTRACKED, null);
		daemonClient.sendRequest(getTrackedRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		if (reply != null) {
			List<String> trackedFileList = ((ArrayList<ArrayList<String>>) reply.data).get(0);
			return (String[]) trackedFileList.toArray(new String[trackedFileList.size()]);
		}
		return null;
	}
	
	private void notifyChange(DaemonNotification type) {
		setChanged();
		notifyObservers(type);
	}
}
