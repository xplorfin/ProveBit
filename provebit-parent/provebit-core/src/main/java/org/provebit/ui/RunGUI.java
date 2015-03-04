package org.provebit.ui;

import javax.swing.SwingUtilities;

import org.provebit.ui.main.MainController;
import org.provebit.ui.main.MainModel;
import org.provebit.ui.main.MainView;

public class RunGUI {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				startGUI();
			}
		});
	}
	
	public static void startGUI() {
		MainModel model = new MainModel();
		MainView view = new MainView(model);
		model.addObserver(view);
		MainController controller = new MainController(model, view);
		view.addController(controller);
	}
}
