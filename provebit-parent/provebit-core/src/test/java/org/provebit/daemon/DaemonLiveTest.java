package org.provebit.daemon;

import java.io.File;
import java.io.IOException;

import org.provebit.merkle.Merkle;

public class DaemonLiveTest {
	// Change to some path on your local computer
    private static File liveDir = new File("/home/dan/school/cs429/liveDemos");
	public static void main(String[] args) throws InterruptedException, IOException {
		MerkleDaemon daemon = new MerkleDaemon(new Merkle(liveDir, false), 100);
		daemon.start();
		Thread.sleep(2000);
		new DaemonLiveReconnect().start();
		System.out.println("Main closing...");
	}
}
