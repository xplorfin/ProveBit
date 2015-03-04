package org.provebit.ui.general;

import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

public class GeneralView extends JPanel implements Observer {
	
	private GeneralModel model;
	
	public GeneralView(GeneralModel model) {
		this.model = model;
	}
	
	public void addController(ActionListener controller) {
		
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
