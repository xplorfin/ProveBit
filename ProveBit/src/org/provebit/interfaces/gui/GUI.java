package org.provebit.interfaces.gui;

import javax.swing.SwingUtilities;

public class GUI {

	public GUI() {
		
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
