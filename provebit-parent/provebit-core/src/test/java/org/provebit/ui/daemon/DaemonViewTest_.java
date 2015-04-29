package org.provebit.ui.daemon;

import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.provebit.daemon.DaemonProtocol;
import org.provebit.daemon.DaemonProtocol.DaemonMessage;
import org.provebit.daemon.DaemonProtocol.DaemonMessage.DaemonMessageType;
import org.provebit.ui.RunGUI;
import org.simplesockets.client.SimpleClient;
import org.uispec4j.ListBox;
import org.uispec4j.Panel;
import org.uispec4j.TabGroup;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.MainClassAdapter;
import org.uispec4j.interception.WindowInterceptor;

@RunWith(JUnit4.class)
public class DaemonViewTest_ extends UISpecTestCase {
	private Window window;
	private Panel daemonPane;
	private DaemonStatus daemonStatus;
	private enum DaemonStatus{ACTIVE, SUSPENDED, TRACKING};
	private SimpleClient daemonClient = null;
	private int port;
	private final String hostname = "localhost";
	private DaemonProtocol clientProtocol;
	private boolean daemonConnected;
	private DaemonMessage killDaemon = new DaemonMessage(DaemonMessageType.KILL, null);
	// test file path
	private String filePath = System.getProperty("user.dir") + "/src/test/resources/org/provebit/ui/daemon/test.txt";
	
	/**
	 * this function call super.setUp() for some initial setup,
	 * and then set the adaptation between test case and main class
	 * to intercept the main window of the application
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		UISpec4J.setWindowInterceptionTimeLimit(100000);
		setAdapter(new MainClassAdapter(RunGUI.class, new String[0]));
		window = getMainWindow();
		
		// retrieve daemon panel from the main window
		TabGroup tabGroup = window.getTabGroup("Main");
		tabGroup.selectTab("Daemon");
		daemonPane = tabGroup.getSelectedTab();

		clientProtocol = getProtocol();
		/**
		 * Since the GUI launches the actual application, the actual application
		 * launches a standard application daemon (that has recovery mode enabled)
		 * 
		 * Thus we need to kill that daemon, and launch our own daemon from this test case
		 * This is an extremely silly solution, and a better solution should be researched
		 * the future
		 */
		connectToDaemon();
		daemonClient.sendRequest(killDaemon);
		Thread.sleep(1500);
		connectToDaemon();
		
		if (!daemonConnected) { // No daemon running, start a new one
			try {
				launchNewDaemon();
			} catch (IOException | InterruptedException e) {
				System.out.println("Failed to launch new JVM with daemon");
				e.printStackTrace();
			}
			System.out.println("LAUNCHED");
		}
	}
	
	@After
	public void tearDown() throws Exception {
		// Kill the daemon after every launch to ensure a clean daemon
		// is launched
		if (daemonClient != null) {
			daemonClient.sendRequest(killDaemon);
			daemonConnected = false;
			Thread.sleep(1500);
		}
	}
	
	/**
	 * invoke and test the file chooser for adding file to daemon
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Test
	public void testAddFile() throws IOException, InterruptedException{
		WindowInterceptor
		// set up the trigger to invoke the file chooser dialog
		.init(daemonPane.getButton("Add Files to Monitor").triggerClick())
		.process(FileChooserHandler.init()
				.titleEquals("Open")
				.assertAcceptsFilesAndDirectories()
				.select(filePath))
		.run();
		
		ListBox listBox = daemonPane.getListBox();
		Thread.sleep(500);
		assertTrue(listBox.contains(filePath));
	}
	
	@Test
	public void testDeleteFile() throws InterruptedException {
		/**
		 * Need to add a file first, then try to delete it, tests cannot be dependent
		 */
		WindowInterceptor
		// set up the trigger to invoke the file chooser dialog
		.init(daemonPane.getButton("Add Files to Monitor").triggerClick())
		.process(FileChooserHandler.init()
				.titleEquals("Open")
				.assertAcceptsFilesAndDirectories()
				.select(filePath))
		.run();
		
		ListBox listBox = daemonPane.getListBox();
		assertTrue(listBox.contains(filePath));
		
		listBox = daemonPane.getListBox();
		assertFalse(listBox.isEmpty());
		listBox.select(filePath);
		daemonPane.getButton("Remove Selected Files").click();
		Thread.sleep(500);
		assertTrue(listBox.isEmpty());
	}
	
	@Test
	public void testLogAfterAdd() throws InterruptedException {
		WindowInterceptor
		// set up the trigger to invoke the file chooser dialog
		.init(daemonPane.getButton("Add Files to Monitor").triggerClick())
		.process(FileChooserHandler.init()
				.titleEquals("Open")
				.assertAcceptsFilesAndDirectories()
				.select(filePath))
		.run();
		
		// check message
		DaemonMessage logRequest = new DaemonMessage(DaemonMessageType.GETLOG, null);
		daemonClient.sendRequest(logRequest);
		Thread.sleep(500);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		String[] testStrings = ((String) reply.data).split(" ");
		assertTrue(Arrays.asList(testStrings).contains("'ADDFILES'"));
	}
	
	@Test
	public void testLogAfterDelete() throws InterruptedException {
		WindowInterceptor
		// set up the trigger to invoke the file chooser dialog
		.init(daemonPane.getButton("Add Files to Monitor").triggerClick())
		.process(FileChooserHandler.init()
				.titleEquals("Open")
				.assertAcceptsFilesAndDirectories()
				.select(filePath))
		.run();
		
		ListBox listBox = daemonPane.getListBox();
		listBox.select(filePath);
		daemonPane.getButton("Remove Selected Files").click();
		
		// check message
		DaemonMessage logRequest = new DaemonMessage(DaemonMessageType.GETLOG, null);
		daemonClient.sendRequest(logRequest);
		Thread.sleep(500);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		String[] testStrings = ((String) reply.data).split(" ");
		assertTrue(Arrays.asList(testStrings).contains("'REMOVEFILES'"));
	}
	
	
	private void createDaemonClient() {
		daemonClient = new SimpleClient(hostname, port, clientProtocol);
	}
	
	private void connectToDaemon() {
		// Get last known port form well known (application folder config file) location
		// For now the daemon starts the server on a known port (9999)
		/** @TODO Remove hardcoded port */
		int testPort = 9999, attempts = 10;
		SimpleClient heartbeat = new SimpleClient(hostname, testPort, clientProtocol);
		boolean connected = false;
		while (!connected && attempts > 0) {
			heartbeat.sendRequest(new DaemonMessage(DaemonMessageType.HEARTBEAT, null));
			DaemonMessage reply = (DaemonMessage) heartbeat.getReply();
			if (reply != null) {
				System.out.println("Daemon server found on port " + testPort);
				port = testPort;
				connected = true;
				createDaemonClient();
			}
			attempts--;
		}
		daemonConnected = connected;
	}
	
	private DaemonProtocol getProtocol() {
		return new DaemonProtocol() {
			@Override
			public DaemonMessage handleMessage(DaemonMessage request) {
				return request;
			}
		};
	}
	
	private void launchNewDaemon() throws IOException, InterruptedException {
		String separator = System.getProperty("file.separator");
		String classpath = System.getProperty("java.class.path");
		String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
		ProcessBuilder processBuilder =  new ProcessBuilder(path, "-cp", classpath, LaunchDaemon.class.getName(), "false");
		processBuilder.start();
		waitOnDaemon();
	}
	
	private void waitOnDaemon() {
		while(!daemonConnected) {
			connectToDaemon();
		}
		createDaemonClient();
	}
}
