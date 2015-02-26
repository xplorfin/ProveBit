package org.provebit.daemon;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationObserver;
import org.provebit.merkle.Merkle;
public class MerkleDaemon extends Thread {
    int period;
    FileAlterationObserver observer;
    DirectoryMonitor listener;
    
    /**
     * Daemon constructor, 
     * @param dir - Dir to run daemon on
     * @param period - Daemon polling period
     */
    public MerkleDaemon(String dir, int period) {
    	File toWatch = new File(dir);
        observer = new FileAlterationObserver(toWatch);
        listener = new DirectoryMonitor(toWatch, false);
        observer.addListener(listener);
        this.period = period;
        setDaemon(true);
    }
    
    public void run() {       
        if (!Thread.currentThread().isDaemon()) {
            Thread.currentThread().interrupt();
            return;
        }
        try {
			observer.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
        monitorDirectory();
    }

	private void monitorDirectory() {
		try {
			while (true) {
				observer.checkAndNotify();
				Thread.sleep(period);
			}
		} catch (InterruptedException ie) {
			System.out.println("Monitor interrupted, exiting...");
			try {
				observer.destroy();
			} catch (Exception e) {
				System.out.println("Observer destruction failed, messy exit...");
				e.printStackTrace();
			}
		} finally {
			Thread.currentThread().interrupt();
		}
	}
    
    /**
     * Get the current merkle tree
     * @return Currently constructed merkle tree
     */
    public Merkle getTree() {
        return listener.getTree();
    }
    
    /**
     * Get number of changes detected since daemon launched
     * @return number of changes since launch
     */
    public int getChanges() {
        return listener.getChanges();
    }
}
