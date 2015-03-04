package org.provebit.ui.daemon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DaemonController implements ActionListener {
	DaemonModel model;
	DaemonView view;
	
	public DaemonController(DaemonModel model, DaemonView view) {
		this.model = model;
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
