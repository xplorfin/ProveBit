package org.provebit.systems.bitcoin.ui.gui;

import javax.swing.JPanel;


public class WalletPanel {

	private WalletModel model;
	private WalletView view;
	private WalletController controller;

	public WalletPanel() {
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
