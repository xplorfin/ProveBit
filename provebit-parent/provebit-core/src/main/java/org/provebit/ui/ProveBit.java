package org.provebit.ui;

import javax.swing.*;

public class ProveBit {
    JFrame frame = new JFrame("ProveBit");
    JPanel progressTable = new JPanel();
    
    public ProveBit() {
        MainFrame mainFrame = new MainFrame();
        
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ProveBit();
            }
        });
        
    }

}
