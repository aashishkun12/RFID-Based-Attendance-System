package dashboard;

import attendance.AttendanceMailer;
import attendance.AttendanceViewer;
import employee.EmployeeRegistrationForm;
import employee.EmployeeUpdateDetails;
import employee.EmployeeViewer;
import util.ExitApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dashboard - Main navigation interface for the attendance system
 * Provides access to all system functionalities
 */
public class Dashboard {
    
    public Dashboard() {
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setTitle("Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));

        JPanel topSpacer = new JPanel();
        topSpacer.setPreferredSize(new Dimension(0, 30));
        panel.add(topSpacer, BorderLayout.NORTH);

        GridLayout grid = new GridLayout(6, 1, 0, 20);
        JPanel buttonPanel = new JPanel(grid);

        JButton regStudent = new JButton("Register a Employee");
        JButton viewEmployee = new JButton("View Employees");
        JButton viewAttendance = new JButton("View Attendance");
        JButton sendReport = new JButton("Send Report");
        JButton modButton = new JButton("Modify");
        JButton exiButton = new JButton("Exit");

        buttonPanel.add(regStudent);
        buttonPanel.add(viewEmployee);
        buttonPanel.add(viewAttendance);
        buttonPanel.add(sendReport);
        buttonPanel.add(modButton);
        buttonPanel.add(exiButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        JPanel eastSpacer = new JPanel();
        eastSpacer.setPreferredSize(new Dimension(50, 0));
        panel.add(eastSpacer, BorderLayout.EAST);

        JPanel westSpacer = new JPanel();
        westSpacer.setPreferredSize(new Dimension(50, 0));
        panel.add(westSpacer, BorderLayout.WEST);

        JPanel southSpacer = new JPanel();
        southSpacer.setPreferredSize(new Dimension(0, 30));
        panel.add(southSpacer, BorderLayout.SOUTH);

        frame.add(panel);

        regStudent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                new EmployeeRegistrationForm();
                frame.dispose();
            }  
        });

        viewEmployee.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                new EmployeeViewer();
                frame.dispose();
            } 
        });

        viewAttendance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                new AttendanceViewer();  
                frame.dispose(); 
            }  
        });

       sendReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            try {
                AttendanceMailer.sendMonthlyReports();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                "Error sending attendance reports:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    });

        modButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                new EmployeeUpdateDetails();
                frame.dispose();    
            }  
        });

        exiButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                new ExitApp();
            }   
        });

        frame.setVisible(true);
    }
}