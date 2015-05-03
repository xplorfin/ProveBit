package org.provebit.ui.main;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.provebit.ui.AdvancedTab;
import org.provebit.ui.DaemonTab;
import org.provebit.ui.GeneralTab;
import org.provebit.ui.WalletsTab;

public class MainView extends JFrame implements Observer {
	private static final long serialVersionUID = 5472827135842556415L;
	// TODO implement Main model
	@SuppressWarnings("unused")
	private MainModel model;
	private JLabel status;
	private JTabbedPane tabbedPane; 
	private JMenuBar menuBar;
	private JMenu menuFile;
	private JMenuItem menuItemFileQuit;
	private JMenu menuAbout;
	private JMenuItem menuItemAboutUs;
	private List<JMenuItem> menuItems;
	
	/**
	 * Main view model
	 * @param model 
	 */
	public MainView(MainModel model) {
		this.model = model;
		menuItems = new ArrayList<JMenuItem>();
		
		setLayout(new MigLayout());
		
		setSize(800, 600);
		setMinimumSize(new Dimension(500, 375));
		setTitle("ProveBit");
		
		// set up menu and tabs
		addMenuBar();
		addStatus();
		addTabs();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
	}
	
	/**
	 * Adds the top menu bar to the main view
	 */
	private void addMenuBar() {
		menuBar = new JMenuBar();
		
		menuFile = new JMenu("File");
		menuItemFileQuit = new JMenuItem("Quit");
		menuItemFileQuit.setActionCommand("File-Quit");
		
		menuAbout = new JMenu("About");
		menuItemAboutUs = new JMenuItem("About Us");
		menuItemAboutUs.setActionCommand("About-About Us");
		
		menuItems.add(menuItemAboutUs);
		menuItems.add(menuItemFileQuit);
		
		menuFile.add(menuItemFileQuit);
		menuAbout.add(menuItemAboutUs);
		menuBar.add(menuFile);
		menuBar.add(menuAbout);
		setJMenuBar(menuBar);
	}
	
	/**
	 * Adds status label, currently placeholder
	 */
	public void addStatus() {
		status = new JLabel("Status: you have 1 file(s) being proved");
		add(status, "wrap");
	}
	
	/**
	 * Adds the tab pane and instantiates all the tab MVC's
	 */
	private void addTabs() {
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setName("Main");
		tabbedPane.addTab("General", new GeneralTab().getPanel());
		tabbedPane.addTab("Wallet", new WalletsTab().getPanel());
		tabbedPane.addTab("Daemon", new DaemonTab().getPanel());
		tabbedPane.addTab("Advanced", new AdvancedTab().getPanel());
		add(tabbedPane, "grow, push, span");
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof String) {
			if ("showAbout".compareTo((String) arg) == 0) {
				JOptionPane.showMessageDialog(this, 
						"ProveBit - Trusted Timestamp System Using Bitcoin Network", 
						"About Us", 
						JOptionPane.PLAIN_MESSAGE);
			}
		}
	}
	
	public void addController(ActionListener controller) {
		for (JMenuItem item : menuItems) {
			item.addActionListener(controller);
		}
	}
	
	public static class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			e.getWindow().setVisible(false);
			System.exit(0);
		}
	}

}
