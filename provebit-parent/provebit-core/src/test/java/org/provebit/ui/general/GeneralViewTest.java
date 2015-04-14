package org.provebit.ui.general;

import org.junit.*;
import org.provebit.ui.RunGUI;
import org.uispec4j.Panel;
import org.uispec4j.TabGroup;
import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.MainClassAdapter;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class GeneralViewTest extends UISpecTestCase {
	private Window window;
	private Panel generalPane;
	
	/**
	 * this function call super.setUp() for some initial setup,
	 * and then set the adaptation between test case and main class
	 * to intercept the main window of the application
	 */
	public void setUp() throws Exception {
		super.setUp();
		UISpec4J.setWindowInterceptionTimeLimit(10000);
		setAdapter(new MainClassAdapter(RunGUI.class, new String[0]));
		window = getMainWindow();
		
		// retrieve general panel from the main window
		TabGroup tabGroup = window.getTabGroup("Main");
		tabGroup.selectTab("General");
		generalPane = tabGroup.getSelectedTab();
	}
	
	/**
	 * test the header name of the table
	 */
	public void testTableHeader() {
		assertTrue(generalPane.getTable().getHeader().contentEquals(new String[] {
				"File", "Status"
		}));
	}
	
	/**
	 * invoke the file chooser for certify file and test its properties
	 */
	public void testCertifyFileDialog() {	
		WindowInterceptor
		// set up the trigger to invoke the pop-up dialog
		.init(generalPane.getButton("Certify File").triggerClick())
		.process(FileChooserHandler.init()
				.titleEquals("Choose File to Certify")
				.cancelSelection())
		.run();
	}
}
