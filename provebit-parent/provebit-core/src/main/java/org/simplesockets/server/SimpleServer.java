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
	
	/**
	 * Checks if server is currently accepting connections
	 * @return true if accepting connections, false o/w
	 */
	public boolean isAlive() {
		return alive;
	}
	
	/**
	 * Spawn a new thread that runs the server
	 */
	public void startServer() {
		Thread serverThread = new Thread(this);
		serverThread.start();
	}
	
	/**
	 * Primary method
	 * Binds socket then enters loop to continually accept and handle client connections
	 */
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
				socket = new ServerSocket(port);
				bound = true;
			} catch (IOException e) {
				port = ((port+1) % 65535);
			}
		}
		System.out.println("Server successfuly bound port: " + port);
	}
	
	/**
	 * Kill the server
	 */
	public void stopServer() {
		killServer = true;
		try {
			socket.close();
		} catch (IOException e) {
			// Expecting socket exception to be thrown
		}
		alive = false;
	}
}
