package org.provebit.daemon;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.commons.codec.binary.Hex;
import org.provebit.merkle.Merkle;
public class MerkleDaemon extends Thread {
    int period;
    Path path;
    FileSystem fs;
    Merkle tree;
    int changes;
    
    /**
     * Daemon constructor, 
     * @param dir - Dir to run daemon on
     * @param period - Daemon polling period
     */
    public MerkleDaemon(String dir, int period) {
        setDaemon(true);
        this.period = period;
        path = Paths.get(dir);
        fs = path.getFileSystem();
        tree = new Merkle(dir);
        tree.makeTree();
        changes = 0;
    }
    
    public void run() {       
        if (!Thread.currentThread().isDaemon()) {
            Thread.currentThread().interrupt();
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("Stopping daemon");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }   
        });
        
        monitorDirectory();
    }

	private void monitorDirectory() {
		try(WatchService wService = fs.newWatchService()) {
            path.register(wService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            WatchKey key = null;
            
            while(true) {
                Thread.sleep(period);
                key = wService.take();
                
                if (!key.pollEvents().isEmpty()) { // Event occurred
                	changes++;
                	reconstructTree();
                	key.pollEvents().clear();
                }
                
                key.reset();
            }
        } catch (IOException | InterruptedException e2) {
            // Ignore
        }
	}
    
    /**
     * Helper function called when modification to directory is detected
     * Reconstructs merkle tree
     */
    private void reconstructTree() {
        System.out.println("Reconstructing tree...");
        System.out.println("Old root: " + Hex.encodeHexString(tree.getRootHash()));
        byte[] newHash = tree.makeTree();
        System.out.println("New root: " + Hex.encodeHexString(newHash));
    }
    
    /**
     * Get the current merkle tree
     * @return Currently constructed merkle tree
     */
    public Merkle getTree() {
        return tree;
    }
    
    /**
     * Get number of changes detected since daemon launched
     * @return number of changes since launch
     */
    public int getChanges() {
        return changes;
    }
}
