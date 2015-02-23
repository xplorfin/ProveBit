package org.provebit.daemon;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class DaemonListener implements FileAlterationListener {

    @Override
    public void onDirectoryChange(File arg0) {
        /** @TODO Notify change, reconstruct tree **/
    }

    @Override
    public void onDirectoryCreate(File arg0) {
        /** @TODO Notify change, reconstruct tree **/
    }

    @Override
    public void onDirectoryDelete(File arg0) {
        /** @TODO Notify change, reconstruct tree **/
    }

    @Override
    public void onFileChange(File arg0) {
        /** @TODO Notify change, reconstruct tree **/
    }

    @Override
    public void onFileCreate(File arg0) {
        /** @TODO Notify change, reconstruct tree **/
    }

    @Override
    public void onFileDelete(File arg0) {
        /** @TODO Notify change, reconstruct tree **/        
    }

    @Override
    public void onStart(FileAlterationObserver arg0) {
        /** @TODO **/
    }

    @Override
    public void onStop(FileAlterationObserver arg0) {
        /** @TODO **/
    }

}
