package org.provebit.ui.wallet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class WithdrawFrame extends JFrame{
	
	public WithdrawFrame(WalletController controller){
		this.setLayout(new MigLayout("","[]10[]", "[]20[]20[]40[]"));
		this.setTitle("Send from Provebit wallet");
	
		
		JTextArea sWalletAddress = new JTextArea("Destination Wallet Address: ");
		sWalletAddress.setOpaque(false);
		add(sWalletAddress);
		
		JTextField addressField = new JTextField("" , 15);
		addressField.setOpaque(false);
		add(addressField, "wrap");
		
		JTextArea amount = new JTextArea("Amount: ");
		amount.setOpaque(false);
		add(amount);
		
		JTextField amountField = new JTextField("" , 5);
		amountField.setOpaque(false);
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
