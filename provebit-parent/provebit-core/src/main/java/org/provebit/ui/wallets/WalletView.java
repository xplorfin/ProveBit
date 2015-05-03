package org.provebit.ui.wallets;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

public class WalletView extends JPanel implements Observer {
	private static final long serialVersionUID = 1428328297228370330L;
	// TODO implement the wallet model and controller 
	@SuppressWarnings("unused")
	private WalletModel model;
	@SuppressWarnings("unused")
	private WalletController controller;
	private ArrayList<JPanel> walletsViews;
	
	/**
	 * WalletView constructor
	 * 
	 * @param model
	 */
	public WalletView(WalletModel model) {
		this.model = model;
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		this.controller = new WalletController(model, this);
		
		walletsViews = new ArrayList<JPanel>();
		
		
			
		// Add all of the current wallets
		walletsViews.add(new org.provebit.systems.bitcoin.ui.gui.WalletPanel().getPanel());
		
		for (JPanel wallet: walletsViews){
			this.add(wallet);
		}
		

	}

	
	@Override
	public void update(Observable o, Object arg) {
		
	}

}
