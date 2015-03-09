package org.provebit.ui.daemon;

import java.io.File;
import java.util.Observable;

import org.provebit.daemon.Log;
import org.provebit.daemon.MerkleDaemon;
import org.provebit.merkle.Merkle;
import org.provebit.ui.daemon.DaemonController.DaemonNotification;

public class DaemonModel extends Observable {
	private MerkleDaemon daemon;
	private DaemonStatus daemonStatus;
	private enum DaemonStatus{ONLINE, OFFLINE};
	private Merkle tree;
	
	public DaemonModel() {
		daemonStatus = DaemonStatus.OFFLINE;
		tree = new Merkle();
		daemon = null;
	}
	
	public void addFileToTree(File file, boolean recursive) {
		tree.addTracking(file, recursive);
	}
	
	public void removeFileFromTree(File file) {
		tree.removeTracking(file);
	}
	
	public void startDaemon(int period) {
		if (daemon != null) return;
		daemon = new MerkleDaemon(tree, period);
		daemon.start();
		daemonStatus = DaemonStatus.ONLINE;
		notifyChange(DaemonNotification.DAEMONSTATUS);
	}
	
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
	
	public Log getDaemonLog() {
		return (daemon == null) ? null : daemon.getLog();
	}
	
	public String getDaemonStatus() {
		return daemonStatus.toString();
	}
	
	private void notifyChange(DaemonNotification type) {
		setChanged();
		notifyObservers(type);
	}
}
