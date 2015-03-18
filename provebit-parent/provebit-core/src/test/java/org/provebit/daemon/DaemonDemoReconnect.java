package org.provebit.daemon;

import java.io.IOException;
import java.util.Set;

public class DaemonDemoReconnect extends Thread {
	public void run() {  
		MerkleDaemon daemon = null;
        System.out.println("Reconnect thread running...");
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		for (Thread thread : threadArray) {
			System.out.println(thread.getName());
			if (thread.getName().equals("MerkleDaemon")) {
				System.out.println("Found daemon as thread: " + thread.getId());
				daemon = (MerkleDaemon) thread;
			}
		}
		if (daemon == null) {
			System.out.println("Couldn't find daemon, something wrong, closing...");
			System.exit(0);
		}
		System.out.println("Press any key to terminate daemon and dump log");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log log = daemon.getLogActual();
		System.out.println(log.toString());
		daemon.interrupt();
	}
}
