package org.provebit.ui.daemon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.provebit.daemon.Log;

public class DaemonController implements ActionListener, ListSelectionListener {
	public enum DaemonNotification{DAEMONSTATUS, SHOWLOG}; // Enum to hold notification types for observers
	DaemonModel model;
	DaemonView view;

	public DaemonController(DaemonModel model, DaemonView view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
			case "startDaemon":
				// Start daemon
				break;
			case "stopDaemon":
				model.stopDaemon();
				break;
			case "showLog":
				view.update(model, DaemonNotification.SHOWLOG);
				break;
			case "addFiles":
				// Add files
				break;
			case "removeFiles":
				// Remove files
				break;
			case "refreshLog":
				Log log = model.getDaemonLog();
				String data = (log == null) ? "Daemon Offline" : log.toString();
				view.updateLog(data);
				break;
			default:
				break;
		}

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
