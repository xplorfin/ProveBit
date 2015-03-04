package org.provebit.ui;

import javax.swing.JPanel;

import org.provebit.ui.advanced.AdvancedController;
import org.provebit.ui.advanced.AdvancedModel;
import org.provebit.ui.advanced.AdvancedView;

public class AdvancedTab {
	private AdvancedModel model;
	private AdvancedView view;
	private AdvancedController controller;
	
	public AdvancedTab() {
		model = new AdvancedModel();
		view = new AdvancedView(model);
		model.addObserver(view);
		controller = new AdvancedController(model, view);
		view.addController(controller);
	}
	
	public JPanel getPanel() {
		return view;
	}
}
