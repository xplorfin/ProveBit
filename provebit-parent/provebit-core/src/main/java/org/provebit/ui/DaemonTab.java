package org.provebit.ui;

import javax.swing.JPanel;

import org.provebit.ui.daemon.DaemonController;
import org.provebit.ui.daemon.DaemonModel;
import org.provebit.ui.daemon.DaemonView;

public class DaemonTab {
	private DaemonModel model;
	private DaemonView view;
	private DaemonController controller;
	
	public DaemonTab() {
		model = new DaemonModel();
		view = new DaemonView(model);
		model.addObserver(view);
		controller = new DaemonController(model, view);
		view.addController(controller);
	}
	
	public JPanel getPanel() {
		return view;
	}
}
