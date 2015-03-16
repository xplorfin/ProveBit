package org.provebit.ui.advanced;

import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

public class AdvancedView extends JPanel implements Observer {
	AdvancedModel model;
	private static final String[] TESTDROPDOWNDATA = {"SHA256", "SHA3", ""};
	private static final String warning = "Warning: Do not edit these settings if you don't know what to do.";
	private JComboBox advancedDropDown;
	private JRadioButton advancedFirst;
	private JRadioButton advancedSecond;
	private JButton configButton;
	private JLabel warningLabel;
	private JLabel chooseHashLabel;
	private ButtonGroup radioGroup;
	
	public AdvancedView(AdvancedModel model) {
		this.model = model;
		setLayout(new MigLayout("","[]","5[]5[][][]"));
		
		warningLabel = new JLabel(warning);
		add(warningLabel, "dock north");
		/*
		advancedFirst = new JRadioButton("Option 1");
		advancedSecond = new JRadioButton("Option 2");
		
		radioGroup = new ButtonGroup();
		radioGroup.add(advancedFirst);
		radioGroup.add(advancedSecond);
		
		add(advancedFirst,"wrap");
		add(advancedSecond, "wrap");
		*/
		advancedDropDown = new JComboBox(TESTDROPDOWNDATA);
		advancedDropDown.setSelectedIndex(0);
		chooseHashLabel = new JLabel("Change crypto hash:");
		add(chooseHashLabel);
		add(advancedDropDown, "wrap");
		
		configButton = new JButton("Open config.yaml");
		add(configButton);
		
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
