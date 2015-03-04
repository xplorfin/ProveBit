package org.provebit.ui;
/**
 * 
 */

/** Swing Imports  */
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;



public class DaemonConfigTab extends JPanel {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5876970360392816111L;
	private static final String ADDFILESTRING = "Add Files to Monitor...";
	private static final String REMOVEFILESTRING = "Remove Files...";
	
	private JButton addFileButton;
	private JButton removeFileButton;
	
	private JList fileList;
	
	private JTextField runPeriodInput;
	
	/** Frame Constructor
	 *
	 */
	public DaemonConfigTab() {
		super();
		
		addFileButton = new JButton(ADDFILESTRING);
		addFileButton.setEnabled(true);
		
		removeFileButton = new JButton(REMOVEFILESTRING);
		removeFileButton.setEnabled(true);
		
		setUpList();
		
		runPeriodInput = new JTextField();
		
		
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
}
