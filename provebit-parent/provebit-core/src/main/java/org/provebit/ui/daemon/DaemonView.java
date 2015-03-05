package org.provebit.ui.daemon;

import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;

public class DaemonView extends JPanel implements Observer {
	private static final long serialVersionUID = 5876970360392816111L;
	private static final String ADDFILESTRING = "Add Files to Monitor...";
	private static final String REMOVEFILESTRING = "Remove Files...";
	private static final String[] TESTLISTDATA = {
		"alphabet.png",
		"juice.txt",
		"secrets.txt",
		"insurenceInfo.txt",
		"ipsumlorum.png",
		"funds.wallet",
		"escapePlan.tiff",
		"cowlevel.wav",
		"ZAP.app",
		"file1.txt",
		"file34.txt",
		"thatotherfile.txt"
		};
	private DaemonModel model;
	private JButton addFileButton;
	private JButton removeFileButton;
	private JList<String> fileList;
	private JTextField runPeriodInput;
	private JScrollPane listScrollPane;
	
	public DaemonView(DaemonModel model) {
		this.model = model;
		this.setLayout(new MigLayout("","[][][]","[]5[]5[][]"));
		
		addFileButton = new JButton(ADDFILESTRING);
		removeFileButton = new JButton(REMOVEFILESTRING);
		
		setUpList();
		
		runPeriodInput = new JTextField("");
		
		add(addFileButton);
		add(removeFileButton, "wrap");
		add(listScrollPane, "wrap");
		add(new JLabel("Daemon Run Period: "));
		add(runPeriodInput, "wrap, width :480");
		setVisible(true);
	}

	/**
	 * Private helper for setting up the List
	 */
	private void setUpList() {
		fileList = new JList<String>(TESTLISTDATA);
		fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fileList.setLayoutOrientation(JList.VERTICAL);
		fileList.setVisibleRowCount(10);
		listScrollPane = new JScrollPane(fileList);
	}
	
	public void addController(ActionListener controller) {
		
	}
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
