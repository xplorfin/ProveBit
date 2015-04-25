package org.provebit.daemon;

import java.io.File;
import java.io.IOException;

import org.provebit.merkle.FileMerkle;
import org.provebit.merkle.HashType;

public class DaemonDemo {
	// Change to some path on your local computer
    private static File liveDir = new File("/home/dan/school/cs429/liveDemos");
	public static void main(String[] args) throws InterruptedException, IOException {
		FileMerkle m = new FileMerkle(HashType.SHA256);
    	m.addTracking(liveDir, false);
		MerkleDaemon daemon = new MerkleDaemon(false, 100);
		daemon.start();
		Thread.sleep(1000);
		new DaemonDemoReconnect().start();
		Thread.sleep(10000);
		System.out.println("Main closing...");
	}
}
