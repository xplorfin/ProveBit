package org.provebit.ui;

import java.awt.Menu;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

public class MainFrame extends JFrame {

	public JMenuBar menuBar = new JMenuBar();
	public JMenu menuFile = new JMenu("File");
	public JMenuItem menuItemFileQuit = new JMenuItem("Quit");
	public JMenu menuAbout = new JMenu("About");
	public JMenuItem menuItemAboutUs = new JMenuItem("About Us");
	
	public MainFrame() {
		setLayout(new MigLayout());
		// set up menu bar
		menuFile.add(menuItemFileQuit);
		menuAbout.add(menuItemAboutUs);
		menuBar.add(menuFile);
		menuBar.add(menuAbout);
		this.setJMenuBar(menuBar);
		
		// pack up
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
	}
}
