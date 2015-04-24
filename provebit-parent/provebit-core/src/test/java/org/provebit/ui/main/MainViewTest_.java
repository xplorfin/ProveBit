package org.provebit.ui.main;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.provebit.ui.RunGUI;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TabGroup;
import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.MainClassAdapter;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class MainViewTest_ extends UISpecTestCase {
	
	private Window window;
	private TabGroup tabGroup;
	
	/**
	 * this function call super.setUp() for some initial setup,
	 * and then set the adaptation between test case and main class
	 * to intercept the main window of the application
	 */
	public void setUp() throws Exception {
		super.setUp();
		UISpec4J.setWindowInterceptionTimeLimit(10000);
		setAdapter(new MainClassAdapter(RunGUI.class));
		window = getMainWindow();
	}
	
	/**
	 * test the menu bar options
	 */
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
	public void testAllTabNameInTabGroup() {
		// get a new TabGroup for every test case
		tabGroup = window.getTabGroup("Main");
		// this is the expected names for tabs
		assertTrue(tabGroup.tabNamesEquals(new String[]{"General", "Wallet", "Daemon", "Advanced"}));
	}
	
	/**
	 * test default tab when the application starts up
	 */
	public void testDefaultTabIsGeneral() {
		tabGroup = window.getTabGroup("Main");
		// default tab is General
		assertTrue(tabGroup.selectedTabEquals("General"));
		// not true
		assertFalse(tabGroup.selectedTabEquals("Wallet"));
	}
	
	/**
	 * test every panel in TabbedPane is initialized
	 */
	public void testTabPanelIsInitilized() {
		// TODO test panels are initialized
	}
	
}
