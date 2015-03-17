package org.provebit.systems.bitcoin.ui.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WalletController implements ActionListener {

	WalletModel model;
	WalletView view;
	
	public WalletController(WalletModel model, WalletView view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "send":
				// launch a pane to collect input
				view.sendPrompt();
				break;
			default:
				break;
		}
		
	}
}
