package org.provebit.ui.wallets;

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
		switch(e.getActionCommand()) {
			case "Send":
				view.update(model, "OpenSend");
				break;
			case "CancelWithdraw":
				view.update(model, "CloseWithdraw");
				break;
			case "SendWithdraw":
				view.update(model, "CloseWithdraw");
				break;
		}
	}

}
