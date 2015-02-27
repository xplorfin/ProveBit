package org.provebit.daemon;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.provebit.daemon.Log.LogEntry;

public class LogTests {
	static String TESTDIR = "/src/test/java/org/provebit/daemon/testLogDir";
	static String testDirPath;
	static String[] messages = {"msg0", "msg1", "msg2", "msg3"};
	static File logFile;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testDirPath = new java.io.File( "." ).getCanonicalPath() + TESTDIR;
		logFile = new File(testDirPath + "/file.log");
		clearDirectory();
	}
	
	private static void clearDirectory() throws IOException {
		for (File file : new File(testDirPath).listFiles()) {
    		FileUtils.deleteQuietly(file);
    	}
	}

	@After
	public void tearDown() throws Exception {
		clearDirectory();
	}

	@Test
	public void testLogAddSimple() {
		Log log = new Log();
		log.addEntry("simple test");
		ArrayList<LogEntry> entries = log.getLog();
		assertTrue(entries.get(0).getMessage().compareTo("simple test") == 0);
	}
	
	@Test
	public void testLogAddMultiple() {
		Log log = new Log();
		for (String msg : messages) {
			log.addEntry(msg);
		}
		
		ArrayList<LogEntry> entries = log.getLog();
		for (int i = 0; i < messages.length; i++) {
			assertTrue(entries.get(i).getMessage().compareTo(messages[i]) == 0);
		}
	}
	
	@Test
	public void testLogToFileSimple() throws IOException {
		Log log = new Log();
		log.addEntry("simple test");
		log.setLogFile(logFile);
		log.writeToFile();
		log.endLog();
		ArrayList<LogEntry> entries = log.getLog();
		assertTrue(entries.get(0).getMessage().compareTo("simple test") == 0);
	}

	@Test
	public void testLogToFileMultiple() throws IOException {
		Log log = new Log(logFile);
		for (String msg : messages) {
			log.addEntry(msg);
		}
		log.writeToFile();
		log.endLog();
		ArrayList<LogEntry> entries = log.getLog();
		for (int i = 0; i < messages.length; i++) {
			assertTrue(entries.get(i).getMessage().compareTo(messages[i]) == 0);
		}
	}
	
	@Test
	public void testLogRecovery() throws IOException {
		Log logOriginal = new Log(logFile);
		Log logRecovery;
		for (String msg : messages) {
			logOriginal.addEntry(msg);
		}
		logOriginal.writeToFile();
		logOriginal.endLog();
		
		logRecovery = new Log();
		if (!logRecovery.recoverLog(logFile)) {
			fail();
		}
		ArrayList<LogEntry> entries = logRecovery.getLog();
		for (int i = 0; i < messages.length; i++) {
			assertTrue(entries.get(i).getMessage().compareTo(messages[i]) == 0);
		}
	}
	
	@Test
	public void testLogTimestamp() {
		Log log = new Log();
		Timestamp current = new Timestamp(new Date().getTime());
		log.addEntry("simple test");
		
		LogEntry entry = log.getLog().get(0);
		assertTrue(current.equals(entry.getTime()));
	}
	
	@Test
	public void testLogTimestampRecovery() throws IOException {
		Log logOriginal = new Log(logFile);
		Log logRecovery;
		Timestamp startTime = new Timestamp(new Date().getTime());
		ArrayList<Timestamp> messageTimes = new ArrayList<Timestamp>();
		for (String msg : messages) {
			messageTimes.add(new Timestamp(new Date().getTime()));
			logOriginal.addEntry(msg);
		}
		logOriginal.writeToFile();
		logOriginal.endLog();
		
		logRecovery = new Log();
		if (!logRecovery.recoverLog(logFile)) {
			fail();
		}
		
		for (int i = 0; i < messageTimes.size(); i++) {
			LogEntry entry = logRecovery.getLog().get(i);
			assertTrue(startTime.before(entry.getTime()) || startTime.equals(entry.getTime()));
		}
	}
}
