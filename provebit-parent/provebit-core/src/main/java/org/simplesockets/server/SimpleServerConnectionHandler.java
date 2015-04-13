package org.simplesockets.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.simplesockets.protocol.SimpleSocketsProtocol;

public class SimpleServerConnectionHandler implements Runnable {
	private Socket socket;
	private SimpleSocketsProtocol protocol;
	
	/**
	 * Spawn thread to handle an incoming client connection
	 * @param socket - socket with active client connection
	 * @param protocol - protocol to use when handling client data
	 */
	public SimpleServerConnectionHandler(Socket socket, SimpleSocketsProtocol protocol) {
		this.socket = socket;
		this.protocol = protocol;
		Thread thread = new Thread(this);
		thread.start();
	}
	
	/**
	 * Reads any incoming client data and calls the protocols receive method
	 * Returns a reply if one is returned by the defined protocol
	 */
	public void run() {
		ObjectInputStream socketInput;
		ObjectOutputStream socketOutput;
		Object request;
		Object reply;
		
		try {
			socketInput = new ObjectInputStream(socket.getInputStream());
			socketOutput = new ObjectOutputStream(socket.getOutputStream());
			
			request = socketInput.readObject();
			reply = protocol.receive(request);
			if (reply != null) {
				socketOutput.writeObject(protocol.send(reply));
			}
			
			socketInput.close();
			socketOutput.close();
			socket.close();
		} catch (IOException | ClassNotFoundException e) {
			if (!(e instanceof SocketException)) {
				// Client may have chosen to ignore the reply message and closed the connection
				e.printStackTrace();
			}
		}
	}
}
