package org.provebit.ui;

import javax.swing.JPanel;

import org.provebit.ui.wallet.WalletController;
import org.provebit.ui.wallet.WalletModel;
import org.provebit.ui.wallet.WalletView;

public class WalletTab {
	private WalletModel model;
	private WalletView view;
	private WalletController controller;
	
	public WalletTab() {
		model = new WalletModel();
		view = new WalletView(model);
		model.addObserver(view);
		controller = new WalletController(model, view);
		view.addController(controller);
	}
	
	public JPanel getPanel() {
		return view;
	}
}
