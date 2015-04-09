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
	private Merkle tree;
	private SimpleClient daemonClient;
	private int port;
	private String hostname;
	
	public DaemonModel() {
		daemonStatus = DaemonStatus.OFFLINE;
		tree = new Merkle();
		daemon = null;
		DaemonProtocol protocol = getProtocol();
		daemonClient = new SimpleClient(hostname, port, protocol);
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
