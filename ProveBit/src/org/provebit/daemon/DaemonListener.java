package org.provebit.daemon;

import java.io.File;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.provebit.merkle.Merkle;

public class DaemonListener implements FileAlterationListener {
    Merkle tree = null;
    
    public DaemonListener(String dir) {
        tree = new Merkle(dir);
    }
    
    @Override
    public void onDirectoryChange(File arg0) {
        System.out.println("Directory changed: " + arg0.getName());
        reconstructTree();
    }

    @Override
    public void onDirectoryCreate(File arg0) {
        System.out.println("Directory created: " + arg0.getName());
        reconstructTree();
    }

    @Override
    public void onDirectoryDelete(File arg0) {
        System.out.println("Directory deleted: " + arg0.getName());
        reconstructTree();
    }

    @Override
    public void onFileChange(File arg0) {
        System.out.println("File changed: " + arg0.getName());
        reconstructTree();
    }

    @Override
    public void onFileCreate(File arg0) {
        System.out.println("New file found: " + arg0.getName());
        reconstructTree();
    }

    @Override
    public void onFileDelete(File arg0) {
        System.out.println("File: " + arg0.getName() + " was deleted");
        reconstructTree();      
    }

    @Override
    public void onStart(FileAlterationObserver arg0) {
        System.out.println("Daemon listener started on " + arg0.getDirectory().getPath());
        if (tree == null) {
            tree.makeTree();
            System.out.println("Starting hash: " + Hex.encodeHexString(tree.getRootHash()));
        }
    }

    @Override
    public void onStop(FileAlterationObserver arg0) {
        System.out.println("Daemon listener for " + arg0.getDirectory().getPath() + " stopped");
    }
    
    private void reconstructTree() {
        System.out.println("Reconstructing tree...");
        System.out.println("Old root: " + Hex.encodeHexString(tree.getRootHash()));
        tree.makeTree();
        System.out.println("New root: " + Hex.encodeHexString(tree.getRootHash()));
    }

}
