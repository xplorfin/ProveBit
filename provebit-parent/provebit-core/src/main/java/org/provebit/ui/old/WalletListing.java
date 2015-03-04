package org.provebit.ui.old;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class WalletListing extends JPanel{

	
	public WalletListing(String name, int amount, String type){
		this.setLayout(new GridLayout(2,4));
		JPanel[][] panelHolder = new JPanel[2][4];  
		Border blackline = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

		for(int m = 0; m < 2; m++) {
		   for(int n = 0; n < 4; n++) {
		      panelHolder[m][n] = new JPanel();
		      panelHolder[m][n].setLayout(new FlowLayout(FlowLayout.LEFT,3,3));
		      add(panelHolder[m][n]);
		   }
		}
		// Add the name of the wallet
		JTextArea nameField = new JTextArea(name);
		nameField.setOpaque(false);
		panelHolder[0][0].add(nameField);
		
		// Add the Balance of the wallet
		JTextArea balanceField = new JTextArea("Balance: " + amount + " " + type);
		balanceField.setOpaque(false);
		panelHolder[1][0].add(balanceField);
		
		// Add the deposit and send buttons
		JButton depositButton = new JButton("Deposit");
		JButton sendButton = new JButton("Send");
		
		panelHolder[1][2].add(depositButton);
		panelHolder[1][3].add(sendButton);

		
		this.setBorder(blackline);
	}
	
	public static void main(String[] args){
		WalletListing w = new WalletListing("Bitcoin Wallet", 10, "BTC");
		WalletListing a = new WalletListing("Bitcoin Wallet", 10, "BTC");
		JFrame f = new JFrame();
		f.setLayout(new GridLayout(2,1));
		f.add(w);
		f.add(a);
		f.pack();
		f.setVisible(true);
	}

}
