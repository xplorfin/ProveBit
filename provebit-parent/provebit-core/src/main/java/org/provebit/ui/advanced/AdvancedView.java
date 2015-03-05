package org.provebit.ui.advanced;

import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

public class AdvancedView extends JPanel implements Observer {
	AdvancedModel model;
	
	public AdvancedView(AdvancedModel model) {
		this.model = model;
		
		// Put view controls here
		// Buttons, labels, textboxes, lists, etc...
		
		setVisible(true);
	}
	
	public void addController(ActionListener controller) {
		// Iterate over controls that require an action listener
		// and add 'controller' => object.addActionListener(controller)
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// If there is model implementation (not expected at this point)
		// This callback will be notified of model changes and should
		// appropriately update the view
	}

}
