package org.provebit.systems.bitcoin.ui.gui;

import java.awt.Color;
import java.awt.LayoutManager;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class WalletView extends JPanel implements Observer {

	public WalletView(WalletModel model){
		setLayout(new MigLayout("", "[]70[]", "[][]10[]"));
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Add the name of the wallet
		JLabel nameField = new JLabel("Bitcoin");
		add(nameField, "wrap");
		
		// Add the Balance of the wallet
		JLabel balanceField = new JLabel("Balance: " + 0 + " " + "BTC");
		add(balanceField, "wrap");
		
		// Show the Address of the Wallet
		JLabel addressArea = new JLabel("Address: 19VStDyNS5QdMTtV8juQ5sDZcGPyyoLrvi");
		add(addressArea, "wrap");
		
		// Add the deposit and send buttons
		JButton sendButton = new JButton("Send");
		
		sendButton.setActionCommand("Send");
		
		//sendButton.addActionListener(controller);
		
		add(sendButton);
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

	public void addController(WalletController controller) {
		// TODO Auto-generated method stub
		
	}

}
