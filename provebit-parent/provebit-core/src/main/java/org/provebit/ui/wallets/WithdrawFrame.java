package org.provebit.ui.wallets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class WithdrawFrame extends JFrame{
	private static final long serialVersionUID = -8222307005319903652L;

	public WithdrawFrame(WalletController controller){
		this.setLayout(new MigLayout("","[]10[]", "[]20[]20[]40[]"));
		this.setTitle("Send from Provebit wallet");
	
		
		JLabel sWalletAddress = new JLabel("Destination Wallet Address: ");
		add(sWalletAddress);
		
		JTextField addressField = new JTextField("" , 15);
		add(addressField, "wrap");
		
		JLabel amount = new JLabel("Amount: ");
		add(amount);
		
		JTextField amountField = new JTextField("" , 5);
		add(amountField, "cell 0 1");
		
		// Add the deposit and send buttons
		JButton cancelButton = new JButton("Cancel");
		JButton sendButton = new JButton("Send");
		
		cancelButton.addActionListener(controller);
		sendButton.addActionListener(controller);
		
		cancelButton.setActionCommand("CancelWithdraw");
		sendButton.setActionCommand("SendWithdraw");
		
		add(cancelButton, "cell 0 3");
		add(sendButton, "align right");
		
		pack();
	}


}
