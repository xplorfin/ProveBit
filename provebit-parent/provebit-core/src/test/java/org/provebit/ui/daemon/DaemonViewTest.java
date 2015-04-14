package org.provebit.ui.daemon;

import org.provebit.ui.RunGUI;
import org.uispec4j.Panel;
import org.uispec4j.TabGroup;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.MainClassAdapter;
import org.uispec4j.interception.WindowInterceptor;

public class DaemonViewTest extends UISpecTestCase {
	private Window window;
	private Panel daemonPane;
	
	/**
	 * this function call super.setUp() for some initial setup,
	 * and then set the adaptation between test case and main class
	 * to intercept the main window of the application
	 */
	public void setUp() throws Exception {
		super.setUp();
		UISpec4J.setWindowInterceptionTimeLimit(100000);
		setAdapter(new MainClassAdapter(RunGUI.class, new String[0]));
		window = getMainWindow();
		
		// retrieve daemon panel from the main window
		TabGroup tabGroup = window.getTabGroup("Main");
		tabGroup.selectTab("Daemon");
		daemonPane = tabGroup.getSelectedTab();
	}
	
	/**
	 * invoke and test the file chooser for adding file to daemon
	 */
	public void testAddFile() {	
		WindowInterceptor
		// set up the trigger to invoke the pop-up dialog
		.init(daemonPane.getButton("Add Files to Monitor...").triggerClick())
		.process(FileChooserHandler.init()
				.titleEquals("Open")
				.cancelSelection())
		.run();
	}
}
