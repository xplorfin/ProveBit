package org.provebit.ui.daemon;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.provebit.ui.RunGUI;
import org.uispec4j.ListBox;
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

@RunWith(JUnit4.class)
public class DaemonViewTest_ extends UISpecTestCase {
	private Window window;
	private Panel daemonPane;
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
	}
	
	/**
	 * invoke and test the file chooser for adding file to daemon
	 * @throws InterruptedException 
	 */
	@Test
	public void testAddFile() throws IOException, InterruptedException{
		WindowInterceptor
		// set up the trigger to invoke the pop-up dialog
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
	public void testDeleteFile() {
		ListBox listBox = daemonPane.getListBox();
		listBox.select(filePath);
		daemonPane.getButton("Remove Selected Files").click();
		assertTrue(listBox.isEmpty());
	}
	
}
