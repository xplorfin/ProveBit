package org.simplesockets.testing;

import org.simplesockets.server.SimpleServer;

public class SandboxServer {
	public static void main(String[] args) {
		SimpleServer server;
		SandboxProtocol protocol = new SandboxProtocol() {
			
			@Override
			public Object receive(byte[] data) {
				String request = new String(data);
				System.out.println("Server got '" + request + "'");
				return (Object) "Server Reply";
			}

			@Override
			public byte[] send(Object data) {
				String reply = (String) data;
				return reply.getBytes();
			}
			
		};
		
		server = new SimpleServer(55566, protocol);
		server.startServer();
	}
}
