package org.provebit.ui.general;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.provebit.daemon.DaemonProtocol;
import org.provebit.daemon.DaemonProtocol.DaemonMessage;
import org.provebit.daemon.DaemonProtocol.DaemonMessage.DaemonMessageType;
import org.provebit.ui.RunGUI;
import org.provebit.utils.ServerUtils;
import org.simplesockets.client.SimpleClient;
import org.uispec4j.Panel;
import org.uispec4j.TabGroup;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.MainClassAdapter;
import org.uispec4j.interception.WindowInterceptor;

@RunWith(JUnit4.class)
public class GeneralViewTest_ extends UISpecTestCase {
	private Window window;
	private Panel generalPane;
	
	private static SimpleClient daemonClient = null;
	private int port;
	private final String hostname = "localhost";
	private static DaemonProtocol clientProtocol;
	@SuppressWarnings("unused")
	private boolean daemonConnected;
	private DaemonMessage killDaemon = new DaemonMessage(DaemonMessageType.KILL, null);
	private DaemonMessage resetDaemon = new DaemonMessage(DaemonMessageType.RESET, null);
	
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
		
		clientProtocol = getProtocol();
		connectToDaemon();
		
		// retrieve general panel from the main window
		TabGroup tabGroup = window.getTabGroup("Main");
		tabGroup.selectTab("General");
		generalPane = tabGroup.getSelectedTab();
	}
	
	@After
	public void tearDown() throws Exception {
		// Kill the daemon after every launch to ensure a clean daemon
		// is launched
		if (daemonClient != null) {
			daemonClient.sendRequest(resetDaemon);
			Thread.sleep(500);
			daemonClient.sendRequest(killDaemon);
			daemonConnected = false;
			Thread.sleep(1500);
		}
	}
	
	/**
	 * test the header name of the table
	 */
	@Test
	public void testTableHeader() {
		assertTrue(generalPane.getTable().getHeader().contentEquals(new String[] {
				"File", "Status"
		}));
	}
	
	/**
	 * invoke the file chooser for certify file and test its properties
	 */
	@Test
	public void testCertifyFileDialog() {	
		WindowInterceptor
		// set up the trigger to invoke the pop-up dialog
		.init(generalPane.getButton("Certify File").triggerClick())
		.process(FileChooserHandler.init()
				.titleEquals("Choose File to Certify")
				.cancelSelection())
		.run();
	}
	
	private void createDaemonClient() {
		daemonClient = new SimpleClient(hostname, port, clientProtocol);
	}
	
	private void connectToDaemon() {
		int testPort = ServerUtils.getPort(), attempts = 10;
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
}
	
	