package org.provebit.ui.main;

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
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.provebit.ui.AdvancedTab;
import org.provebit.ui.DaemonTab;
import org.provebit.ui.GeneralTab;
import org.provebit.ui.WalletTab;

public class MainView extends JFrame implements Observer {
	private MainModel model;
	private JTabbedPane tabbedPane; 
	private JMenuBar menuBar;
	private JMenu menuFile;
	private JMenuItem menuItemFileQuit;
	private JMenu menuAbout;
	private JMenuItem menuItemAboutUs;
	private JFrame aboutUsFrame;
	private JPanel aboutUsPanel;
	private List<JMenuItem> menuItems;
	
	public MainView(MainModel model) {
		this.model = model;
		menuItems = new ArrayList<JMenuItem>();
		setSize(500,400); // Temporary until we get sizes of tabs set
		
		setTitle("ProveBit");
		
		// set up menu and tabs
		addMenuBar();
		addTabs();
		setupAboutUsFrame();
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
	}
	
	private void setupAboutUsFrame() {
		aboutUsFrame = new JFrame("About Us");
		aboutUsPanel = new JPanel(new MigLayout());
		aboutUsPanel.setSize(300,200);
		aboutUsPanel.add(new JLabel("ProveBit Iteration 3 About Us"));
		aboutUsPanel.setVisible(true);
		aboutUsFrame.add(aboutUsPanel);
		aboutUsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		aboutUsFrame.pack();
		aboutUsFrame.setVisible(false);
	}
	
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
	
	private void addTabs() {
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("General", new GeneralTab().getPanel());
		tabbedPane.addTab("Wallet", new WalletTab().getPanel());
		tabbedPane.addTab("Daemon", new DaemonTab().getPanel());
		tabbedPane.addTab("Advanced", new AdvancedTab().getPanel());
		add(tabbedPane);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof String) {
			if ("showAbout".compareTo((String) arg) == 0) {
				aboutUsFrame.setVisible(true);
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
