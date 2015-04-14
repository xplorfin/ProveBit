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
	
	/**
	 * Create a client that will use a specified server
	 * @param hostname - hostname of server to use
	 * @param port - port to contact server on
	 * @param protocol - protocol to use for communication
	 */
	public SimpleClient(String hostname, int port, SimpleSocketsProtocol protocol) {
		this.hostname = hostname;
		this.port = port;
		this.protocol = protocol;
	}
	
	/**
	 * Open a new connection with the server
	 */
	private void connect() {
		try {
			if (socket != null) {
				toServer.close();
				fromServer.close();
				socket.close();
			}
			socket = new Socket(hostname, port);
			toServer = new ObjectOutputStream(socket.getOutputStream());
			fromServer = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			//e.printStackTrace();
			socket = null;
		}
	}
	
	/**
	 * Kill an active connection if one exists
	 */
	public void disconnect() {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Open a connection with the server and send data
	 * @param request - Serializable object to send to server
	 */
	public void sendRequest(Object request) {
		connect();
		if (socket != null) {
			try {
				toServer.writeObject(protocol.send(request));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Get reply from server using connection established by previous
	 * call to sendRequest
	 * @return Object - Object received from server as reply
	 */
	public Object getReply() {
		if (socket == null) return null;
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
