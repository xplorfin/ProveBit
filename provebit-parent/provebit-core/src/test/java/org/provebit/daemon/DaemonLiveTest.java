package org.provebit.daemon;

import java.io.IOException;

import org.provebit.merkle.Merkle;

public class DaemonLiveTest {
	static String DAEMONDIR = "/src/test/java/org/provebit/daemon/testDaemonDir";
	static String daemonDirPath;
	// Use jps -v to thread information
	public static void main(String[] args) throws InterruptedException, IOException {
		daemonDirPath = new java.io.File( "." ).getCanonicalPath() + DAEMONDIR;
		MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), 100);
		daemon.start();
		Thread.sleep(2000);
		new DaemonLiveReconnect().start();
		System.out.println("Main closing...");
	}
}
