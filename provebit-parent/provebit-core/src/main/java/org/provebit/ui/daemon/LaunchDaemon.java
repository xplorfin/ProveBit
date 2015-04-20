package org.provebit.ui.daemon;

import org.provebit.daemon.MerkleDaemon;
import org.provebit.merkle.FileMerkle;
import org.provebit.merkle.HashType;

public class LaunchDaemon {
	private static int defaultPeriod = 1000;
	
	public static void main(String[] args) {
		new MerkleDaemon(new FileMerkle(HashType.SHA256), defaultPeriod).start(); 
	}
}
