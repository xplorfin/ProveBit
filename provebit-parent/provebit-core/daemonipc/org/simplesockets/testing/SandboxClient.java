package org.simplesockets.testing;

import org.simplesockets.client.SimpleClient;
import org.simplesockets.protocol.SimpleSocketsProtocol;

public class SandboxClient {
	public static void main(String[] args) {
		SimpleClient client;
		String hostname = "localhost";
		int port = 55566;
		SimpleSocketsProtocol protocol = new SimpleSocketsProtocol() {

			@Override
			public Object receive(byte[] data) {
				String fromServer = new String(data);
				return fromServer;
			}

			@Override
			public byte[] send(Object data) {
				String toServer = (String) data;
				return toServer.getBytes();
			}
			
		};
		
		client = new SimpleClient(hostname, port, protocol);
		client.connect();
		client.sendRequest((Object) "Client request"); 
		String reply = (String) client.getReply();
		System.out.println("Client got reply '" + reply + "'");
		client.disconnect();
	}
}
