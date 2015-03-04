package org.provebit.ui.old;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainWalletView extends JPanel{
	
	JFrame addWalletFrame;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MainWalletView(WalletListing[] wallets){
		addWalletFrame = new JFrame("Hello ");
		addWalletFrame.add(new JButton("Hello"));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// Add all of the current wallets
		for(WalletListing listing: wallets){
			this.add(listing);
		}
		
		// Add the "add wallet" button
		JButton addWalletButton = new JButton("+Add Wallet");
		addWalletButton.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                addWalletFrame.pack();
                addWalletFrame.setVisible(true);
            }
		});
		addWalletButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(addWalletButton);
	}
	
	/**
	 * Test code to see the frame displayed
	 * @param args
	 */
	public static void main(String[] args){
		WalletListing [] w = {new WalletListing("Bitcoin Wallet", 10, "BTC"), new WalletListing("Litecoin Wallet", 10, "LTC")};
		MainWalletView m = new MainWalletView(w);
		JFrame frame = new JFrame();
		frame.add(m);
		frame.pack();
		frame.setVisible(true);
	}

}
