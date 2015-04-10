package org.simplesockets.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.simplesockets.protocol.SimpleSocketsProtocol;

public class SimpleServer implements Runnable {
	private int port;
	private ServerSocket socket;
	private boolean killServer, alive;
	private SimpleSocketsProtocol protocol;
	
	/**
	 * Basic constructor, takes port number as argument, port may change if bind fails
	 * @param port
	 */
	public SimpleServer(int port, SimpleSocketsProtocol protocol) {
		this.port = port;
		killServer = false;
		this.protocol = protocol;
		alive = false;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void startServer() {
		Thread serverThread = new Thread(this);
		serverThread.start();
	}
	
	public void run() {
		if ((port < 1024) || (port > 65535)) {
			throw new RuntimeException("Invalid port specified");
		}
		bindSocket();
		
		alive = true;
		while (!killServer) {
			Socket connection;
			try {
				connection = socket.accept();
				new SimpleServerConnectionHandler(connection, protocol);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (!(e instanceof SocketException)) {
					e.printStackTrace();
				}
			}
		}
		alive = false;
	}
	
	/**
	 * Private helper, attempts to bind socket on desired port,
	 * if port already bound will walk up port numbers attempting to bind
	 * until successful
	 */
	private void bindSocket() {
		boolean bound = false;
		while (!bound) {
			try {
				System.out.println("Attempting to bind port: " + port);
				socket = new ServerSocket(port);
				bound = true;
			} catch (IOException e) {
				System.out.println("Failed to bind port: " + port);
				port = ((port+1) % 65535);
			}
		}
		System.out.println("Successfuly bound port: " + port);
	}
	
	public void stopServer() {
		killServer = true;
		try {
			socket.close();
		} catch (IOException e) {
			// Expecting socket exception to be thrown
		}
	}
}
