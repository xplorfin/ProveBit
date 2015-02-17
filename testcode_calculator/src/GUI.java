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
		panel.add(screen, "wrap");
		// numeric buttons
		panel.add(btnOne);
		btnOne.addActionListener(this);
		panel.add(btnTwo);
		panel.add(btnThree);
		panel.add(btnAdd, "wrap");
		panel.add(btnFour);
		panel.add(btnFive);
		panel.add(btnSix);
		panel.add(btnSub, "wrap");
		panel.add(btnSeven);
		panel.add(btnEight);
		panel.add(btnNine);
		panel.add(btnEqn, "wrap");
		panel.add(btnZero);
		
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e){
		switch (e.getActionCommand()){
		case "1": appendNumToOutputString(1);
		break;
		}
	}

	public void appendNumToOutputString(int inputNum) {
		StringBuilder outputStringBuilder = new StringBuilder();
		outputStringBuilder.append(screen.getText());
		outputStringBuilder.append(inputNum);
		screen.setText(outputStringBuilder.toString());
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
