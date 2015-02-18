package org.provebit.interfaces.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

public class GUI {
	JFrame frame = new JFrame("ProveBit");
	JMenuBar menuBar = new JMenuBar();
	JMenu menuAbout = new JMenu("About");
	JMenuItem menuItemAboutUs = new JMenuItem("About Us");
	JPanel panelMain = new JPanel();
	JLabel labelTitle = new JLabel("ProveBit");
	JButton btnProve = new JButton("Prove a File");
	JButton btnVerify = new JButton("Verify a Certificate");
	JButton btnWallet = new JButton("Bitcoin Wallet");
	
	public GUI() {
		// Set up menu bar and menu
		menuAbout.add(menuItemAboutUs);
		menuBar.add(menuAbout);
		frame.setJMenuBar(menuBar);
		// Set up main panel layout
		panelMain.setLayout(new MigLayout());
		panelMain.add(btnProve);
		panelMain.add(btnVerify);
		panelMain.add(btnWallet);
		panelMain.add(labelTitle, "dock north, gapleft 150");
		// Pack up frame
		frame.add(panelMain);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new GUI();
			}
		});
	}
}
