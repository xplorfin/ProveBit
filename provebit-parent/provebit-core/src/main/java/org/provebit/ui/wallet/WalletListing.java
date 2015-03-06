package org.provebit.ui.wallet;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

public class WalletListing extends JPanel{

	
	public WalletListing(String name, int amount, String type, WalletController controller){
		setLayout(new MigLayout("", "[]70[]", "[][]10[]"));
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Add the name of the wallet
		JTextArea nameField = new JTextArea(name);
		nameField.setOpaque(false);
		add(nameField, "wrap");
		
		// Add the Balance of the wallet
		JTextArea balanceField = new JTextArea("Balance: " + amount + " " + type);
		balanceField.setOpaque(false);
		add(balanceField, "wrap");
		
		// Show the Address of the Wallet
		JTextArea addressArea = new JTextArea("Address: 19VStDyNS5QdMTtV8juQ5sDZcGPyyoLrvi");
		addressArea.setOpaque(false);
		add(addressArea, "wrap");
		
		// Add the deposit and send buttons
		JButton sendButton = new JButton("Send");
		
		sendButton.setActionCommand("Send");
		
		sendButton.addActionListener(controller);
		
		add(sendButton);

	}


}
