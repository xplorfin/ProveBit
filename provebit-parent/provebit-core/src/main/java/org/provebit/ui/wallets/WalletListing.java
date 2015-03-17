package org.provebit.ui.wallets;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

public class WalletListing extends JPanel{
	private JButton sendButton;
	private JLabel nameField, balanceField, addressField;

	
	public WalletListing(String name, int amount, String type, WalletController controller){
		setLayout(new MigLayout("", "[]40[]", "[][][]"));
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		nameField = new JLabel(name);
		sendButton = new JButton("Send");
		sendButton.setActionCommand("Send");
		sendButton.addActionListener(controller);
		balanceField = new JLabel("Balance: " + amount + " " + type);
		addressField = new JLabel("Address: 19VStDyNS5QdMTtV8juQ5sDZcGPyyoLrvi");
		
		// Add the name of the wallet
		add(nameField);
		
		// Add the send button
		add(sendButton, "wrap");
		
		// Add the Balance of the wallet
		add(balanceField, "wrap");
		
		// Show the Address of the Wallet
		add(addressField, "wrap");
		
	}


}
