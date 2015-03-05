package org.provebit.daemon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.provebit.daemon.Log.LogEntry;

public class LogTest {
	public static final long TIME_DIFF = 3000; // in ms
	public String testDirPath;
	public String[] messages = {"msg0", "msg1", "msg2", "msg3"};
	public File logFile;

    @Rule
    public TemporaryFolder daemonTemp = new TemporaryFolder();
    
    @Before
    public void setUp() {
    	logFile = new File(daemonTemp.getRoot(), "file.log");
    }

	@Test
	public void testLogAddSimple() {
		Log log = new Log();
		log.addEntry("simple test");
		ArrayList<LogEntry> entries = log.getLog();
		assertEquals("simple test", entries.get(0).getMessage());
	}
	
	@Test
	public void testLogAddMultiple() {
		Log log = new Log();
		for (String msg : messages) {
			log.addEntry(msg);
		}
		
		ArrayList<LogEntry> entries = log.getLog();
		for (int i = 0; i < messages.length; i++) {
			assertEquals(messages[i], entries.get(i).getMessage());
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
		assertEquals("simple test", entries.get(0).getMessage());
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
			assertEquals(messages[i], entries.get(i).getMessage());
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
			assertEquals(messages[i], entries.get(i).getMessage());
		}
	}
	
	@Test
	public void testLogTimestamp() {
		Log log = new Log();
		Timestamp current = new Timestamp(new Date().getTime());
		log.addEntry("simple test");
		
		LogEntry entry = log.getLog().get(0);
		long diff = entry.getTime().getTime() - current.getTime();
		assertTrue(diff > -TIME_DIFF && diff < TIME_DIFF); // make sure time is close
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
			long diff = entry.getTime().getTime() - startTime.getTime();
			assertTrue(diff > -TIME_DIFF && diff < TIME_DIFF); // make sure time is close
			//assertTrue(startTime.before(entry.getTime()) || startTime.equals(entry.getTime()));
		}
	}
}
