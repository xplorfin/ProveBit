package org.provebit.ui.daemon;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.provebit.ui.RunGUI;
import org.uispec4j.Panel;
import org.uispec4j.TabGroup;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.MainClassAdapter;

@RunWith(JUnit4.class)
public class DaemonViewTest_ extends UISpecTestCase {
	private Window window;
	private Panel daemonPane;
	
	@Rule
    public TemporaryFolder emptyFolder = new TemporaryFolder();
	
	public File emptyDirPath;
	
	
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
		
		emptyDirPath = emptyFolder.getRoot();
	}
	
	/**
	 * invoke and test the file chooser for adding file to daemon
	 */
//	@Test
//	public void testAddFile() throws IOException{
//		File tempFile = new File(emptyDirPath.getAbsolutePath() + "/tempFile");
//    	FileUtils.write(tempFile, "temp data");
//    	
//		WindowInterceptor
//		// set up the trigger to invoke the pop-up dialog
//		.init(daemonPane.getButton("Add Files to Monitor...").triggerClick())
//		.process(FileChooserHandler.init()
//				.titleEquals("Open")
//				.assertAcceptsFilesAndDirectories()
////				.select(tempFile.getAbsolutePath()))
//				.select("/home/qding5/test.txt"))
//		.run();
//		
//		ListBox listBox = daemonPane.getListBox();
////		assertTrue(listBox.contains((tempFile.getAbsolutePath())));
//		assertTrue(listBox.contains("/home/qding5/test.txt"));
//	}
}
