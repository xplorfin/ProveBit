package org.provebit.daemon;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.provebit.daemon.DirectoryMonitor.MonitorEvent;
import org.provebit.daemon.Log.LogEntry;
import org.provebit.merkle.Merkle;

public class DaemonTests {
    static String DAEMONDIR = "/src/test/java/org/provebit/daemon/testDaemonDir";
    static String DAEMONSUBDIR = "/src/test/java/org/provebit/daemon/testDaemonDir/subDir";
    static String daemonDirPath;
    static String daemonSubDirPath;
    static File file1;
    static File file2;
    static File tempFile;
    static File subDirFile;
    static final int TESTSLEEP = 100;
    static final int DAEMONPERIOD = 50;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        daemonDirPath = new java.io.File( "." ).getCanonicalPath() + DAEMONDIR;
        daemonSubDirPath = new java.io.File( "." ).getCanonicalPath() + DAEMONSUBDIR;
        file1 = new File(daemonDirPath + "/file1");
        file2 = new File(daemonDirPath + "/file2");
        tempFile = new File(daemonDirPath + "/tempfile");
        subDirFile = new File(daemonSubDirPath + "/subDirTempFile");
        resetTestDirectory();
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws IOException {
    	resetTestDirectory();
    }
    
    /**
     * Resets test directory to default state (two files with known file contents)
     * @throws IOException
     */
    public static void resetTestDirectory() throws IOException {
    	for (File file : new File(daemonDirPath).listFiles()) {
    		if (!(file.getName().compareTo(file1.getName()) == 0) || !(file.getName().compareTo(file2.getName()) == 0)) {
    			if (file.isDirectory()) {
    				FileUtils.deleteDirectory(file);
    			} else {
    				FileUtils.deleteQuietly(file);
    			}
    		}
    	}
    	
    	FileUtils.write(file1, "file 1");
    	FileUtils.write(file2, "file 2");
    }
    
    @Before
	public void setUp() throws Exception {
    	resetTestDirectory();
	}
    
    @Test
    public void testLaunchDaemon() {
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
        assertTrue(daemon.isDaemon());
        assertTrue(!daemon.isInterrupted());
        daemon.interrupt();
    }
    
    @Test
    public void testLaunchNotDaemon() throws InterruptedException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
        daemon.setDaemon(false);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        assertTrue(!daemon.isAlive());
        daemon.interrupt();
    }
    
    @Test
    public void testDetectNoChanges() throws InterruptedException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        assertTrue(daemon.getChanges() == 0);
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileAdd() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(tempFile, "temp data");
        Thread.sleep(TESTSLEEP);
        assertTrue(daemon.getTree().getNumLeaves() == 4);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.deleteQuietly(tempFile);
        Thread.sleep(TESTSLEEP);
        assertTrue(startingHash.compareTo(endingHash) != 0);
        assertTrue(daemon.getChanges() == 2);
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileDelete() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.deleteQuietly(file1);
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertTrue(startingHash.compareTo(endingHash) != 0);
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileChange() throws InterruptedException, IOException {
        FileUtils.write(file1, "testData");
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(file1, "file 1 modified");
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertTrue(startingHash.compareTo(endingHash) != 0);
        FileUtils.deleteQuietly(file1);
        daemon.interrupt();
    }
    
    @Test
    public void testDetectDirectoryAdd() throws IOException, InterruptedException {
        String subDir = daemonDirPath + "/subdir";
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.forceMkdir(new File(subDir));
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertTrue(startingHash.compareTo(endingHash) == 0);
        assertTrue(daemon.getChanges() == 1);
        FileUtils.deleteQuietly(new File(subDir));
        daemon.interrupt();
    }
    
    @Test
    public void testDetectDirectoryDelete() throws InterruptedException, IOException {
        FileUtils.forceMkdir(new File(daemonSubDirPath));
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.deleteQuietly(new File(daemonSubDirPath));
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertTrue(startingHash.compareTo(endingHash) == 0);
        assertTrue(daemon.getChanges() == 1);
        daemon.interrupt();
    }
    
    @Test
    public void testSubDirectoryRecursive() throws IOException, InterruptedException {
        FileUtils.forceMkdir(new File(daemonSubDirPath));
        FileUtils.write(subDirFile, "sub dir file data");
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, true), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(subDirFile, "sub dir file modified data");
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertTrue(startingHash.compareTo(endingHash) != 0);
        assertTrue(daemon.getChanges() == 1);
        daemon.interrupt();
    }
    
    @Test
    public void testSubDirectoryNonRecursive() throws IOException, InterruptedException {
    	FileUtils.forceMkdir(new File(daemonSubDirPath));
        FileUtils.write(subDirFile, "sub dir file data");
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        FileUtils.write(subDirFile, "sub dir file modified data");
        Thread.sleep(TESTSLEEP);
        assertTrue(daemon.getChanges() == 0);
        daemon.interrupt();
    }
    
    @Test
    public void testTwoSubDirectoriesRecursive() throws IOException, InterruptedException {
    	FileUtils.forceMkdir(new File(daemonSubDirPath));
        FileUtils.write(subDirFile, "sub dir file data");
        MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, true), DAEMONPERIOD);
        FileUtils.forceMkdir(new File(daemonSubDirPath + "/subSubDir"));
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(new File(daemonSubDirPath + "/subSubDir/subSubFile"), "sub sub dir file data");
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertTrue(startingHash.compareTo(endingHash) != 0);
        assertTrue(daemon.getChanges() == 1);
        daemon.interrupt();
    }
    
    @Test
    public void testDaemonFileLogging() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
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
    	assertTrue(entries.size() == 3);
    	assertTrue(entries.get(0).message.contains(MonitorEvent.FCREATE.toString()));
    	assertTrue(entries.get(1).message.contains(MonitorEvent.FCHANGE.toString()));
    	assertTrue(entries.get(2).message.contains(MonitorEvent.FDELETE.toString()));
    	daemon.interrupt();
    }
    
    @Test
    public void testDaemonDirLoggingRecursive() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, true), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.forceMkdir(new File(daemonSubDirPath));
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(subDirFile, "data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(new File(daemonSubDirPath));
    	Thread.sleep(TESTSLEEP);
    	Log log = daemon.getLog();
    	ArrayList<LogEntry> entries = log.getLog();
    	assertTrue(entries.size() == 4);
    	assertTrue(entries.get(0).message.contains(MonitorEvent.DCREATE.toString()));
    	assertTrue(entries.get(1).message.contains(MonitorEvent.FCREATE.toString()));
    	assertTrue(entries.get(2).message.contains(MonitorEvent.FDELETE.toString()));
    	assertTrue(entries.get(3).message.contains(MonitorEvent.DDELETE.toString()));
    	daemon.interrupt();
    }
    
    @Test
    public void testDaemonDirLoggingNonRecursive() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new Merkle(daemonDirPath, false), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.forceMkdir(new File(daemonSubDirPath));
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(subDirFile, "data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(new File(daemonSubDirPath));
    	Thread.sleep(TESTSLEEP);
    	Log log = daemon.getLog();
    	ArrayList<LogEntry> entries = log.getLog();
    	for (LogEntry entry : entries) {
    		System.out.println(entry.toString());
    	}
    	assertTrue(entries.size() == 2);
    	assertTrue(entries.get(0).message.contains(MonitorEvent.DCREATE.toString()));
    	assertTrue(entries.get(1).message.contains(MonitorEvent.DDELETE.toString()));
    	daemon.interrupt();
    }
}
