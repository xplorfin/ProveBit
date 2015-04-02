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
	
	public SimpleClient(String hostname, int port, SimpleSocketsProtocol protocol) {
		this.hostname = hostname;
		this.port = port;
		this.protocol = protocol;
	}
	
	public boolean connect() {
		try {
			socket = new Socket(hostname, port);
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
		ObjectOutputStream toServer;
		try {
			toServer = new ObjectOutputStream(socket.getOutputStream());
			toServer.writeObject(protocol.send(request));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Object getReply() {
		byte[] reply = null;
		ObjectInputStream fromServer;
		try {
			fromServer = new ObjectInputStream(socket.getInputStream());
			reply = (byte[]) fromServer.readObject();
			protocol.receive(reply);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return protocol.receive(reply);
	}
}
