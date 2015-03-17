package org.provebit.systems.bitcoin.ui.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;

import net.miginfocom.swing.MigLayout;

public class WalletView extends JPanel implements Observer {

	private JButton sendButton;
	private WalletModel model;
	private JLabel balance;
	private JLabel addressLabel;
	private JTextPane address;
	
	public WalletView(WalletModel model){
		this.model = model;
		setLayout(new MigLayout("", "[]", "[][]10[]"));
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Add the name of the wallet
		JLabel nameField = new JLabel("Bitcoin");
		add(nameField, "wrap");
		
		balance = new JLabel();
		updateBalance();
		add(balance, "wrap");
		
		addressLabel = new JLabel("Address:");
		add(addressLabel, "split 2");
		
		address = new JTextPane();
		///address.setContentType("text/html"); // let the text pane know this is what you want
		address.setEditable(false);
		address.setBackground(null);
		address.setBorder(null);
		add(address, "pushx, wrap");
		updateAddress();
		
		sendButton = new JButton("Send");
		
		sendButton.setActionCommand("send");
		
		//sendButton.addActionListener(controller);
		
		add(sendButton);
	}
	
	private void updateBalance() {
		balance.setText("Balance: " + model.getBalance());
	}
	
	private void updateAddress() {
		address.setText(model.getReceivingAddress());
	}

	@Override
	public void update(Observable o, Object arg) {
		updateBalance();
		updateAddress();
	}

	public void addController(WalletController controller) {
		sendButton.addActionListener(controller);
	}

	public void sendPrompt() {
		JPanel dialog = new JPanel(new MigLayout());
		dialog.setPreferredSize(new Dimension(350, 120));
		
		JLabel labelAddress = new JLabel("Address:");
		JTextField address = new JTextField("");
		
		dialog.add(labelAddress, "");
		dialog.add(address, "pushx, width 280, wrap");
		
		JLabel labelAmount = new JLabel("Amount:");
		JTextField amount = new JTextField("");
		
		dialog.add(labelAmount, "");
		dialog.add(amount, "pushx, width 280, wrap");
		
		JLabel info = new JLabel("<html><p>Note that " + 
				Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.toFriendlyString() + " will be used for transaction fees.</p></html>");
		
		dialog.add(info, "span, width 280");
		
		int result = JOptionPane.showConfirmDialog(this,
				dialog, "Send coins",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}
		
		try {
			Coin sendAmount = Coin.parseCoin(amount.getText());
			model.simpleSendCoins(sendAmount, address.getText());
		} catch (IllegalArgumentException e) {
			// error message window for wrong amount format
			JOptionPane.showMessageDialog(this,
					"Please check your amount is a valid number of BTC.",
					"Invalid Amount", JOptionPane.ERROR_MESSAGE);
		} catch (AddressFormatException e) {
			// error message window for wrong address format
			JOptionPane.showMessageDialog(this,
					"Please check your address is in the correct format.",
					"Invalid Address", JOptionPane.ERROR_MESSAGE);
		} catch (InsufficientMoneyException e) {
			// error message window for inefficient money
			Coin missingValue = e.missing;
			JOptionPane.showMessageDialog(this, "You are missing "
					+ missingValue.toFriendlyString() + " BTC "
					+ " necessary to complete the transaction.",
					"Insufficient amount", JOptionPane.ERROR_MESSAGE);
		}
		
	}

}
