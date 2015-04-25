package org.provebit.ui.daemon;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
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

// test cases are designed to run in order, so they will clean
// up their trace in GUI
// every test case name has a prefix to ensure the running order
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DaemonViewTest_ extends UISpecTestCase {
	private Window window;
	private Panel daemonPane;
	private DaemonStatus daemonStatus;
	private enum DaemonStatus{ACTIVE, SUSPENDED, TRACKING};
	private SimpleClient daemonClient;
	private int port;
	private final String hostname = "localhost";
	private DaemonProtocol clientProtocol;
	private boolean daemonConnected;
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
		UISpec4J.setWindowInterceptionTimeLimit(10000);
		setAdapter(new MainClassAdapter(RunGUI.class, new String[0]));
		window = getMainWindow();
		
		// retrieve daemon panel from the main window
		TabGroup tabGroup = window.getTabGroup("Main");
		tabGroup.selectTab("Daemon");
		daemonPane = tabGroup.getSelectedTab();
		
		// set up a simple client to get message from log
		clientProtocol = getProtocol();
		connectToDaemon();
		
		if (!daemonConnected) { // No daemon running, start a new one
			try {
				launchNewDaemon();
			} catch (IOException | InterruptedException e) {
				System.out.println("Failed to launch new JVM with daemon");
				e.printStackTrace();
			}
			System.out.println("LAUNCHED");
			//connectToDaemon();
		}
		
		if (daemonConnected) {
			daemonClient = new SimpleClient(hostname, port, clientProtocol);
			daemonStatus = DaemonStatus.ACTIVE;
		} else {
			throw new RuntimeException("Cannot connect to local daemon server!");
		}
	}
	
	
	/**
	 * invoke and test the file chooser for adding file to daemon
	 * @throws IOException
	 */
	@Test
	public void aTestAddFile() throws IOException{
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
	}
	
	@Test
	public void bTestDeleteFile() throws InterruptedException {
		ListBox listBox = daemonPane.getListBox();
		listBox.select(filePath);
		daemonPane.getButton("Remove Selected Files").click();
		Thread.sleep(500);
		assertTrue(listBox.isEmpty());
	}
	
	@Test
	public void cTestLogAfterAdd() {
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
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		System.out.println(reply);
		String[] testStrings = ((String) reply.data).split(" ");
		assertTrue(Arrays.asList(testStrings).contains("'GETTRACKED'"));
	}
	
	@Test
	public void dTestLogAfterDelete() {
		ListBox listBox = daemonPane.getListBox();
		listBox.select(filePath);
		daemonPane.getButton("Remove Selected Files").click();
		
		// check message
		DaemonMessage logRequest = new DaemonMessage(DaemonMessageType.GETLOG, null);
		daemonClient.sendRequest(logRequest);
		DaemonMessage reply = (DaemonMessage) daemonClient.getReply();
		String[] testStrings = ((String) reply.data).split(" ");
		assertTrue(Arrays.asList(testStrings).contains("'GETLOG'"));
	}
	
	@Test
	public void eTestLogFrameAfterAdd() {
		// TODO check log from log frame after add file
	}
	
	
	// below are functions from DaemonModel used to connect the server
	// it doesn't really make sense to create a DaemonModel in this test case
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
		ProcessBuilder processBuilder =  new ProcessBuilder(path, "-cp", classpath, LaunchDaemon.class.getName());
		processBuilder.start();
		waitOnDaemon();
	}
	
	private void waitOnDaemon() {
		while(!daemonConnected) {
			connectToDaemon();
		}
	}
}
