package org.provebit.ui.daemon;

import org.provebit.daemon.MerkleDaemon;

public class LaunchDaemon {
	private static int defaultPeriod = 1000;
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Missing argument");
			return;
		}
		boolean recover = (args[0].equals("true")) ? true : false;
		new MerkleDaemon(recover, defaultPeriod).start();
	}
}
