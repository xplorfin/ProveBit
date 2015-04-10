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
	
	public SimpleServerConnectionHandler(Socket socket, SimpleSocketsProtocol protocol) {
		this.socket = socket;
		this.protocol = protocol;
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void run() {
		System.out.println("Connection handler launched");
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
