package org.simplesockets.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.simplesockets.protocol.SimpleSocketsProtocol;

public class SimpleServer {
	private int port;
	private ServerSocket socket;
	private boolean killServer;
	private SimpleSocketsProtocol protocol;
	
	/**
	 * Basic constructor, takes port number as argument, port may change if bind fails
	 * @param port
	 */
	public SimpleServer(int port, SimpleSocketsProtocol protocol) {
		this.port = port;
		killServer = false;
		this.protocol = protocol;
	}
	
	public int getPort() {
		return port;
	}
	
	public void startServer() {
		if ((port < 1024) || (port > 65535)) {
			throw new RuntimeException("Invalid port specified");
		}
		bindSocket();

		while (!killServer) {
			Socket connection;
			try {
				connection = socket.accept();
				new SimpleServerConnectionHandler(connection, protocol);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
				port = ((port+1) % 65535) + 1024;
			}
		}
		System.out.println("Successfuly bound port: " + port);
	}
	
	public void stopServer() {
		killServer = true;
	}
}
