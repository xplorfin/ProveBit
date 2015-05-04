package org.provebit.ui.general;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.provebit.proof.ProofManager;

import net.miginfocom.swing.MigLayout;

public class GeneralView extends JPanel implements Observer {
	private static final long serialVersionUID = 2795934197957164906L;
	private GeneralModel model;
	private JButton certifyFileButton;
	private JButton verifyFileButton;
	private JTable statTable;
	private String[] columnNames = new String[] {"File","Status"};;
	private StatusTableModel tableModel;
	
	private final static String[][] TEST_DATA = { 
		new String[] {"alphabet.png", "In progress"}, 
		new String[] {"juice.txt", "Complete"},
		new String[] {"secrets.txt", "Complete"}, 
		new String[] {"insurenceInfo.txt", "Complete"}, 
		new String[] {"ipsumlorum.png", "Complete"},
		new String[] {"funds.wallet", "Complete"}, 
		new String[] {"escapePlan.tiff", "Complete"}, 
		new String[] {"cowlevel.wav", "Complete"}, 
		new String[] {"ZAP.app", "Complete"},
		new String[] {"file1.txt", "Complete"}, 
		new String[] {"file34.txt", "Complete"}, 
		new String[] {"thatotherfile.txt", "Complete"},
	};
	
	/**
	 * GeneralView constructor
	 * 
	 * @param model
	 * TODO: fully implement MVC interaction
	 */
	public GeneralView(GeneralModel model) {
		this.model = model;
		setLayout(new MigLayout("", "[]5[]", "[]5[]5[]"));
		
		certifyFileButton = new JButton("Certify File");
		certifyFileButton.setActionCommand("certify");
		verifyFileButton = new JButton("Verify File");
		verifyFileButton.setActionCommand("verify");
		verifyFileButton.setEnabled(false); // remove disabling when implemented
		add(certifyFileButton, "split 2");
		add(verifyFileButton, "pushx, wrap");
		
		addStatusTable();
		
	}
	
	private void addStatusTable() {
		Object[][] data = model.getStatusList(); // TEST_DATA;
		tableModel = new StatusTableModel();
		tableModel.setDataVector(data, columnNames);
		statTable = new JTable(tableModel);
		JPanel panel= new JPanel();
		panel.setLayout(new BorderLayout());
		JScrollPane tableScrollPane= new JScrollPane(statTable);
		statTable.setFillsViewportHeight(true);
		panel.add(tableScrollPane,BorderLayout.CENTER);
		this.add(panel,"span, grow, push");
	}
	
	public void addController(ActionListener controller) {
		certifyFileButton.addActionListener(controller);
		verifyFileButton.addActionListener(controller);
	}

	@Override
	public void update(Observable o, Object arg) {
		redrawStatus();
	}
	
	private void redrawStatus() {
		tableModel.setDataVector(model.getStatusList(), columnNames);
	}
	
	private class StatusTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int col){
			return false;
		}
	}

	public void cerifyPrompt() {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setApproveButtonText("Certify");
		fileChooser.setDialogTitle("Choose File to Certify");
		
		int res = fileChooser.showOpenDialog(this);
		if (res != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File certFile = fileChooser.getSelectedFile();
		
		Thread hashThread = new Thread() {
			@Override
			public void run() {
				byte[] digest = null;
				try {
					FileInputStream fis = new FileInputStream(certFile);
					digest = org.apache.commons.codec.digest.DigestUtils.sha256(fis);
					fis.close();
					Transaction tx = model.proofTX(digest);
					PostHash postHash = new PostHash(true, certFile, digest, tx, null);
					SwingUtilities.invokeLater(postHash);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InsufficientMoneyException e) {
					PostHash postHash = new PostHash(false, certFile, digest, null, e);
					SwingUtilities.invokeLater(postHash);
				}

			}
		};
		hashThread.start();
	}
	
	private class PostHash implements Runnable {
		// TODO: unused 'hash' member in PostHash class
		@SuppressWarnings("unused")
		private byte[] hash;
		private boolean success;
		private Transaction tx;
		private InsufficientMoneyException ise;
		private File file;
		
		public PostHash(boolean success, File file, byte[] hash, Transaction tx, InsufficientMoneyException e) {
			this.hash = hash;
			this.file = file;
			this.success = success;
			this.tx = tx;
			this.ise = e;
		}
		
		@Override
		public void run() {
			if (success) {
				ProofManager.INSTANCE.addProof(tx, file, hash);
				
				JPanel txidDialog = new JPanel(new MigLayout());
				txidDialog.add(new JLabel("TXID:"));
				JTextPane txidSelectable = new JTextPane();
				txidSelectable.setText(tx.getHash().toString());
				txidSelectable.setEditable(false);
				txidSelectable.setBackground(null);
				txidSelectable.setBorder(null);
				txidDialog.add(txidSelectable);
				
				JOptionPane.showMessageDialog(GeneralView.this, txidDialog, "Proof Prepared", JOptionPane.INFORMATION_MESSAGE);
				
			} else if (ise != null) {
				// error message window for inefficient money
				Coin missingValue = ise.missing;
				JOptionPane.showMessageDialog(GeneralView.this, "You are missing "
						+ missingValue.toFriendlyString() + " BTC "
						+ " necessary to complete the proof transaction.",
						"Insufficient amount", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
