package org.provebit.ui.daemon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.provebit.daemon.Log;

public class DaemonController implements ActionListener, ListSelectionListener {
	public enum DaemonNotification{DAEMONSTATUS, SHOWLOG, SHOWFILESELECT, UPDATETRACKING}; // Enum to hold notification types for observers
	DaemonModel model;
	DaemonView view;

	public DaemonController(DaemonModel model, DaemonView view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String data;
		switch(e.getActionCommand()) {
			case "startDaemon":
				int period = view.getPeriod();
				if (period != -1 && model.getNumTracked() > 0) {
					model.startDaemon(view.getPeriod());
				}
				break;
			case "stopDaemon":
				model.stopDaemon();
				break;
			case "showLog":
				view.update(model, DaemonNotification.SHOWLOG);
				data = (model.getDaemonLog() == null) ? "Daemon Offline" : model.getDaemonLog().toString();
				view.updateLog(data);
				break;
			case "addFile":
				view.update(model, DaemonNotification.SHOWFILESELECT);
				break;
			case "removeFiles":
				for (String file : view.getSelectedFiles()) {
					model.removeFileFromTree(new File(file));
				}
				break;
			case "refreshLog":
				Log log = model.getDaemonLog();
				data = (log == null) ? "Daemon Offline" : log.toString();
				view.updateLog(data);
				break;
			default:
				break;
		}
		
		if (e.getSource() instanceof JFileChooser) {
			boolean recursive = false;
			JFileChooser fileSelector = (JFileChooser) e.getSource();
			File[] files = fileSelector.getSelectedFiles();
			for (File file : files) {
				if (!model.isTracking(file)) {
					if (file.isDirectory()) {
						recursive = view.getRecursiveOption(file.getName());
					}
					model.addFileToTree(file, recursive);
				}
				
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
