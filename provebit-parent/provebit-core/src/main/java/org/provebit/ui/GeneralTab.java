package org.provebit.ui;

import javax.swing.JPanel;

import org.provebit.ui.general.GeneralController;
import org.provebit.ui.general.GeneralModel;
import org.provebit.ui.general.GeneralView;

public class GeneralTab {
	private GeneralModel model;
	private GeneralView view;
	private GeneralController controller;
	
	public GeneralTab() {
		model = new GeneralModel();
		view = new GeneralView(model);
		model.addObserver(view);
		controller = new GeneralController(model, view);
		view.addController(controller);
	}
	
	public JPanel getPanel() {
		return view;
	}
}
