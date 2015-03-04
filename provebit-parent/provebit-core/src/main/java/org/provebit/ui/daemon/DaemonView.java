package org.provebit.ui.daemon;

import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class DaemonView extends JPanel implements Observer {
	private static final long serialVersionUID = 5876970360392816111L;
	private static final String ADDFILESTRING = "Add Files to Monitor...";
	private static final String REMOVEFILESTRING = "Remove Files...";
	private DaemonModel model;
	private JButton addFileButton;
	private JButton removeFileButton;
	private JList fileList;
	private JTextField runPeriodInput;
	
	public DaemonView(DaemonModel model) {
		this.model = model;
		
		addFileButton = new JButton(ADDFILESTRING);
		removeFileButton = new JButton(REMOVEFILESTRING);
		
		setUpList();
		runPeriodInput = new JTextField();
		
		add(addFileButton);
		add(removeFileButton);
		add(runPeriodInput);
		setVisible(true);
	}

	/**
	 * Private helper for setting up the List
	 */
	@SuppressWarnings("rawtypes")
	private void setUpList() {
		fileList = new JList();
		fileList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		fileList.setLayoutOrientation(JList.VERTICAL);
		fileList.setVisibleRowCount(7);
	}
	
	public void addController(ActionListener controller) {
		
	}
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
