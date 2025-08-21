package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ExitApp - Provides application exit confirmation dialog
 * Handles graceful application termination with user confirmation
 */
public class ExitApp {
    
    /**
     * Creates exit confirmation dialog with yes/no options
     * Closes all application windows when confirmed
     */
    public ExitApp() {
        JFrame frame = new JFrame();

        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(null);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JLabel msg = new JLabel("Do you want to exit?");
        msg.setBounds(80, 50, 180, 40);
        frame.add(msg);

        JButton yes = new JButton("Yes");
        yes.setBounds(40, 150, 80, 40);
        frame.add(yes);

        JButton no = new JButton("No");
        no.setBounds(180, 150, 80, 40);
        frame.add(no);

        yes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Window[] allWindows = Window.getWindows();
                for (Window window : allWindows) {
                    if (window instanceof JFrame) {
                        window.dispose();
                    }
                }
            }
        });

        no.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        
        frame.setVisible(true);
    }
}