package org.simplesockets.unittests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.simplesockets.protocol.SimpleSocketsProtocol;
import org.simplesockets.server.SimpleServer;

public class SimpleSocketsTest {
	static SimpleSocketsProtocol emptyProtocol, echoProtocol;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		emptyProtocol = new SimpleSocketsProtocol() {

			@Override
			public Object receive(byte[] data) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public byte[] send(Object data) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		
		echoProtocol = new SimpleSocketsProtocol() {

			@Override
			public Object receive(byte[] data) {
				String received = new String(data);
				return received;
			}

			@Override
			public byte[] send(Object data) {
				return ((String)data).getBytes();
			}
			
		};
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void testServerStartInvalidPortHigh() throws InterruptedException {
		int port = 65536;
		SimpleServer server = new SimpleServer(port, emptyProtocol);
		try {
			server.startServer();
		} catch (Exception e) {
			// Ignore
		}
		
		Thread.sleep(200);
		assertFalse(server.isAlive());
	}
	
	@Test
	public void testServerStartInvalidPortLow() throws InterruptedException {
		int port = 1022;
		SimpleServer server = new SimpleServer(port, emptyProtocol);
		try {
			server.startServer();
		} catch (Exception e) {
			// Ignore
		}
		
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
	public void testClientConnect() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testClientSend() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testClientReceive() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testServerReceive() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testServerSend() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testServerMultipleClients() {
		fail("Not yet implemented");
	}
}
