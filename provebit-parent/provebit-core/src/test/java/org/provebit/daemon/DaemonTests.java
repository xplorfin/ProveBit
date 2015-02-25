package org.provebit.daemon;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class DaemonTests {
    static String DAEMONDIR = "/src/test/java/org/provebit/daemon/testDaemonDir";
    static String daemonDirPath;
    static File tempFile1;
    static File tempFile2;
    static final int TESTSLEEP = 200;
    static final int DAEMONPERIOD = 50;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        daemonDirPath = new java.io.File( "." ).getCanonicalPath() + DAEMONDIR;
        tempFile1 = new File(daemonDirPath + "/tempfile1");
        tempFile2 = new File(daemonDirPath + "/tempfile2");
    }
    
    @Test
    public void testLaunchDaemon() {
        MerkleDaemon daemon = new MerkleDaemon(daemonDirPath, DAEMONPERIOD);
        assertTrue(daemon.isDaemon());
        assertTrue(!daemon.isInterrupted());
        daemon.interrupt();
    }
    
    @Test
    public void testLaunchNotDaemon() throws InterruptedException {
        MerkleDaemon daemon = new MerkleDaemon(daemonDirPath, DAEMONPERIOD);
        daemon.setDaemon(false);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        assertTrue(!daemon.isAlive());
        daemon.interrupt();
    }
    
    @Test
    public void testDetectNoChanges() throws InterruptedException {
        MerkleDaemon daemon = new MerkleDaemon(daemonDirPath, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        assertTrue(daemon.getChanges() == 0);
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileAdd() throws InterruptedException, IOException {
        MerkleDaemon daemon = new MerkleDaemon(daemonDirPath, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(tempFile1, "testData");
        Thread.sleep(TESTSLEEP);
        assertTrue(daemon.getTree().getNumLeaves() == 4);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.deleteQuietly(tempFile1);
        assertTrue(startingHash.compareTo(endingHash) != 0);
        assertTrue(daemon.getChanges() == 1);
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileDelete() throws InterruptedException, IOException {
        MerkleDaemon daemon = new MerkleDaemon(daemonDirPath, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(tempFile1, "testData");
        Thread.sleep(TESTSLEEP);
        String intermediateHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.deleteQuietly(tempFile1);
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertTrue(startingHash.compareTo(endingHash) == 0);
        assertTrue(endingHash.compareTo(intermediateHash) != 0);
        daemon.interrupt();
    }
    
    @Test
    public void testDetectFileChange() throws InterruptedException, IOException {
        FileUtils.write(tempFile1, "testData");
        MerkleDaemon daemon = new MerkleDaemon(daemonDirPath, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.write(tempFile1, "testData modified");
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertTrue(startingHash.compareTo(endingHash) != 0);
        FileUtils.deleteQuietly(tempFile1);
        daemon.interrupt();
    }
    
    @Test
    public void testDetectDirectoryAdd() throws IOException, InterruptedException {
        String subDir = daemonDirPath + "/subdir";
        MerkleDaemon daemon = new MerkleDaemon(daemonDirPath, DAEMONPERIOD);
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
        String subDir = daemonDirPath + "/subdir";
        FileUtils.forceMkdir(new File(subDir));
        MerkleDaemon daemon = new MerkleDaemon(daemonDirPath, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        String startingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        FileUtils.deleteQuietly(new File(subDir));
        Thread.sleep(TESTSLEEP);
        String endingHash = Hex.encodeHexString(daemon.getTree().getRootHash());
        assertTrue(startingHash.compareTo(endingHash) == 0);
        assertTrue(daemon.getChanges() == 1);
        daemon.interrupt();
    }
}
