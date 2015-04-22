package org.provebit.ui.wallets;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class WalletView extends JPanel implements Observer {
	private static final long serialVersionUID = 1428328297228370330L;
	private WalletModel model;
	private WalletController controller;
	private ArrayList<JPanel> walletsViews;
	private JScrollPane listScrollPane;
	
	private WithdrawFrame withdraw;
	
	public WalletView(WalletModel model) {
		this.model = model;
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		this.controller = new WalletController(model, this);
		
		walletsViews = new ArrayList<JPanel>();
		// TODO actually add the scroll pane
		listScrollPane = new JScrollPane();
			
		// Add all of the current wallets
		walletsViews.add(new org.provebit.systems.bitcoin.ui.gui.WalletPanel().getPanel());
		
		for (JPanel wallet: walletsViews){
			this.add(wallet);
		}
		

	}

	
	@Override
	public void update(Observable o, Object arg) {
		if(arg instanceof String){
			if (arg.equals("OpenSend")){
				withdraw.setVisible(true);
				withdraw.setLocationRelativeTo(this);
			} else if(arg.equals("CloseWithdraw")){
				withdraw.setVisible(false);
			}
		}
		
	}

}
