package org.provebit.ui.wallet;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class WalletView extends JPanel implements Observer {
	private WalletModel model;
	private ArrayList<WalletListing> wallets;
	
	private WithdrawFrame withdraw;
	
	public WalletView(WalletModel model) {
		this.model = model;
		WalletController controller = new WalletController(model, this);
		withdraw = new WithdrawFrame(controller);
		wallets = new ArrayList<WalletListing>();
		
		this.setLayout(new MigLayout("","[]",""));
		
		// Add all of the current wallets
		wallets.add(new WalletListing("Bitcoin Wallet", 10, "BTC", controller));
		wallets.add(new WalletListing("Litecoin Wallet", 10, "LTC", controller));
		
		for (WalletListing wallet: wallets){
			this.add(wallet, "wrap");
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
