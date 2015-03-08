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
import org.provebit.daemon.FileMonitor.MonitorEvent;
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
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, false);
        MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        assertTrue(!daemon.isDaemon());
        assertTrue(!daemon.isInterrupted());
        daemon.interrupt();
    }
    
    @Test
    public void testLaunchDaemon() throws InterruptedException {
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        daemon.setDaemon(true);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        assertFalse(daemon.isAlive());
        daemon.interrupt();
    }
    
    @Test
    public void testDetectNoChanges() throws InterruptedException {
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        assertEquals(0, daemon.getEvents());
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileAdd() throws InterruptedException, IOException {
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
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
        assertEquals(2, daemon.getEvents());
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileDelete() throws InterruptedException, IOException {
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
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
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, false);
        FileUtils.write(file1, "testData");
        MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
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
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, true);
        MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        FileUtils.forceMkdir(daemonSubDir);
        Thread.sleep(TESTSLEEP);
        assertTrue(daemon.getLog().toString().contains(MonitorEvent.DCREATE.toString()));
        daemon.interrupt();
    }
    
    @Test
    public void testDetectDirectoryDelete() throws InterruptedException, IOException {
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, true);
        FileUtils.forceMkdir(daemonSubDir);
        MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        FileUtils.deleteQuietly(daemonSubDir);
        Thread.sleep(TESTSLEEP);
        assertTrue(daemon.getLog().toString().contains(MonitorEvent.DDELETE.toString()));
        assertEquals(1, daemon.getEvents());
        daemon.interrupt();
    }
    
    @Test
    public void testSubDirectoryRecursive() throws IOException, InterruptedException {
        FileUtils.forceMkdir(daemonSubDir);
        Merkle m = new Merkle();
    	m.addTracking(daemonDir, true);
        FileUtils.write(subDirFile, "sub dir file data");
        MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(subDirFile, "sub dir file modified data");
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertNotEquals(startingHash, endingHash);
        assertEquals(1, daemon.getEvents());
        daemon.interrupt();
    }
    
    @Test
    public void testSubDirectoryNonRecursive() throws IOException, InterruptedException {
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, false);
    	FileUtils.forceMkdir(daemonSubDir);
        FileUtils.write(subDirFile, "sub dir file data");
        MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        FileUtils.write(subDirFile, "sub dir file modified data");
        Thread.sleep(TESTSLEEP);
        assertEquals(0, daemon.getEvents());
        daemon.interrupt();
    }
    
    @Test
    public void testTwoSubDirectoriesRecursive() throws IOException, InterruptedException {
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, true);
    	FileUtils.forceMkdir(daemonSubDir);
        FileUtils.write(subDirFile, "sub dir file data");
        MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        FileUtils.forceMkdir(new File(daemonSubDir, "subSubDir"));
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(new File(daemonSubDir, "subSubDir/subSubFile"), "sub sub dir file data");
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertNotEquals(startingHash, endingHash);
        assertTrue(daemon.getEvents() >= 1);
        daemon.interrupt();
    }
    
    @Test
    public void testDaemonFileLogging() throws InterruptedException, IOException {
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
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
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, true);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.forceMkdir(daemonSubDir);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(subDirFile, "data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(subDirFile);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(daemonSubDir);
    	Thread.sleep(TESTSLEEP);
    	Log log = daemon.getLog();
    	ArrayList<LogEntry> entries = log.getLog();
    	System.out.println(log.toString());
    	assertTrue(entries.size() >= 4);
    	assertTrue(log.toString().contains(MonitorEvent.DCREATE.toString()));
    	assertTrue(log.toString().contains(MonitorEvent.FCREATE.toString()));
    	assertTrue(log.toString().contains(MonitorEvent.DDELETE.toString()));
    	assertTrue(log.toString().contains(MonitorEvent.FDELETE.toString()));
    	daemon.interrupt();
    }
    
    @Test
    public void testDaemonDirLoggingNonRecursive() throws InterruptedException, IOException {
    	Merkle m = new Merkle();
    	m.addTracking(daemonDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.forceMkdir(daemonSubDir);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(subDirFile, "data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(daemonSubDir);
    	Thread.sleep(TESTSLEEP);
    	assertEquals(0, daemon.getLog().getNumEntries());
    	daemon.interrupt();
    }
    
    @Test
    public void testDaemonMultipleFileLogging() throws InterruptedException, IOException {
    	Merkle m = new Merkle();
    	m.addTracking(file1, false);
    	m.addTracking(file2, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(file1, "new data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(file2, "new data 2");
    	Thread.sleep(TESTSLEEP);
    	assertEquals(2, daemon.getEvents());
    	daemon.interrupt();
    }
    
    @Test
    public void testDaemonMultipleDirLogging() throws InterruptedException, IOException {
    	Merkle m = new Merkle();
    	FileUtils.forceMkdir(daemonSubDir);
    	m.addTracking(daemonDir, false);
    	m.addTracking(daemonSubDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(file1, "data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(subDirFile, "data 2");
    	Thread.sleep(TESTSLEEP);
    	assertEquals(2, daemon.getEvents());
    	daemon.interrupt();
    }
    
    @Test
    public void testDaemonDisjointFileDirLogging() throws IOException, InterruptedException {
    	Merkle m = new Merkle();
    	FileUtils.forceMkdir(daemonSubDir);
    	m.addTracking(file1, false);
    	m.addTracking(daemonSubDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(file2, "new data");
    	Thread.sleep(TESTSLEEP);
    	System.out.println(daemon.getLog().toString());
    	assertEquals(0, daemon.getEvents());
    	daemon.interrupt();
    }
}
