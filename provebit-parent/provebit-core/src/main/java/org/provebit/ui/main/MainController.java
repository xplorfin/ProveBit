package org.provebit.ui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainController implements ActionListener {	
	private MainModel model;
	private MainView view;
	
	public MainController(MainModel model, MainView view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
			case("File-Quit"):
				System.exit(0);
				break;
			case("About-About Us"):
				view.update(model, new String("showAbout"));
				break;
			default:
				break;
		}
		
	}
}
