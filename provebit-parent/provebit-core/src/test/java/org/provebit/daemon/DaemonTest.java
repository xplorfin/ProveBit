package org.provebit.daemon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.provebit.daemon.DirectoryMonitor.MonitorEvent;
import org.provebit.daemon.Log.LogEntry;
import org.provebit.merkle.Merkle;

public class DaemonTest {
    public File daemonDir;
    public File daemonSubDir;
    public File file1;
    public File file2;
    public File tempFile;
    public File subDirFile;
    public final int TESTSLEEP = 100;
    public final int DAEMONPERIOD = 50;
    
    @Rule
    public TemporaryFolder daemonTemp = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        daemonDir = daemonTemp.getRoot();
        daemonSubDir = new File(daemonDir, "subDir");
    	file1 = new File(daemonDir, "file1");
        file2 = new File(daemonDir, "file2");
        tempFile = new File(daemonDir, "tempfile");
        subDirFile = new File(daemonSubDir, "subDirTempFile");
    	FileUtils.write(file1, "file 1");
    	FileUtils.write(file2, "file 2");
    }
    
    @Test
    public void testLaunch() {
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
        assertTrue(!daemon.isDaemon());
        assertTrue(!daemon.isInterrupted());
        daemon.interrupt();
    }
    
    @Test
    public void testLaunchDaemon() throws InterruptedException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
        daemon.setDaemon(true);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        assertFalse(daemon.isAlive());
        daemon.interrupt();
    }
    
    @Test
    public void testDetectNoChanges() throws InterruptedException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        assertEquals(0, daemon.getChanges());
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileAdd() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(tempFile, "temp data");
        Thread.sleep(TESTSLEEP);
        assertEquals(4, daemon.getTree().getNumLeaves());
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.deleteQuietly(tempFile);
        Thread.sleep(TESTSLEEP);
        assertNotEquals(startingHash, endingHash);
        assertEquals(2, daemon.getChanges());
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileDelete() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.deleteQuietly(file1);
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertNotEquals(startingHash, endingHash);
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileChange() throws InterruptedException, IOException {
        FileUtils.write(file1, "testData");
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(file1, "file 1 modified");
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertNotEquals(startingHash, endingHash);
        FileUtils.deleteQuietly(file1);
        daemon.interrupt();
    }
    
    @Test
    public void testDetectDirectoryAdd() throws IOException, InterruptedException {
        //String subDir = daemonDirPath + "/subdir";
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.forceMkdir(daemonSubDir);
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertEquals(startingHash, endingHash);
        assertEquals(1, daemon.getChanges());
        FileUtils.deleteQuietly(daemonSubDir); // TODO maybe don't need
        daemon.interrupt();
    }
    
    @Test
    public void testDetectDirectoryDelete() throws InterruptedException, IOException {
        FileUtils.forceMkdir(daemonSubDir);
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.deleteQuietly(daemonSubDir);
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertEquals(startingHash, endingHash);
        assertEquals(1, daemon.getChanges());
        daemon.interrupt();
    }
    
    @Test
    public void testSubDirectoryRecursive() throws IOException, InterruptedException {
        FileUtils.forceMkdir(daemonSubDir);
        FileUtils.write(subDirFile, "sub dir file data");
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, true), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(subDirFile, "sub dir file modified data");
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertNotEquals(startingHash, endingHash);
        assertEquals(1, daemon.getChanges());
        daemon.interrupt();
    }
    
    @Test
    public void testSubDirectoryNonRecursive() throws IOException, InterruptedException {
    	FileUtils.forceMkdir(daemonSubDir);
        FileUtils.write(subDirFile, "sub dir file data");
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        FileUtils.write(subDirFile, "sub dir file modified data");
        Thread.sleep(TESTSLEEP);
        assertEquals(0, daemon.getChanges());
        daemon.interrupt();
    }
    
    @Test
    public void testTwoSubDirectoriesRecursive() throws IOException, InterruptedException {
    	FileUtils.forceMkdir(daemonSubDir);
        FileUtils.write(subDirFile, "sub dir file data");
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, true), DAEMONPERIOD);
        FileUtils.forceMkdir(new File(daemonSubDir, "subSubDir"));
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(new File(daemonSubDir, "subSubDir/subSubFile"), "sub sub dir file data");
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertNotEquals(startingHash, endingHash);
        assertEquals(1, daemon.getChanges());
        daemon.interrupt();
    }
    
    @Test
    public void testDaemonFileLogging() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(tempFile, "temp data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(tempFile, "new data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(tempFile);
    	Thread.sleep(TESTSLEEP);
    	Log log = daemon.getLog();
    	ArrayList<LogEntry> entries = log.getLog();
    	assertEquals(3, entries.size());
    	assertTrue(entries.get(0).message.contains(MonitorEvent.FCREATE.toString()));
    	assertTrue(entries.get(1).message.contains(MonitorEvent.FCHANGE.toString()));
    	assertTrue(entries.get(2).message.contains(MonitorEvent.FDELETE.toString()));
    	daemon.interrupt();
    }
    
    @Test
    public void testDaemonDirLoggingRecursive() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, true), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.forceMkdir(daemonSubDir);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(subDirFile, "data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(daemonSubDir);
    	Thread.sleep(TESTSLEEP);
    	Log log = daemon.getLog();
    	ArrayList<LogEntry> entries = log.getLog();
    	assertEquals(4, entries.size());
    	assertTrue(entries.get(0).message.contains(MonitorEvent.DCREATE.toString()));
    	assertTrue(entries.get(1).message.contains(MonitorEvent.FCREATE.toString()));
    	assertTrue(entries.get(2).message.contains(MonitorEvent.FDELETE.toString()));
    	assertTrue(entries.get(3).message.contains(MonitorEvent.DDELETE.toString()));
    	daemon.interrupt();
    }
    
    @Test
    public void testDaemonDirLoggingNonRecursive() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDir, false), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.forceMkdir(daemonSubDir);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(subDirFile, "data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(daemonSubDir);
    	Thread.sleep(TESTSLEEP);
    	Log log = daemon.getLog();
    	ArrayList<LogEntry> entries = log.getLog();
    	for (LogEntry entry : entries) {
    		System.out.println(entry.toString());
    	}
    	assertEquals(2, entries.size());
    	assertTrue(entries.get(0).message.contains(MonitorEvent.DCREATE.toString()));
    	assertTrue(entries.get(1).message.contains(MonitorEvent.DDELETE.toString()));
    	daemon.interrupt();
    }
}
