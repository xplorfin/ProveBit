package org.provebit.ui.wallet;

import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

public class WalletView extends JPanel implements Observer {
	WalletModel model;
	
	public WalletView(WalletModel model) {
		this.model = model;
	}
	
	public void addController(ActionListener controller) {
		
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
