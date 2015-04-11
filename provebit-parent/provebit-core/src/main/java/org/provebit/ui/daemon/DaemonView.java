package org.provebit.ui.daemon;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;

import org.provebit.ui.daemon.DaemonController.DaemonNotification;

public class DaemonView extends JPanel implements Observer {
	private static final long serialVersionUID = 5876970360392816111L;
	private static final String ADDFILESTRING = "Add Files to Monitor...";
	private static final String REMOVEFILESTRING = "Remove Files...";
	private static final String[] RECURSIVEOPTIONS = {"No","Yes"};
	private DaemonModel model;
	private JButton addFileButton, removeFileButton, startDaemonButton, suspendDaemonButton, updateIntervalButton, showLogButton, refreshLogButton;
	private JLabel trackedFilesLabel, daemonStatusLabel;
	private JList<String> fileList;
	private JTextField runPeriodInput;
	private JScrollPane listScrollPane;
	private JFrame logFrame, optionFrame;
	private JPanel logPanel;
	private JTextPane logTextPane;
	private JFileChooser fileSelector;
	private JScrollPane logScrollPane;
	private JOptionPane recursiveOptionPane;
	private List<JButton> buttons;
	
	/**
	 * Constructor
	 * @param model - DaemonModel
	 */
	public DaemonView(DaemonModel model) {
		this.model = model;
		
		this.setLayout(new MigLayout("", "[]", "[]20[]5[]20[]5[]5[]"));
		buttons = new ArrayList<JButton>();
		
		setupButtons();
		setupList();
		setupLogFrame();
		
		fileSelector = new JFileChooser();
		fileSelector.setMultiSelectionEnabled(true);
		fileSelector.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		runPeriodInput = new JTextField("");
		runPeriodInput.setText("1");
		daemonStatusLabel = new JLabel("Daemon status: " + model.getDaemonStatus());
		optionFrame = new JFrame();
		optionFrame.setVisible(false);
		
		addControls();
		
		setVisible(true);
	}

	private void addControls() {
		add(addFileButton, "split 2");
		add(removeFileButton, "wrap");
		add(trackedFilesLabel, "wrap");
		add(listScrollPane, "span, grow, push, wrap");
		add(new JLabel("Daemon Run Period: "), "split 3");
		add(runPeriodInput, "width :50");
		add(new JLabel("sec"), "wrap");
		add(startDaemonButton, "split 3");
		add(suspendDaemonButton);
		add(updateIntervalButton, "wrap");
		add(daemonStatusLabel, "wrap");
		add(showLogButton, "");
	}
	
	private void setupButtons() {
		addFileButton = new JButton(ADDFILESTRING);
		addFileButton.setActionCommand("addFile");
		buttons.add(addFileButton);
		
		removeFileButton = new JButton(REMOVEFILESTRING);
		removeFileButton.setActionCommand("removeFiles");
		buttons.add(removeFileButton);
		
		startDaemonButton = new JButton("Start Daemon");
		startDaemonButton.setActionCommand("startDaemon");
		buttons.add(startDaemonButton);
		
		suspendDaemonButton = new JButton("Suspend Daemon");
		suspendDaemonButton.setActionCommand("suspendDaemon");
		buttons.add(suspendDaemonButton);
		
		showLogButton = new JButton("Show Log");
		showLogButton.setActionCommand("showLog");
		buttons.add(showLogButton);
		
		refreshLogButton = new JButton("Refresh Log");
		refreshLogButton.setActionCommand("refreshLog");
		buttons.add(refreshLogButton);
		
		updateIntervalButton = new JButton("Update Interval");
		updateIntervalButton.setActionCommand("updateInterval");
		buttons.add(updateIntervalButton);
	}

	private void setupList() {
		trackedFilesLabel = new JLabel("Tracking");
		fileList = new JList<String>(model.getTrackedFileStrings());
		fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fileList.setLayoutOrientation(JList.VERTICAL);
		fileList.setVisibleRowCount(10);
		listScrollPane = new JScrollPane(fileList);
	}
	
	private void setupLogFrame() {
		logFrame = new JFrame("Daemon Log");
		logFrame.setSize(500, 800);
		logPanel = new JPanel(new MigLayout());
		logTextPane = new JTextPane();
		logPanel.add(refreshLogButton, "span, wrap");
		logScrollPane = new JScrollPane(logTextPane);
		logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		logScrollPane.setPreferredSize(new Dimension(500, 800));
		logPanel.add(logScrollPane);
		logFrame.add(logPanel);
		logFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		logFrame.setVisible(false);
	}
	
	/**
	 * Get recursive status of a given directory
	 * @param dirName - Directory to ask user to choose status of
	 * @return - true if directory should be tracked recursively, false o/w
	 */
	public boolean getRecursiveOption(String dirName) {
		int choice;
		optionFrame.setVisible(true);
		choice = JOptionPane.showOptionDialog(optionFrame, 
									 "Should " + dirName + " track sub directories?", 
									 "Recursive Status", 
									 JOptionPane.YES_NO_OPTION, 
									 JOptionPane.QUESTION_MESSAGE,
									 null, 
									 RECURSIVEOPTIONS,
									 RECURSIVEOPTIONS[0]);
		optionFrame.setVisible(false);
		return (choice == 1) ? true : false;
	}

	public void addController(ActionListener controller) {
		for (JButton button : buttons) {
			button.addActionListener(controller);
		}
		fileSelector.addActionListener(controller);
	}
	
	public List<String> getSelectedFiles() {
		return fileList.getSelectedValuesList();
	}

	@Override
	/**
	 * Called by observables (DaemonModel) when a change
	 * occurs that requires a view update
	 * 
	 * @param o - DaemonModel or null
	 * @param arg - object relevant to update
	 */
	public void update(Observable o, Object arg) {
		if (arg instanceof DaemonNotification) {
			DaemonNotification notification = (DaemonNotification) arg;
			switch(notification) {
				case DAEMONSTATUS:
					daemonStatusLabel.setText("Daemon status: " + model.getDaemonStatus().toString());
					break;
				case SHOWLOG:
					logFrame.setLocationRelativeTo(this);
					logFrame.setVisible(true);
					break;
				case SHOWFILESELECT:
					fileSelector.showOpenDialog(null);
					break;
				case UPDATETRACKING:
					updateTrackingList();
					break;
				default:
					break;
			}
		}
	}
	
	/**
	 * Update the list of tracked files
	 */
	private void updateTrackingList() {
		fileList.setListData(model.getTrackedFileStrings());
	}
	
	/**
	 * Returns the desired period runtime
	 * @return desired period, or -1 if period invalid
	 */
	public int getPeriod() {
		int period;
		try {
			period = Integer.parseInt(runPeriodInput.getText());
		} catch (NumberFormatException nfe) {
			return -1;
		}
		return period*1000;
	}
	
	/**
	 * Updates the log panes text 
	 * @param logData - Log data
	 */
	public void updateLog(String logData) {
		logTextPane.setText(logData);
	}

}
