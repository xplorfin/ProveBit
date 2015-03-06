package org.provebit.ui.old;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.provebit.ui.wallet.WalletController;

public class DepositFrame extends JFrame{
	
	public DepositFrame(WalletController controller){
		this.setLayout(new MigLayout("","[]10[]", "[]20[]20[]40[]"));
		this.setTitle("Deposit to Provebit wallet");
		
		JTextArea pWalletAddress = new JTextArea("ProveBit Wallet Address: ");
		pWalletAddress.setOpaque(false);
		add(pWalletAddress);
		
		JTextArea givenWalletAddress = new JTextArea("ASDKK5309FJS09K320J4443");
		givenWalletAddress.setOpaque(false);
		add(givenWalletAddress, "wrap");
		
		JTextArea sWalletAddress = new JTextArea("Source Wallet Address: ");
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
		add(amountField, "cell 0 2");
		
		// Add the deposit and send buttons
		JButton cancelButton = new JButton("Cancel");
		JButton depositButton = new JButton("Deposit");
		
		cancelButton.addActionListener(controller);
		depositButton.addActionListener(controller);
		
		cancelButton.setActionCommand("CancelDeposit");
		depositButton.setActionCommand("DepositDeposit");
		
		add(cancelButton, "cell 0 3");
		add(depositButton, "align right");
		
		pack();
	}
}
