import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

public class GUI implements ActionListener{
	JFrame frame = new JFrame("Calculator");
	JPanel panel = new JPanel();
	JLabel screen = new JLabel("0");
	
	JButton btnOne = new JButton("1");
	JButton btnTwo = new JButton("2");
	JButton btnThree = new JButton("3");
	JButton btnFour = new JButton("4");
	JButton btnFive = new JButton("5");
	JButton btnSix = new JButton("6");
	JButton btnSeven = new JButton("7");
	JButton btnEight = new JButton("8");
	JButton btnNine = new JButton("9");
	JButton btnZero = new JButton("0");
	
	JButton btnAdd = new JButton("+");
	JButton btnSub = new JButton("-");
	JButton btnMul = new JButton("*");
	JButton btnDiv = new JButton("/");
	JButton btnEqn = new JButton("=");
	
	public GUI() {
		panel.setLayout(new MigLayout());
		// display screen for the calculator
		panel.add(screen, "span");
		// numeric and operation buttons
		panel.add(btnOne);
		btnOne.addActionListener(this);
		panel.add(btnTwo);
		btnTwo.addActionListener(this);
		panel.add(btnThree);
		btnThree.addActionListener(this);
		panel.add(btnAdd, "wrap");
		btnAdd.addActionListener(this);
		panel.add(btnFour);
		btnFour.addActionListener(this);
		panel.add(btnFive);
		btnFive.addActionListener(this);
		panel.add(btnSix);
		btnSix.addActionListener(this);
		panel.add(btnSub, "wrap");
		btnSub.addActionListener(this);
		panel.add(btnSeven);
		btnSeven.addActionListener(this);
		panel.add(btnEight);
		btnEight.addActionListener(this);
		panel.add(btnNine);
		btnNine.addActionListener(this);
		panel.add(btnEqn, "wrap");
		btnEqn.addActionListener(this);
		panel.add(btnZero);
		btnZero.addActionListener(this);
		
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e){
		switch (e.getActionCommand()){
			case "1":
				appendNumToOutputString(1);
				break;
			case "2": 
				appendNumToOutputString(2);
				break;
			case "3": 
				appendNumToOutputString(3);
				break;
			case "4": 
				appendNumToOutputString(4);
				break;
			case "5": 
				appendNumToOutputString(5);
				break;
			case "6": 
				appendNumToOutputString(6);
				break;
			case "7": 
				appendNumToOutputString(7);
				break;
			case "8": 
				appendNumToOutputString(8);
				break;
			case "9": 
				appendNumToOutputString(9);
				break;
			case "0": 
				appendNumToOutputString(0);
				break;
			case "+":
				appendOperationToOutputString("+");
				break;
			case "-":
				appendOperationToOutputString("-");
				break;
			case "=":
				computeString();
				break;
		}
	}

	public void appendNumToOutputString(int inputNum) {
		StringBuilder outputStringBuilder = new StringBuilder();
		// if screen only has "0" on it
		if(screen.getText().equals("0")) {
			outputStringBuilder.append(inputNum);
			screen.setText(outputStringBuilder.toString());
		}
		// normal case
		else {
			outputStringBuilder.append(screen.getText());
			outputStringBuilder.append(inputNum);
			screen.setText(outputStringBuilder.toString());
		}
	}
	
	public void appendOperationToOutputString(String inputString) {
		StringBuilder outputStringBuilder = new StringBuilder();
			outputStringBuilder.append(screen.getText());
			outputStringBuilder.append(inputString);
			screen.setText(outputStringBuilder.toString());
	}
	
	public void computeString() {
		String currentString = screen.getText();
		long result = -1;
		// TODO write function to computer current displaying string and store it in result
		screen.setText(Long.toString(result));
	}
	
	public void backspace() {
		// TODO create button and write function for backspace
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new GUI();
			}
		});
	}

}
