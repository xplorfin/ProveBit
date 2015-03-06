package org.provebit.ui.general;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

public class GeneralView extends JPanel implements Observer {
	
	private GeneralModel model;
	
	private JButton certifyFileButton;
	private JButton verifyFileButton;
	private JLabel status;
	private JTable statTable;
	private String[] columnNames;
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
	
	public GeneralView(GeneralModel model) {
		this.model = model;
		setLayout(new MigLayout("", "[]5[]", "[]5[]5[]"));
		
		certifyFileButton = new JButton("Certify File");
		verifyFileButton = new JButton("Verify File");
		add(certifyFileButton, "split 2");
		add(verifyFileButton, "pushx, wrap");
		
		status = new JLabel("Status of ProveBit: you have 1 file(s) being proved");
		add(status, "span");
		
		addStatusTable();
		
	}
	
	private void addStatusTable() {
		columnNames = new String[] {"File","Status"};
		Object[][] data = TEST_DATA;
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
		
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
	
	private class StatusTableModel extends DefaultTableModel {
		@Override
		public boolean isCellEditable(int row, int col){
			return false;
		}
	}

}
