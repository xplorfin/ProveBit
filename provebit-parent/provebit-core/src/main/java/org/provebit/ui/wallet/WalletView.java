package org.provebit.ui.wallet;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import net.miginfocom.swing.MigLayout;

public class WalletView extends JPanel implements Observer {
	private WalletModel model;
	private ArrayList<WalletListing> wallets;
	private JList<WalletListing> walletList;
	private JScrollPane listScrollPane;
	
	private WithdrawFrame withdraw;
	
	public WalletView(WalletModel model) {
		this.model = model;
		this.setLayout(new MigLayout("","[]",""));
		WalletController controller = new WalletController(model, this);
		withdraw = new WithdrawFrame(controller);
		wallets = new ArrayList<WalletListing>();
		listScrollPane = new JScrollPane();
		
		
		
		// Add all of the current wallets
		wallets.add(new WalletListing("Bitcoin Wallet", 10, "BTC", controller));
		wallets.add(new WalletListing("Litecoin Wallet", 10, "LTC", controller));
		wallets.add(new WalletListing("Hackurcoin Wallet", 15, "HXC", controller));
		
		for (WalletListing wallet: wallets){
			this.add(wallet, "wrap, span, push");
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
