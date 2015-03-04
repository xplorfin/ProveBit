package org.provebit.ui.advanced;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdvancedController implements ActionListener {
	AdvancedModel model;
	AdvancedView view;
	
	public AdvancedController(AdvancedModel model, AdvancedView view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
