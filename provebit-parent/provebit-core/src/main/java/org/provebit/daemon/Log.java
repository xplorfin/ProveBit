package org.provebit.daemon;

import java.io.EOFException;
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
 * 
 * Add log entires using addEntry Flush log to file using writeLog Close log
 * using endLog
 */
public class Log {
	/**
	 * Simple log entry class
	 */
	public static class LogEntry implements Serializable {
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

	private ArrayList<LogEntry> entries;
	private File output;
	private FileOutputStream fOut;
	private ObjectOutputStream oOut;
	private int nextPending;
	private int numEntries;

	/**
	 * Simple log constructor that performs no I/O
	 */
	public Log() {
		entries = new ArrayList<LogEntry>();
		output = null;
		numEntries = 0;
	}

	/**
	 * Constructor for log that outputs to a file
	 * 
	 * @param outputFile
	 *            - File to write log to
	 * @throws IOException
	 */
	public Log(File outputFile) throws IOException {
		entries = new ArrayList<LogEntry>();
		setLogFile(outputFile);
		nextPending = 0;
	}

	/**
	 * Specify a file to write log to
	 * 
	 * @param output
	 *            - Log output file
	 * @throws IOException
	 */
	public void setLogFile(File output) throws IOException {
		if (output != null) {
			this.endLog();
		}
		this.output = output;
		fOut = new FileOutputStream(output);
		oOut = new ObjectOutputStream(fOut);
		nextPending = 0;
	}

	/**
	 * Write new log entry to log
	 * 
	 * @Note: Timestamps are automatically included for each log entry
	 * 
	 * @param message
	 *            - Message to write to log, does not flush to file
	 */
	public void addEntry(String message) {
		LogEntry entry = new LogEntry(message, new Timestamp(
				new Date().getTime()));
		entries.add(entry);
		numEntries++;
	}

	/**
	 * Writes all pending log entries to file
	 */
	public void writeToFile() {
		if (output != null) {
			while (nextPending < entries.size()) {
				doWrite(entries.get(nextPending));
				nextPending++;
			}
		} else {
			throw new RuntimeException("No file set for logging");
		}
	}

	/**
	 * Returns all entries in the current log
	 * 
	 * @return - ArrayList of log entries
	 */
	public ArrayList<LogEntry> getLog() {
		return entries;
	}

	/**
	 * Ends logging, only necessary if logging to a file Flushes any remaining
	 * log entries to file if file logging is being used
	 * 
	 * @throws IOException
	 */
	public void endLog() throws IOException {
		if (output != null) {
			if (nextPending != entries.size() - 1) {
				writeToFile();
			}
			oOut.close();
			fOut.close();
		}
		output = null;
	}
	
	public int getNumEntries() {
		return numEntries;
	}

	/**
	 * Recover existing log file and continue using it
	 * 
	 * @param input
	 *            - Existing log file to use
	 * @return true if recovery succeeds, false o/w
	 */
	public boolean recoverLog(File input) {
		entries = new ArrayList<LogEntry>();

		LogEntry entry;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					input));
			while ((entry = (LogEntry) ois.readObject()) != null) {
				entries.add(entry);
			}
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			if (e instanceof EOFException) {
				// Ignore, we finished reading all objects
			} else {
				System.out.println("Log recovery failed");
				e.printStackTrace();
				return false;
			}
		}
		numEntries = entries.size();
		
		return true;
	}

	/**
	 * Write new log entry to file
	 * 
	 * @param entry
	 *            - Entry to write to file
	 */
	private void doWrite(LogEntry entry) {
		if (output != null) {
			try {
				oOut.writeObject(entry);
			} catch (IOException e) {
				System.out.println("Log write failed");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Get all the log entries saved in a given file
	 * @param file - Existing log file to read
	 * @return ArrayList<LogEntry> of all entries found
	 */
	public ArrayList<LogEntry> readLogFile(File file) {
		ArrayList<LogEntry> fileEntries = new ArrayList<LogEntry>();

		LogEntry entry;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					file));
			while ((entry = (LogEntry) ois.readObject()) != null) {
				fileEntries.add(entry);
			}
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			if (e instanceof EOFException) {
				// Ignore, we finished reading all objects
			} else {
				System.out.println("Log recovery failed");
				e.printStackTrace();
				return null;
			}
		}
		return fileEntries;
	}

	public String toString() {
		return entriesToString(this.entries);
	}
	
	/**
	 * Static to string method to create a human readable form of a log entry list
	 * @param logEntries - list of log entries
	 * @return
	 */
	public static String entriesToString(ArrayList<LogEntry> logEntries) {
		String logAsString = "";
		for (LogEntry entry : logEntries) {
			logAsString += entry.toString() + "\n";
		}
		return logAsString;
	}
}
