package org.provebit.ui.advanced;

import java.awt.event.ActionListener;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class AdvancedView extends JPanel implements Observer {
	private static final long serialVersionUID = 3810351363941511450L;
	AdvancedModel model;
	private static final String[] TESTDROPDOWNDATA = {"SHA256", "SHA3"};
	private static final String warning = "Warning: Do not edit these settings if you don't know what to do.";
	private JComboBox<String> advancedDropDown;
	private JButton configButton;
	private JLabel warningLabel;
	private JLabel chooseHashLabel;
	
	
	public AdvancedView(AdvancedModel model) {
		this.model = model;
		setLayout(new MigLayout("","[]","5[]5[][][]"));
		
		warningLabel = new JLabel(warning);
		add(warningLabel,"wrap");
		
		advancedDropDown = new JComboBox<String>(TESTDROPDOWNDATA);
		advancedDropDown.setSelectedIndex(0);
		chooseHashLabel = new JLabel("Change crypto hash:");
		add(chooseHashLabel, "split");
		add(advancedDropDown, "wrap");
		
		configButton = new JButton("Open config.yaml");
		add(configButton, "dock south");
		
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
