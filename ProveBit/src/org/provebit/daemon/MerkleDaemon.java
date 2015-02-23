package org.provebit.daemon;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class MerkleDaemon extends Thread {
    int period;
    FileAlterationObserver dirObserver;
    FileAlterationListener dirListener;
    FileAlterationMonitor dirMonitor;
    
    public MerkleDaemon(String dir, int period) {
        this.period = period;
        dirObserver = new FileAlterationObserver(new File(dir));
        dirListener = new DaemonListener(dir);
        dirObserver.addListener(dirListener);
        dirMonitor = new FileAlterationMonitor();
        dirMonitor.addObserver(dirObserver);
    }
    
    public void run() {        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("Stopping monitor");
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
