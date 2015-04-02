package org.simplesockets.unittests;

import static org.junit.Assert.fail;

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
	public void testServerStartInvalidPortHigh() {
		int port = 65536;
		SimpleServer server = new SimpleServer(port, emptyProtocol);
		try {
			server.startServer();
			fail();
		} catch (Exception e) {
			if (!(e instanceof RuntimeException)) {
				fail();
			}
		}
	}
	
	@Test
	public void testServerStartInvalidPortLow() {
		int port = 1022;
		SimpleServer server = new SimpleServer(port, emptyProtocol);
		try {
			server.startServer();
			fail();
		} catch (Exception e) {
			if (!(e instanceof RuntimeException)) {
				fail();
			}
		}
	}
	
	@Test
	public void testServerStart() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testServerStop() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testServerStartPortBusy() {
		fail("Not yet implemented");
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
