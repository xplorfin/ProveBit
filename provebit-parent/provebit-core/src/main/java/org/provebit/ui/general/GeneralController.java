package org.provebit.ui.general;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GeneralController implements ActionListener {
	private GeneralView view;
	private GeneralModel model;
	
	public GeneralController(GeneralModel model, GeneralView view) {
		this.view = view;
		this.model = model;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
