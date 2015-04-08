package org.simplesockets.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.simplesockets.protocol.SimpleSocketsProtocol;

public class SimpleClient {
	private String hostname;
	private int port;
	private Socket socket;
	private SimpleSocketsProtocol protocol;
	private ObjectOutputStream toServer;
	private ObjectInputStream fromServer;
	
	public SimpleClient(String hostname, int port, SimpleSocketsProtocol protocol) {
		this.hostname = hostname;
		this.port = port;
		this.protocol = protocol;
	}
	
	public boolean connect() {
		try {
			socket = new Socket(hostname, port);
			toServer = new ObjectOutputStream(socket.getOutputStream());
			fromServer = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			socket = null;
			return false;
		}
		return true;
	}
	
	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendRequest(Object request) {
		this.connect();
		try {
			toServer.writeObject(protocol.send(request));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Object getReply() {
		Object reply = null;
		try {
			reply = fromServer.readObject();
			protocol.receive(reply);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return reply;
	}
}
