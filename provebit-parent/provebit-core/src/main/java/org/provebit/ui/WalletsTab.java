package org.provebit.ui;

import javax.swing.JPanel;

import org.provebit.ui.wallets.WalletController;
import org.provebit.ui.wallets.WalletModel;
import org.provebit.ui.wallets.WalletView;

public class WalletsTab {
	private WalletModel model;
	private WalletView view;
	// TODO: unused 'controller' member variable
	@SuppressWarnings("unused")
	private WalletController controller;

	public WalletsTab() {
		model = new WalletModel();
		view = new WalletView(model);
		model.addObserver(view);
		controller = new WalletController(model, view);
	}

	public JPanel getPanel() {
		return view;
	}
}
