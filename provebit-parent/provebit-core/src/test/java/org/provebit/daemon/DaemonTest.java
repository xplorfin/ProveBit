package org.provebit.daemon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.provebit.daemon.DaemonProtocol.DaemonMessage;
import org.provebit.daemon.DaemonProtocol.DaemonMessage.DaemonMessageType;
import org.provebit.daemon.FileMonitor.MonitorEvent;
import org.provebit.daemon.Log.LogEntry;
import org.provebit.merkle.FileMerkle;
import org.provebit.merkle.HashType;
import org.simplesockets.client.SimpleClient;

public class DaemonTest {
    public File daemonDir;
    public File daemonSubDir;
    public File file1;
    public File file2;
    public File tempFile;
    public File subDirFile;
    public final int TESTSLEEP = 100;
    public final int DAEMONPERIOD = 50;
    public DaemonProtocol clientProtocol;
    public final String hostname = "localhost";
    
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
    	clientProtocol = new DaemonProtocol() {
    		@Override
			public DaemonMessage handleMessage(DaemonMessage request) {
				return request;
			}
    	};
    }
    
    @Test
    public void testLaunch() throws InterruptedException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
    	m.addTracking(daemonDir, false);
        MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        assertTrue(!daemon.isDaemon());
        assertTrue(!daemon.isInterrupted());
        daemon.interrupt();
        daemon.join();
    }
    
    @Test
    public void testLaunchDaemon() throws InterruptedException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
    	m.addTracking(daemonDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        daemon.setDaemon(true);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        assertFalse(daemon.isAlive());
        daemon.interrupt();
        daemon.join();
    }
    
    @Test
    public void testDetectNoChanges() throws InterruptedException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
    	m.addTracking(daemonDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        assertEquals(0, daemon.getEvents());
        daemon.interrupt();
        daemon.join();
    }
    
    @Test
    public void testDetectFileAdd() throws InterruptedException, IOException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
        daemon.join();
    }
    
    @Test
    public void testDetectFileDelete() throws InterruptedException, IOException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
        daemon.join();
    }
    
    @Test
    public void testDetectFileChange() throws InterruptedException, IOException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
        daemon.join();
    }
    
    @Test
    public void testDetectDirectoryAdd() throws IOException, InterruptedException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
    	m.addTracking(daemonDir, true);
        MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
        daemon.start();
        Thread.sleep(TESTSLEEP);
        FileUtils.forceMkdir(daemonSubDir);
        Thread.sleep(TESTSLEEP);
        assertTrue(daemon.getLog().toString().contains(MonitorEvent.DCREATE.toString()));
        daemon.interrupt();
        daemon.join();
    }
    
    @Test
    public void testDetectDirectoryDelete() throws InterruptedException, IOException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
        daemon.join();
    }
    
    @Test
    public void testSubDirectoryRecursive() throws IOException, InterruptedException {
        FileUtils.forceMkdir(daemonSubDir);
        FileMerkle m = new FileMerkle(HashType.SHA256);
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
        daemon.join();
    }
    
    @Test
    public void testSubDirectoryNonRecursive() throws IOException, InterruptedException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
        daemon.join();
    }
    
    @Test
    public void testTwoSubDirectoriesRecursive() throws IOException, InterruptedException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
        daemon.join();
    }
    
    @Test
    public void testDaemonFileLogging() throws InterruptedException, IOException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
    	Log log = daemon.getLogActual();
    	assertTrue(log.toString().contains(MonitorEvent.FCREATE.toString()));
    	assertTrue(log.toString().contains(MonitorEvent.FCHANGE.toString()));
    	assertTrue(log.toString().contains(MonitorEvent.FDELETE.toString()));
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonDirLoggingRecursive() throws InterruptedException, IOException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
    	Log log = daemon.getLogActual();
    	ArrayList<LogEntry> entries = log.getLog();
    	assertTrue(entries.size() >= 4);
    	assertTrue(log.toString().contains(MonitorEvent.DCREATE.toString()));
    	assertTrue(log.toString().contains(MonitorEvent.FCREATE.toString()));
    	assertTrue(log.toString().contains(MonitorEvent.DDELETE.toString()));
    	assertTrue(log.toString().contains(MonitorEvent.FDELETE.toString()));
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonDirLoggingNonRecursive() throws InterruptedException, IOException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
    	assertEquals(0, daemon.getLogActual().getNumEntries());
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonMultipleFileLogging() throws InterruptedException, IOException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
    	daemon.join();
    }
    
    @Test
    public void testDaemonMultipleDirLogging() throws InterruptedException, IOException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
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
    	daemon.join();
    }
    
    @Test
    public void testDaemonDisjointFileDirLogging() throws IOException, InterruptedException {
    	FileMerkle m = new FileMerkle(HashType.SHA256);
    	FileUtils.forceMkdir(daemonSubDir);
    	m.addTracking(file1, false);
    	m.addTracking(daemonSubDir, false);
    	MerkleDaemon daemon = new MerkleDaemon(m, DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(file2, "new data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(subDirFile, "data");
    	Thread.sleep(TESTSLEEP);
    	assertEquals(1, daemon.getEvents());
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonNetworkHeartBeat() throws InterruptedException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	DaemonMessage heartbeat = new DaemonMessage(DaemonMessageType.HEARTBEAT, null);
    	client.sendRequest(heartbeat);
    	Thread.sleep(TESTSLEEP);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	assertNotNull(reply);
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonNetworkStartStopSuccess() throws InterruptedException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	DaemonMessage request = new DaemonMessage(DaemonMessageType.START, null);
    	client.sendRequest(request);
    	Thread.sleep(TESTSLEEP);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	assertNotNull(reply);
    	request = new DaemonMessage(DaemonMessageType.SUSPEND, null);
    	client.sendRequest(request);
    	Thread.sleep(TESTSLEEP);
    	reply = (DaemonMessage) client.getReply();
    	assertNotNull(reply);
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonNetworkKill() throws InterruptedException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	DaemonMessage heartbeat = new DaemonMessage(DaemonMessageType.HEARTBEAT, null);
    	client.sendRequest(heartbeat);
    	Thread.sleep(TESTSLEEP);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	assertNotNull(reply);
    	DaemonMessage kill = new DaemonMessage(DaemonMessageType.KILL, null);
    	client.sendRequest(kill);
    	Thread.sleep(TESTSLEEP);
    	client.sendRequest(heartbeat);
    	assertNull(client.getReply());
    }
    
    @Test
    public void testDaemonNetworkAddFiles() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	Map<String, Boolean> fileList = new HashMap<String, Boolean>();
    	fileList.put(daemonDir.getAbsolutePath(), false);
    	DaemonMessage request = new DaemonMessage(DaemonMessageType.ADDFILES, fileList);
    	client.sendRequest(request);
    	assertNotNull(client.getReply());
    	FileUtils.write(file1, "modified stuff");
    	Thread.sleep(TESTSLEEP);
    	DaemonMessage getLog = new DaemonMessage(DaemonMessageType.GETLOG, null);
    	client.sendRequest(getLog);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	String logData = (String) reply.data;
    	assertTrue(logData.contains("FCHANGE : " + file1.getAbsolutePath()));
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonNetworkRemoveFiles() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	Map<String, Boolean> fileMap = new HashMap<String, Boolean>();
    	fileMap.put(file1.getAbsolutePath(), false);
    	DaemonMessage request = new DaemonMessage(DaemonMessageType.ADDFILES, fileMap);
    	client.sendRequest(request);
    	assertNotNull(client.getReply());
    	FileUtils.write(file1, "modified stuff");
    	Thread.sleep(TESTSLEEP);
    	DaemonMessage getLog = new DaemonMessage(DaemonMessageType.GETLOG, null);
    	client.sendRequest(getLog);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	String logData = (String) reply.data;
    	assertTrue(logData.contains("FCHANGE : " + file1.getAbsolutePath()));
    	List<String> fileList = new ArrayList<String>();
    	fileList.add(file1.getAbsolutePath());
    	DaemonMessage removeRequest = new DaemonMessage(DaemonMessageType.REMOVEFILES, fileList);
    	client.sendRequest(removeRequest);
    	assertNotNull(client.getReply());
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(file1);
    	client.sendRequest(getLog);
    	reply = (DaemonMessage) client.getReply();
    	logData = (String) reply.data;
    	assertTrue(!logData.contains("FDELETE"));
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonNetworkSetPeriod() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	Map<String, Boolean> fileMap = new HashMap<String, Boolean>();
    	fileMap.put(daemonDir.getAbsolutePath(), true);
    	DaemonMessage addFilesRequest = new DaemonMessage(DaemonMessageType.ADDFILES, fileMap);
    	client.sendRequest(addFilesRequest);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(file1, "set period network test");
    	Thread.sleep(TESTSLEEP);
    	DaemonMessage getLogRequest = new DaemonMessage(DaemonMessageType.GETLOG, null);
    	client.sendRequest(getLogRequest);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	String log = (String) reply.data;
    	assertTrue(log.contains("FCHANGE"));
    	DaemonMessage changePeriodRequest = new DaemonMessage(DaemonMessageType.SETPERIOD, 100*DAEMONPERIOD);
    	client.sendRequest(changePeriodRequest);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(file2);
    	Thread.sleep(TESTSLEEP);
    	client.sendRequest(getLogRequest);
    	reply = (DaemonMessage) client.getReply();
    	log = (String) reply.data;
    	assertTrue(!log.contains("FDELETE"));
    }
    
    @Test
    public void testDaemonNetworkGetLog() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	Map<String,Boolean> addFileMap = new HashMap<String, Boolean>();
    	addFileMap.put(daemonDir.getAbsolutePath(), true);
    	DaemonMessage addFileRequest = new DaemonMessage(DaemonMessageType.ADDFILES, addFileMap);
    	client.sendRequest(addFileRequest);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.forceMkdir(daemonSubDir);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(subDirFile, "data");
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(subDirFile);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.deleteQuietly(daemonSubDir);
    	Thread.sleep(TESTSLEEP);
    	DaemonMessage getLogRequest = new DaemonMessage(DaemonMessageType.GETLOG, null);
    	client.sendRequest(getLogRequest);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	String log = (String) reply.data;
    	assertTrue(log.contains(MonitorEvent.DCREATE.toString()));
    	assertTrue(log.contains(MonitorEvent.FCREATE.toString()));
    	assertTrue(log.contains(MonitorEvent.DDELETE.toString()));
    	assertTrue(log.contains(MonitorEvent.FDELETE.toString()));
    	daemon.interrupt();
    	daemon.join();
    }
    
    @SuppressWarnings("unchecked")
	public void testDaemonNetworkGetTracked() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	Map<String, Boolean> fileList = new HashMap<String, Boolean>();
    	fileList.put(file1.getAbsolutePath(), false);
    	fileList.put(file2.getAbsolutePath(), false);
    	fileList.put(daemonSubDir.getAbsolutePath(), true);
    	DaemonMessage request = new DaemonMessage(DaemonMessageType.ADDFILES, fileList);
    	client.sendRequest(request);
    	assertNotNull(client.getReply());
    	DaemonMessage getTrackedRequest = new DaemonMessage(DaemonMessageType.GETTRACKED, null);
    	Thread.sleep(TESTSLEEP);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	List<List<String>> trackedFiles = (List<List<String>>) reply.data;
    	assertTrue(trackedFiles.get(0).size() == 2);
    	assertTrue(trackedFiles.get(1).size() == 1);
    	List<String> removeList = new ArrayList<String>();
    	removeList.add(file2.getAbsolutePath());
    	DaemonMessage removeFileRequest = new DaemonMessage(DaemonMessageType.REMOVEFILES, removeList);
    	client.sendRequest(removeFileRequest);
    	Thread.sleep(TESTSLEEP);
    	client.sendRequest(getTrackedRequest);
    	reply = (DaemonMessage) client.getReply();
    	trackedFiles = (List<List<String>>) reply.data;
    	assertTrue(trackedFiles.get(0).size() == 1);
    	assertTrue(trackedFiles.get(1).size() == 1);
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonNetworkIsTracked() throws InterruptedException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	Map<String, Boolean> fileMap = new HashMap<String, Boolean>();
    	fileMap.put(file1.getAbsolutePath(), true);
    	DaemonMessage addFileRequest = new DaemonMessage(DaemonMessageType.ADDFILES, fileMap);
    	client.sendRequest(addFileRequest);
    	DaemonMessage isFile1Tracked = new DaemonMessage(DaemonMessageType.ISTRACKED, file1.getAbsolutePath());
    	DaemonMessage isFile2Tracked = new DaemonMessage(DaemonMessageType.ISTRACKED, file2.getAbsolutePath());
    	client.sendRequest(isFile1Tracked);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	assertTrue((boolean) reply.data);
    	client.sendRequest(isFile2Tracked);
    	reply = (DaemonMessage) client.getReply();
    	assertFalse((boolean) reply.data);
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonNetworkGetState() throws InterruptedException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	DaemonMessage getStateRequest = new DaemonMessage(DaemonMessageType.GETSTATE, null);
    	client.sendRequest(getStateRequest);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	assertEquals((int) reply.data, 1);
    	DaemonMessage suspendRequest = new DaemonMessage(DaemonMessageType.SUSPEND, null);
    	client.sendRequest(suspendRequest);
    	Thread.sleep(TESTSLEEP);
    	client.sendRequest(getStateRequest);
    	reply = (DaemonMessage) client.getReply();
    	assertEquals((int) reply.data, 0);
    	daemon.interrupt();
    	daemon.join();
    }
    
    @Test
    public void testDaemonNetworkReset() throws InterruptedException, IOException {
    	MerkleDaemon daemon = new MerkleDaemon(new FileMerkle(HashType.SHA256), DAEMONPERIOD);
    	daemon.start();
    	Thread.sleep(TESTSLEEP);
    	SimpleClient client = new SimpleClient(hostname, daemon.getPort(), clientProtocol);
    	Map<String, Boolean> fileMap = new HashMap<String, Boolean>();
    	fileMap.put(file1.getAbsolutePath(), true);
    	DaemonMessage addFileRequest = new DaemonMessage(DaemonMessageType.ADDFILES, fileMap);
    	client.sendRequest(addFileRequest);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(file1, "data to see");
    	Thread.sleep(TESTSLEEP);
    	DaemonMessage getLog = new DaemonMessage(DaemonMessageType.GETLOG, null);
    	client.sendRequest(getLog);
    	DaemonMessage reply = (DaemonMessage) client.getReply();
    	assertTrue(((String)reply.data).contains("FCHANGE"));
    	DaemonMessage reset = new DaemonMessage(DaemonMessageType.RESET, null);
    	client.sendRequest(reset);
    	Thread.sleep(TESTSLEEP);
    	FileUtils.write(file1, "new data to not see");
    	client.sendRequest(getLog);
    	reply = (DaemonMessage) client.getReply();
    	assertFalse(((String)reply.data).contains("FCHANGE"));
    	daemon.interrupt();
    	daemon.join();
    }
}
