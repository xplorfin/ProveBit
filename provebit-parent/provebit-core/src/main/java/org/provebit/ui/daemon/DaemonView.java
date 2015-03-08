package org.provebit.ui.daemon;

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
	private static final String[] TESTLISTDATA = { "alphabet.png", "juice.txt",
			"secrets.txt", "insurenceInfo.txt", "ipsumlorum.png",
			"funds.wallet", "escapePlan.tiff", "cowlevel.wav", "ZAP.app",
			"file1.txt", "file34.txt", "thatotherfile.txt" };
	private DaemonModel model;
	private JButton addFileButton, removeFileButton, startDaemonButton, stopDaemonButton, showLogButton, refreshLogButton;
	private JLabel trackedFiles, daemonStatusLabel;
	private JList<String> fileList;
	private JTextField runPeriodInput;
	private JScrollPane listScrollPane;
	private JFrame logFrame;
	private JPanel logPanel;
	private JTextPane logTextPane;
	private JFileChooser fileSelector;
	
	private List<JButton> buttons;
	public DaemonView(DaemonModel model) {
		this.model = model;
		this.setLayout(new MigLayout("", "[][][]", "[]5[]5[][]"));
		buttons = new ArrayList<JButton>();
		
		setupButtons();
		setupList();
		setupLogFrame();
		
		fileSelector = new JFileChooser();
		fileSelector.setMultiSelectionEnabled(true);
		runPeriodInput = new JTextField("");
		daemonStatusLabel = new JLabel("Daemon status: " + model.getDaemonStatus());
		
		addControls();
		
		setVisible(true);
	}

	private void addControls() {
		add(addFileButton);
		add(removeFileButton, "wrap");
		add(listScrollPane, "wrap");
		add(new JLabel("Daemon Run Period: "));
		add(runPeriodInput, "width :480");
		add(new JLabel("minutes"), "wrap");
		add(startDaemonButton);
		add(stopDaemonButton, "wrap");
		add(daemonStatusLabel, "span, wrap");
		add(showLogButton);
	}
	
	private void setupButtons() {
		addFileButton = new JButton(ADDFILESTRING);
		addFileButton.setActionCommand("addFile");
		buttons.add(addFileButton);
		
		removeFileButton = new JButton(REMOVEFILESTRING);
		removeFileButton.setActionCommand("removeFile");
		buttons.add(removeFileButton);
		
		startDaemonButton = new JButton("Start Daemon");
		startDaemonButton.setActionCommand("startDaemon");
		buttons.add(startDaemonButton);
		
		stopDaemonButton = new JButton("Stop Daemon");
		stopDaemonButton.setActionCommand("stopDaemon");
		buttons.add(stopDaemonButton);
		
		showLogButton = new JButton("Show Log");
		showLogButton.setActionCommand("showLog");
		buttons.add(showLogButton);
		
		refreshLogButton = new JButton("Refresh Log");
		refreshLogButton.setActionCommand("refreshLog");
		buttons.add(refreshLogButton);
	}

	/**
	 * Private helper for setting up the List
	 */
	private void setupList() {
		fileList = new JList<String>(TESTLISTDATA);
		fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fileList.setLayoutOrientation(JList.VERTICAL);
		fileList.setVisibleRowCount(10);
		listScrollPane = new JScrollPane(fileList);
	}
	
	private void setupLogFrame() {
		// Setup log panel
		logFrame = new JFrame("Daemon Log");
		logFrame.setSize(500, 800);
		logPanel = new JPanel(new MigLayout());
		logTextPane = new JTextPane();
		logPanel.add(refreshLogButton, "span, wrap");
		logPanel.add(logTextPane);
		logFrame.add(logPanel);
		logFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		logFrame.setVisible(false);
	}

	public void addController(ActionListener controller) {
		for (JButton button : buttons) {
			button.addActionListener(controller);
		}
		fileSelector.addActionListener(controller);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof DaemonNotification) {
			DaemonNotification notification = (DaemonNotification) arg;
			switch(notification) {
				case DAEMONSTATUS:
					daemonStatusLabel.setText("Daemon status: " + model.getDaemonStatus());
					break;
				case SHOWLOG:
					logFrame.setLocationRelativeTo(this);
					logFrame.setVisible(true);
					break;
				case SHOWFILESELECT:
					System.out.println("SHOWING FILE SELECTOR");
					fileSelector.showOpenDialog(null);
					break;
				default:
					break;
			}
		}
	}
	
	/**
	 * Updates the log panes text 
	 * @param logData - Log data
	 */
	public void updateLog(String logData) {
		logTextPane.setText(logData);
	}

}
