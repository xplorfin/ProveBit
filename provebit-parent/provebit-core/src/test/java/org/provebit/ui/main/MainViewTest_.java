package org.provebit.ui.main;

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
import org.uispec4j.TabGroup;
import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.MainClassAdapter;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

@RunWith(JUnit4.class)
public class MainViewTest_ extends UISpecTestCase {
	
	private Window window;
	private TabGroup tabGroup;
	
	private static SimpleClient daemonClient = null;
	private int port;
	private final String hostname = "localhost";
	private static DaemonProtocol clientProtocol;
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
		setAdapter(new MainClassAdapter(RunGUI.class));
		window = getMainWindow();
		
		clientProtocol = getProtocol();
		connectToDaemon();
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
	 * test the menu bar options
	 */
	@Test
	public void testMenuBarContent() {
		assertTrue(window.getMenuBar().contentEquals(new String[]{"File", "About"}));
	}
	
	/**
	 * invoke the About Us dialog by using trigger to click the button
	 * on the menu, then call WindowInterceptor for intercepting pop-up
	 * dialog
	 * 
	 * need to close the close this pop-up dialog by return the close
	 * trigger in .process
	 */
	@Test
	public void testAboutUs() {	
		WindowInterceptor
		// set up the trigger to invoke the pop-up dialog
		.init(window.getMenuBar().getMenu("About").getSubMenu("About Us").triggerClick())
		.process(new WindowHandler("Test About Us Dialog") {
			public Trigger process(Window window) throws Exception {
				assertEquals("About Us", window.getTitle());
				// return close trigger
				return window.getButton("OK").triggerClick();
			}
		})
		.run();
	}
	
	/**
	 * test all the tab name in main TabbedPane
	 */
	@Test
	public void testAllTabNameInTabGroup() {
		// get a new TabGroup for every test case
		tabGroup = window.getTabGroup("Main");
		// this is the expected names for tabs
		assertTrue(tabGroup.tabNamesEquals(new String[]{"General", "Wallet", "Daemon", "Advanced"}));
	}
	
	/**
	 * test default tab when the application starts up
	 */
	@Test
	public void testDefaultTabIsGeneral() {
		tabGroup = window.getTabGroup("Main");
		// default tab is General
		assertTrue(tabGroup.selectedTabEquals("General"));
		// not true
		assertFalse(tabGroup.selectedTabEquals("Wallet"));
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
