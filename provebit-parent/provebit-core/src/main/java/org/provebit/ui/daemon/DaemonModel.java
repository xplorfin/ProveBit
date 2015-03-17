package org.provebit.ui.daemon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import org.provebit.daemon.Log;
import org.provebit.daemon.MerkleDaemon;
import org.provebit.merkle.Merkle;
import org.provebit.ui.daemon.DaemonController.DaemonNotification;

public class DaemonModel extends Observable {
	private MerkleDaemon daemon;
	private DaemonStatus daemonStatus;
	private enum DaemonStatus{ONLINE, OFFLINE, TRACKING};
	private Merkle tree;
	
	public DaemonModel() {
		daemonStatus = DaemonStatus.OFFLINE;
		tree = new Merkle();
		daemon = null;
	}
	
	public void addFileToTree(File file, boolean recursive) {
		tree.addTracking(file, recursive);
		notifyChange(DaemonNotification.UPDATETRACKING);
	}
	
	public void removeFileFromTree(File file) {
		tree.removeTracking(file);
		notifyChange(DaemonNotification.UPDATETRACKING);
	}
	
	public void startDaemon(int period) {
		if (daemon != null) return;
		daemon = new MerkleDaemon(tree, period);
		daemon.start();
		daemonStatus = DaemonStatus.ONLINE;
		notifyChange(DaemonNotification.DAEMONSTATUS);
	}
	
	public boolean isTracking(File file) {
		return tree.isTracking(file);
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
	
	public int getNumTracked() {
		return tree.getNumTracked();
	}
	
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
