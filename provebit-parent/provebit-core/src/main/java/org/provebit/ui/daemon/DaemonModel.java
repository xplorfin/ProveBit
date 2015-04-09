package org.provebit.ui.daemon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.provebit.daemon.DaemonProtocol;
import org.provebit.daemon.MerkleDaemon;
import org.provebit.daemon.DaemonProtocol.DaemonMessage;
import org.provebit.daemon.DaemonProtocol.DaemonMessage.DaemonMessageType;
import org.provebit.merkle.Merkle;
import org.provebit.ui.daemon.DaemonController.DaemonNotification;
import org.simplesockets.client.SimpleClient;

public class DaemonModel extends Observable {
	private MerkleDaemon daemon;
	private DaemonStatus daemonStatus;
	private enum DaemonStatus{ONLINE, OFFLINE, TRACKING};
	private Merkle tree; /** @TODO Using networking this model no longer holds reference to Merkle tree, only the
						  * MerkleDaemon thread holds the reference */
	private SimpleClient daemonClient;
	private int port;
	private final String hostname = "localhost";
	private DaemonProtocol clientProtocol;
	
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
		
		if (daemonExists()) {
			daemonClient = new SimpleClient(hostname, port, clientProtocol);
		} else {
			daemonStatus = DaemonStatus.OFFLINE;
			tree = new Merkle();
			daemon = null;
			// Setup and launch a new daemon server
		}
		
	}
	
	/**
	 * Attempts to reconnect to an existing daemon server
	 * 
	 * If connection is successful the port member will be set to the servers port
	 * @return true if connection succeeds, false o/w
	 */
	@SuppressWarnings("unchecked")
	private boolean daemonExists() {
		// Get port from known (application folder config file) location
		/** @TODO Remove hardcoded port */
		int testPort = 9999;
		SimpleClient heartBeatClient = new SimpleClient(hostname, testPort, clientProtocol);
		heartBeatClient.sendRequest(new DaemonMessage<String>(DaemonMessageType.HEARTBEAT, null));
		DaemonMessage<String> reply = (DaemonMessage<String>) heartBeatClient.getReply();
		if (reply != null) {
			port = testPort;
			return true;
		}
		return false;
	}
	
	
	/**
	 * Creates and returns a defined DaemonProtocol
	 * @return DaemonProtocol
	 */
	private DaemonProtocol getProtocol() {
		return new DaemonProtocol() {
			@Override
			public DaemonMessage<?> handleMessage(DaemonMessage<?> request) {
				System.out.println("Daemon client got request: " + request.type.toString());
				return request;
			}
		};
	}
	
	/** @TODO Change to use network model */
	public void addFileToTree(File file, boolean recursive) {
		tree.addTracking(file, recursive);
		notifyChange(DaemonNotification.UPDATETRACKING);
	}
	
	/** @TODO Change to use network model */
	public void removeFileFromTree(File file) {
		tree.removeTracking(file);
		notifyChange(DaemonNotification.UPDATETRACKING);
	}
	
	/** @TODO Change to use network model */
	public void startDaemon(int period) {
		if (daemon != null) return;
		daemon = new MerkleDaemon(tree, period);
		daemon.start();
		daemonStatus = DaemonStatus.ONLINE;
		notifyChange(DaemonNotification.DAEMONSTATUS);
	}
	
	/** @TODO Change to use network model */
	public boolean isTracking(File file) {
		return tree.isTracking(file);
	}
	
	/** @TODO Change to use network model */
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
