package admin;

import dashboard.Dashboard;
import config.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * AdminLogin - Handles administrator authentication
 * Validates credentials and provides access to the main dashboard
 */
public class AdminLogin {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JTextField uname = new JTextField();
        JLabel unamelbl = new JLabel();
        JPasswordField pass = new JPasswordField();
        JLabel passlbl = new JLabel();
        JButton sub = new JButton();
        JLabel err = new JLabel();

        frame.setSize(600,400);
        frame.setTitle("Admin Login Page");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setVisible(true);

        unamelbl.setText("Enter Username:");
        unamelbl.setBounds(100,30,100,80);
        frame.add(unamelbl);

        uname.setBounds(250,50,200,40);
        frame.add(uname);

        passlbl.setText("Enter Password:");
        passlbl.setBounds(100,130,100,80);
        frame.add(passlbl);

        pass.setBounds(250,150,200,40);
        frame.add(pass);

        err.setText("Incorrect Username or Password");
        err.setBounds(200,180,200,80);
        err.setForeground(Color.RED);
        err.setVisible(false);
        frame.add(err);

        sub.setText("Submit");
        sub.setBounds(250,250,80,50);
        frame.add(sub);

        sub.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String username = uname.getText();
                char[] passChar = pass.getPassword();
                String password = new String(passChar);

                if(password.equals(AppConfig.adminPassword) && username.equals(AppConfig.adminUsername)){
                    frame.setVisible(false);
                    frame.dispose();
                    new Dashboard();
                }
                else{
                    err.setVisible(true);
                }
            }
        });
    }
}
