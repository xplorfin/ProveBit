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
			public Object receive(Object data) {
				String fromServer = (String) data;
				return fromServer;
			}

			@Override
			public Object send(Object data) {
				return data;
			}
			
		};
		
		client = new SimpleClient(hostname, port, protocol);
		client.sendRequest((Object) "Client request"); 
		String reply = (String) client.getReply();
		System.out.println("Client got reply '" + reply + "'");
		client.disconnect();
	}
}
