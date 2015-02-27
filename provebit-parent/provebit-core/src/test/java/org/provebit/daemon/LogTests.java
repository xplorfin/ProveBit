package org.provebit.daemon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
		logFile = new File(testDirPath + "file.log");
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
		log.writeLog("simple test");
		ArrayList<LogEntry> entries = log.getLog();
		assertTrue(entries.get(0).getMessage().compareTo("simple test") == 0);
	}
	
	@Test
	public void testLogAddMultiple() {
		Log log = new Log();
		for (String msg : messages) {
			log.writeLog(msg);
		}
		
		ArrayList<LogEntry> entries = log.getLog();
		for (int i = 0; i < messages.length; i++) {
			assertTrue(entries.get(i).getMessage().compareTo(messages[i]) == 0);
		}
	}
	
	@Test
	public void testLogToFileSimple() {
		Log log = new Log(logFile);
		log.writeLog("simple test");
		ArrayList<LogEntry> entries = log.getLog();
		assertTrue(entries.get(0).getMessage().compareTo("simple test") == 0);
	}

	@Test
	public void testLogToFileMultiple() {
		Log log = new Log(logFile);
		for (String msg : messages) {
			log.writeLog(msg);
		}
		
		ArrayList<LogEntry> entries = log.getLog();
		for (int i = 0; i < messages.length; i++) {
			assertTrue(entries.get(i).getMessage().compareTo(messages[i]) == 0);
		}
	}
	
	@Test
	public void testLogRecovery() {
		Log logOriginal = new Log(logFile);
		Log logRecovery;
		for (String msg : messages) {
			logOriginal.writeLog(msg);
		}
		
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
		log.writeLog("simple test");
		
		LogEntry entry = log.getLog().get(0);
		assertTrue(current.before(entry.getTime()));
	}
	
	@Test
	public void testLogTimestampRecovery() {
		Log logOriginal = new Log(logFile);
		Log logRecovery;
		Timestamp startTime = new Timestamp(new Date().getTime());
		ArrayList<Timestamp> messageTimes = new ArrayList<Timestamp>();
		for (String msg : messages) {
			messageTimes.add(new Timestamp(new Date().getTime()));
			logOriginal.writeLog(msg);
		}
		
		logRecovery = new Log();
		if (!logRecovery.recoverLog(logFile)) {
			fail();
		}
		
		LogEntry last = logRecovery.getLog().get(0);
		assertTrue(startTime.before(last.getTime()) && messageTimes.get(0).before(last.getTime()));
		for (int i = 1; i < messageTimes.size(); i++) {
			LogEntry entry = logRecovery.getLog().get(i);
			assertTrue(startTime.before(entry.getTime()));
			assertTrue(messageTimes.get(i).before(entry.getTime()));
			last = entry;
		}
	}
}
