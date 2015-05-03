package org.provebit.systems.bitcoin.ui.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

import org.provebit.proof.Proof;

/**
 * Controller class that controls all functionality related to the Wallet
 * @author noahmalmed
 *
 */
public class WalletController implements ActionListener {

	WalletModel model;
	WalletView view;
	
	public WalletController(WalletModel model, WalletView view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "send":
				// launch a pane to collect input
				view.sendPrompt();
				break;
			case "proveText":
				// test prove hash(text)
				view.provePrompt();
				break;
			case "proveFile":
				view.selectFilePrompt();
				break;
			default:
				break;
		}
		
		// Opens the file chooser for user to select a file to prove
		if(e.getSource() instanceof JFileChooser){
			JFileChooser fileSelector = (JFileChooser) e.getSource();
			File fileToHash = fileSelector.getSelectedFile();
			Proof gennedProof = model.generateProofFromFile(fileToHash);
			view.confirmProofGeneration(gennedProof);
		}
	}
}
