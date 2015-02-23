package org.provebit.daemon;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.provebit.merkle.Merkle;

public class MerkleDaemon extends Thread {
    Merkle tree;
    int period;
    FileAlterationObserver dirObserver;
    FileAlterationMonitor dirMonitor;
    
    public MerkleDaemon(Merkle tree, int period) {
        this.tree = tree;
        this.period = period;
        dirObserver = new FileAlterationObserver(tree.getDir());
        dirObserver.addListener(new DaemonListener());
        dirMonitor = new FileAlterationMonitor(period);
        dirMonitor.addObserver(dirObserver);
    }
    
    public void run() {
        if (!Thread.currentThread().isDaemon()) {
            throw new RuntimeException("MerkleDaemon not running as daemon!");
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    dirMonitor.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }   
        }); 
        
        try {
            dirMonitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
