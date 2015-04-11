package org.simplesockets.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.simplesockets.client.SimpleClient;
import org.simplesockets.protocol.SimpleSocketsProtocol;
import org.simplesockets.server.SimpleServer;

public class SimpleSocketsTest {
	static SimpleSocketsProtocol emptyProtocol, echoProtocol;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		emptyProtocol = new SimpleSocketsProtocol() {

			@Override
			public Object receive(Object data) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object send(Object data) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		
		echoProtocol = new SimpleSocketsProtocol() {

			@Override
			public Object receive(Object data) {
				String received =(String) data;
				return received;
			}

			@Override
			public Object send(Object data) {
				return data;
			}
			
		};
	}

	
	@Test
	public void testServerStartInvalidPortHigh() throws InterruptedException {
		int port = 65536;
		SimpleServer server = new SimpleServer(port, emptyProtocol);
		server.startServer();
		
		Thread.sleep(200);
		assertFalse(server.isAlive());
	}
	
	@Test
	public void testServerStartInvalidPortLow() throws InterruptedException {
		int port = 1022;
		SimpleServer server = new SimpleServer(port, emptyProtocol);
		server.startServer();
		
		Thread.sleep(200);
		assertFalse(server.isAlive());
	}
	
	@Test
	public void testServerStartStop() throws InterruptedException {
		int port = 12345;
		SimpleServer server = new SimpleServer(port, emptyProtocol);
		try {
			server.startServer();
		} catch (Exception e) {
			fail();
		}
		Thread.sleep(200);
		assertTrue(server.isAlive());
		server.stopServer();
		Thread.sleep(200);
		assertFalse(server.isAlive());
	}
	
	@Test
	public void testServerStartPortBusy() throws InterruptedException {
		// Bind a known port, then try to have the server bind it
		// ensure server picks a different port
		int port = 43323;
		SimpleServer server = null;
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			fail();
		}
		server = new SimpleServer(port, emptyProtocol);
		server.startServer();
		Thread.sleep(200);
		assertTrue(server.isAlive());
		assertTrue(server.getPort() != port);
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server.stopServer();
	}
	
	@Test
	public void testClientConnect() throws InterruptedException {
		SimpleServer server;
		SimpleClient client;
		int port = 55555;
		String hostname = "localhost";
		server = new SimpleServer(port, emptyProtocol);
		server.startServer();
		Thread.sleep(200);
		port = server.getPort();
		client = new SimpleClient(hostname, port, emptyProtocol);
		Thread.sleep(50);
		client.disconnect();
		server.stopServer();
	}
	
	@Test
	public void testClientSend() throws InterruptedException {
		SimpleServer server;
		SimpleClient client;
		int port = 55555;
		String hostname = "localhost";
		server = new SimpleServer(port, echoProtocol);
		server.startServer();
		Thread.sleep(200);
		port = server.getPort();
		client = new SimpleClient(hostname, port, echoProtocol);
		client.sendRequest("Hello");
		Thread.sleep(50);
		client.disconnect();
		server.stopServer();
	}
	
	@Test
	public void testClientReceive() throws InterruptedException {
		SimpleServer server;
		SimpleClient client;
		int port = 55555;
		String hostname = "localhost";
		server = new SimpleServer(port, echoProtocol);
		server.startServer();
		Thread.sleep(200);
		port = server.getPort();
		client = new SimpleClient(hostname, port, echoProtocol);
		client.sendRequest("Hello");
		Thread.sleep(50);
		String reply = (String) client.getReply();
		client.disconnect();
		server.stopServer();
		assertEquals(reply.compareTo("Hello"), 0);
	}
	
	@Test
	public void testServerMultipleClients() throws InterruptedException {
		SimpleServer server;
		int port = 34343, numClients = 10, numMessages = 10;
		String hostname = "localhost";
		List<SimpleClient> clientList = new ArrayList<SimpleClient>();
		server = new SimpleServer(port, echoProtocol);
		server.startServer();
		Thread.sleep(200);
		
		for (int i = 0; i < numClients; i++) {
			clientList.add(new SimpleClient(hostname, server.getPort(), echoProtocol));
		}
		
		for (int i = 0; i < numMessages; i++) {
			String message = "Message " + i;
			for (int j = 0; j < clientList.size(); j++) {
				SimpleClient client = clientList.get(j);
				String clientReq = message + " from client " + j;
				client.sendRequest(clientReq);
			}
			
			Thread.sleep(200);
			
			for (int j = 0; j < clientList.size(); j++) {
				SimpleClient client = clientList.get(j);
				String expected = message + " from client " + j;
				String reply = (String) client.getReply();
				assertEquals(reply.compareTo(expected), 0);
			}
		}
		
		for (SimpleClient client : clientList) {
			client.disconnect();
		}
		server.stopServer();
	}
}
