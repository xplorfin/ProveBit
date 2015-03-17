package org.provebit.ui.wallets;

import java.awt.Dimension;
import java.awt.FlowLayout;
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
	private ArrayList<JPanel> walletsViews;
	private JScrollPane listScrollPane;
	
	private WithdrawFrame withdraw;
	
	public WalletView(WalletModel model) {
		this.model = model;
		setLayout(new FlowLayout(FlowLayout.LEFT));
		//this.setLayout(new MigLayout("","[]",""));
		WalletController controller = new WalletController(model, this);
		//withdraw = new WithdrawFrame(controller);
		walletsViews = new ArrayList<JPanel>();
		// TODO actually add the scroll pane
		listScrollPane = new JScrollPane();
		
		
		// Add all of the current wallets
		walletsViews.add(new org.provebit.systems.bitcoin.ui.gui.WalletPanel().getPanel());
		//walletsViews.add(new org.provebit.systems.bitcoin.ui.gui.WalletPanel().getPanel());
		
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
