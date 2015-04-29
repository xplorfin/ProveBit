package org.provebit.ui.daemon;

import org.provebit.daemon.MerkleDaemon;

public class LaunchDaemon {
	private static int defaultPeriod = 1000;
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Missing argument");
			return;
		}
		boolean recover = Boolean.parseBoolean(args[0]);
		new MerkleDaemon(recover, defaultPeriod).start();
	}
}
