package org.provebit.ui;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

public class MainFrame extends JFrame {

	public JMenuBar menuBar = new JMenuBar();
	public JMenu menuFile = new JMenu("File");
	public JMenuItem menuItemFileQuit = new JMenuItem("Quit");
	public JMenu menuAbout = new JMenu("About");
	public JMenuItem menuItemAboutUs = new JMenuItem("About Us");
	
	public DaemonConfigTab daemonConfigTab = new DaemonConfigTab();
	
	public MainFrame() {
		setLayout(new MigLayout());
		// set up menu bar
		addMenuBar();
		
		// set up tabbed pane
		addTabs();
		
		// pack up
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
	}
	
	public void addMenuBar() {
		menuFile.add(menuItemFileQuit);
		menuAbout.add(menuItemAboutUs);
		menuBar.add(menuFile);
		menuBar.add(menuAbout);
		this.setJMenuBar(menuBar);
	}
	
	public void addTabs() {
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Daemon Config", daemonConfigTab);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		this.add(tabbedPane);
	}
}
