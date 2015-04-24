package org.provebit.ui.daemon;

import org.provebit.daemon.MerkleDaemon;

public class LaunchDaemon {
	private static int defaultPeriod = 1000;
	
	public static void main(String[] args) {
		new MerkleDaemon(true, defaultPeriod).start(); 
	}
}
