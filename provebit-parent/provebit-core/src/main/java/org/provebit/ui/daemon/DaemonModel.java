package org.provebit.ui.daemon;

import java.util.Observable;

import org.provebit.daemon.Log;
import org.provebit.daemon.MerkleDaemon;
import org.provebit.merkle.Merkle;

public class DaemonModel extends Observable {
	public enum DaemonStatus{ONLINE, OFFLINE};
	private MerkleDaemon daemon;
	private DaemonStatus daemonStatus;
	
	public DaemonModel() {
		daemonStatus = DaemonStatus.OFFLINE;
		daemon = null;
	}
	
	public void setDaemon(Merkle tree, int period) {
		daemon = new MerkleDaemon(tree, period);
	}
	
	public void startDaemon() {
		if (daemon == null) return;
		daemon.start();
		daemonStatus = (daemon.isAlive()) ? DaemonStatus.ONLINE : DaemonStatus.OFFLINE;
		notifyChange();
	}
	
	public void stopDaemon() {
		if (daemon == null) return;
		daemon.interrupt();
		daemonStatus = (daemon.isAlive()) ? DaemonStatus.ONLINE : DaemonStatus.OFFLINE;
		notifyChange();
	}
	
	public Log getDaemonLog() {
		return daemon.getLog();
	}
	
	public DaemonStatus getDaemonStatus() {
		return daemonStatus;
	}
	
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}
