package org.provebit.ui.old;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.provebit.ui.AddWalletFrame;
import org.provebit.ui.wallet.WalletListing;

public class MainWalletView extends JPanel{
	
	AddWalletFrame addFrame;;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MainWalletView(WalletListing[] wallets){
		addFrame = new AddWalletFrame();
		this.setLayout(new MigLayout());
		
		// Add all of the current wallets
		for(WalletListing listing: wallets){
			this.add(listing, "wrap");
		}
		
		// Add the "add wallet" button
		JButton addWalletButton = new JButton("+Add Wallet");
		addWalletButton.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                addFrame.setVisible(true);
            }
		});
		this.add(addWalletButton, "align center");
	}


}
