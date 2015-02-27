package org.provebit.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Logging helper class that handles all log-based operations
 */
public class Log {
	/**
	 * Simple log entry object
	 */
	public class LogEntry implements Serializable {
		private static final long serialVersionUID = 6144933582181634881L;
		String message;
		String timeString;
		
		public LogEntry(String message, Timestamp time) {
			this.timeString = time.toString();
			this.message = message;
		}
		
		public Timestamp getTime() {
			return Timestamp.valueOf(timeString);
		}
		
		public String getTimeString() {
			return timeString;
		}
		
		public String getMessage() {
			return message;
		}
		
		public String toString() {
			return "[" + timeString + "]: " + message;
		}
	}
	
	ArrayList<LogEntry> entries;
	File output;
	
	/**
	 * Simple log constructor that performs no I/O
	 */
	public Log() {
		entries = new ArrayList<LogEntry>();
		output = null;
	}
	
	/**
	 * Constructor for log that outputs to a file
	 * @param outputFile - File to write log to
	 */
	public Log(File outputFile) {
		entries = new ArrayList<LogEntry>();
		output = outputFile;
	}
	
	/**
	 * Write new log entry to log
	 * @Note: Timestamps are automatically included for each log entry
	 * 
	 * @param message - Message to write to log
	 */
	public void writeLog(String message) {
		LogEntry entry = new LogEntry(message, new Timestamp(new Date().getTime()));
		doWrite(entry);
	}
	
	/**
	 * Returns all entries in the current log
	 * @return - ArrayList of log entries
	 */
	public ArrayList<LogEntry> getLog() {
		return entries;
	}
	
	/**
	 * Recover existing log file and continue using it
	 * @param input - Existing log file to use
	 * @return true if recovery succeeds, false o/w
	 */
	public boolean recoverLog(File input) {
		entries = new ArrayList<LogEntry>();
		
		LogEntry entry;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(input));
			while ((entry = (LogEntry) ois.readObject()) != null) {
				entries.add(entry);
			}
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Log recovery failed");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Write new log entry to file
	 * @param entry - Entry to write to file
	 */
	private void doWrite(LogEntry entry) {
		entries.add(entry);
		if (output != null) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(output));
				oos.writeObject(entry);
				oos.close();
			} catch (IOException e) {
				System.out.println("Log write failed");
				e.printStackTrace();
			}
		}
	}
}
