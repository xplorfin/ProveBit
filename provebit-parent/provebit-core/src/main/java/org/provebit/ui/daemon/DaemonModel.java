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
			daemonStatus = daemonStatus.ACTIVE;
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
			heartbeat.sendRequest(new DaemonMessage<String>(DaemonMessageType.HEARTBEAT, null));
			DaemonMessage<String> reply = (DaemonMessage<String>) heartbeat.getReply();
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
			public DaemonMessage<?> handleMessage(DaemonMessage<?> request) {
				return request;
			}
		};
	}
	
	public void addFileToTree(File file, boolean recursive) {
		Map<String,Boolean> fileMap = new HashMap<String, Boolean>();
		fileMap.put(file.getAbsolutePath(), recursive);
		DaemonMessage<Map<String, Boolean>> addFileRequest = new DaemonMessage<Map<String, Boolean>>(DaemonMessageType.ADDFILES, fileMap);
		daemonClient.sendRequest(addFileRequest);
		DaemonMessage<?> reply = (DaemonMessage<?>) daemonClient.getReply();
		if (reply != null) {
			notifyChange(DaemonNotification.UPDATETRACKING);
		}
	}
	
	public void removeFileFromTree(File file) {
		List<String> fileList = new ArrayList<String>();
		fileList.add(file.getAbsolutePath());
		DaemonMessage<List<String>> removeFileRequest = new DaemonMessage<List<String>>(DaemonMessageType.REMOVEFILES, fileList);
		daemonClient.sendRequest(removeFileRequest);
		DaemonMessage<?> reply = (DaemonMessage<?>) daemonClient.getReply();
		if (reply != null) {
			notifyChange(DaemonNotification.UPDATETRACKING);
		}
	}
	
	public void startDaemon(int period) {
		DaemonMessage<Integer> setPeriodRequest;
		DaemonMessage<String> startRequest;
		setPeriodRequest = new DaemonMessage<Integer>(DaemonMessageType.SETPERIOD, period);
		startRequest = new DaemonMessage<String>(DaemonMessageType.START, null);
		daemonClient.sendRequest(setPeriodRequest);
		daemonClient.sendRequest(startRequest);
		DaemonMessage<?> reply = (DaemonMessage<?>) daemonClient.getReply();
		if (reply != null) {
			daemonStatus = DaemonStatus.ACTIVE;
		}
	}
	
	public boolean isTracking(File file) {
		DaemonMessage<String> isTrackedRequest = new DaemonMessage<String>(DaemonMessageType.ISTRACKED, file.getAbsolutePath());
		daemonClient.sendRequest(isTrackedRequest);
		DaemonMessage<?> reply = (DaemonMessage<?>) daemonClient.getReply();
		return (boolean) reply.data;
	}
	
	/** @TODO Change to use network model */
	// Should SUSPEND the daemon now, not destroy it
	public void stopDaemon() {
		if (daemon == null) return;
		daemon.interrupt();
		try {
			daemon.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		daemon = null;
		daemonStatus = DaemonStatus.OFFLINE;
		notifyChange(DaemonNotification.DAEMONSTATUS);
	}
	
	/** @TODO Change to use network model */
	public String getDaemonLog() {
		return (daemon == null) ? null : daemon.getLog();
	}
	
	/** @TODO Change to use network model */
	public String getDaemonStatus() {
		return daemonStatus.toString();
	}
	
	/** @TODO Change to use network model */
	public int getNumTracked() {
		return tree.getNumTracked();
	}
	
	/** @TODO Change to use network model */
	public void updatePeriod(int newPeriod) {
		
	}
	
	/** @TODO Change to use network model */
	public String[] getTrackedFileStrings() {
		List<String> tracked = new ArrayList<String>();
		for (File file : tree.getTrackedFiles()) {
			tracked.add(file.getAbsolutePath());
		}
		for (File dir : tree.getTrackedDirs()) {
			tracked.add(dir.getAbsolutePath());
		}
		return (String[]) tracked.toArray(new String[tracked.size()]);
	}
	
	private void notifyChange(DaemonNotification type) {
		setChanged();
		notifyObservers(type);
	}
}
