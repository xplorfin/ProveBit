package org.provebit.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.apache.commons.io.FileUtils;

public class ServerUtils {
	private static File portFile = new File(ApplicationDirectory.INSTANCE.getRoot(), "daemon.port");
	private static FileChannel channel;
	private static FileLock lock;
	
	/**
	 * Write daemon port to application directory file
	 * @param port - port to write to file
	 */
	public static void writePort(int port) {
		getLock();
		try {
			FileUtils.write(portFile, String.valueOf(port));
		} catch (IOException e) {
			System.err.println("Failed to write server port to file");
			e.printStackTrace();
		}
		releaseLock();
	}
	
	/**
	 * Returns last known port bound by daemon server
	 * @return port of daemon server or default value if file doens't exist
	 */
	public static int getPort() {
		int port = 1024;
		getLock();
		try {
			if (portFile.exists()) {
				port = Integer.parseInt(FileUtils.readFileToString(portFile));
			}
		} catch (NumberFormatException | IOException e) {
			System.err.println("Failed to read port from file");
			e.printStackTrace();
		}
		releaseLock();
		return port;
	}
	
	private static void getLock() {
		try {
			channel = new RandomAccessFile(portFile, "rw").getChannel();
			lock = channel.lock();
		} catch (IOException e) {
			System.err.println("Failed to lock " + portFile.getAbsolutePath());
			e.printStackTrace();
		}
	}
	
	private static void releaseLock() {
		try {
			lock.release();
			channel.close();
		} catch (IOException e) {
			System.err.println("Failed to release lock on " + portFile.getAbsolutePath());
			e.printStackTrace();
		}
		
	}
}
