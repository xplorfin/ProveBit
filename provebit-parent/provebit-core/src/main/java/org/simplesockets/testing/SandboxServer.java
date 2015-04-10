package org.simplesockets.testing;

import org.simplesockets.protocol.SimpleSocketsProtocol;
import org.simplesockets.server.SimpleServer;

public class SandboxServer {
	public static void main(String[] args) {
		SimpleServer server;
		SimpleSocketsProtocol protocol = new SimpleSocketsProtocol() {
			
			@Override
			public Object receive(Object data) {
				String request = (String) data;
				System.out.println("Server got '" + request + "'");
				return (Object) "Server Reply";
			}

			@Override
			public Object send(Object data) {
				String reply = (String) data;
				return reply.getBytes();
			}
			
		};
		
		server = new SimpleServer(55566, protocol);
		server.startServer();
	}
}
