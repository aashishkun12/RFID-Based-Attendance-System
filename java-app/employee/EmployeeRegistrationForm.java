package employee;

import dashboard.Dashboard;
import config.AppConfig;

import javax.swing.*;
import java.awt.event.*;
import java.util.regex.*;
import com.toedter.calendar.JDateChooser;
import java.util.Calendar;
import java.util.Date;
import java.sql.*;

/**
 * EmployeeRegistrationForm - Handles new employee registration
 * Validates input and stores employee data in database
 */
public class EmployeeRegistrationForm {

    JTextField namebox;
    JTextField rfidbox;
    JTextField phonebox;
    JTextField addressbox;
    JTextField emailbox;
    
    JRadioButton maleRadio;
    JRadioButton femaleRadio;
    
    JDateChooser dobBox;

    public EmployeeRegistrationForm() {
        JFrame frame = new JFrame();

        JLabel title = new JLabel("<html><h2>Register a Student</h2></html>");
        JLabel subtitle1 = new JLabel("<html><u>Student Details:</u></html>");

        JLabel namelbl = new JLabel("Name:");
        JLabel rfidlbl = new JLabel("RFID Tag:");
        JLabel doblbl = new JLabel("Date of Birth:");
        JLabel gender = new JLabel("Gender:");
        JLabel addresslbl = new JLabel("Address:");
        JLabel phonelbl = new JLabel("Phone Number:");
        JLabel emailbl = new JLabel("Email:");

        namebox = new JTextField();
        rfidbox = new JTextField();
        phonebox = new JTextField();
        addressbox = new JTextField();
        emailbox = new JTextField();

        dobBox = new JDateChooser();
        JButton submit = new JButton("Submit");

        maleRadio = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");

        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);

        frame.setSize(800, 600);
        frame.setTitle("Attendance Management System");
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        title.setBounds(325, 10, 200, 40);
        frame.add(title);

        subtitle1.setBounds(60, 60, 100, 15);
        frame.add(subtitle1);

        namelbl.setBounds(40, 120, 60, 10);
        namebox.setBounds(100, 105, 200, 40);
        frame.add(namelbl);
        frame.add(namebox);

        rfidlbl.setBounds(440,120,60,15);
        rfidbox.setBounds(520,105,160,40);
        frame.add(rfidlbl);
        frame.add(rfidbox);

        doblbl.setBounds(40, 180, 80, 10);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);
        dobBox.setMaxSelectableDate(cal.getTime());
        
        dobBox.setBounds(130, 170, 120,30);
        frame.add(doblbl);
        frame.add(dobBox);

        gender.setBounds(440, 180, 60, 10);
        maleRadio.setBounds(505, 180, 80, 15);
        femaleRadio.setBounds(590, 180, 80, 15);
        frame.add(gender);
        frame.add(maleRadio);
        frame.add(femaleRadio);

        addresslbl.setBounds(40, 240, 60, 10);
        addressbox.setBounds(100, 225, 200, 40);
        frame.add(addresslbl);
        frame.add(addressbox);

        phonelbl.setBounds(440, 240, 100, 10);
        phonebox.setBounds(540, 225, 200, 40);
        frame.add(phonelbl);
        frame.add(phonebox);

        emailbl.setBounds(40,320,60,10);
        emailbox.setBounds(100,305,200,40);
        frame.add(emailbl);
        frame.add(emailbox);

        submit.setBounds(365, 480, 80, 40);
        frame.add(submit);

        frame.setLayout(null);

         frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Dashboard();
                frame.dispose();
            }
        });

        frame.setVisible(true);

        submit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                validateForm();
            }
        });
    }
 
    public void validateForm() {
        String ename = namebox.getText();
        String eaddress = addressbox.getText();
        String ephone = phonebox.getText();
        String rfid = rfidbox.getText();
        String email = emailbox.getText();

        if (ename.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Name cannot be empty.");
            return;
        }

        if (rfid.isEmpty()) {
            JOptionPane.showMessageDialog(null, "RFID Tag cannot be empty.");
            return;
        }

        Date edob = dobBox.getDate();
        if (edob == null){
            JOptionPane.showMessageDialog(null, "Select Date of Birth");
            return;
        }
        String genderSelection = "";
        if (maleRadio.isSelected()) {
            genderSelection = "Male";
        } else if (femaleRadio.isSelected()) {
            genderSelection = "Female";
        }

        if (eaddress.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Address cannot be empty.");
            return;
        }

        if (ephone.isEmpty() || !ephone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(null, "Phone number must be 10 digits.");
            return;
        }

        if (genderSelection.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select a gender.");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(null, "Please enter a valid email.");
            return;
        }
        
        storeDB(ename,rfid,edob,eaddress,ephone,genderSelection,email);
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void storeDB(String ename,String rfid,Date edob,String eaddress,String ephone, String genderSelection, String email){

        java.sql.Date birthdate = null;
        try {
            Connection connect = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USERNAME, AppConfig.DB_PASSWORD);
            String query = "INSERT INTO employee (e_name, e_address, e_phone,e_email, e_dob, e_gender,e_rfid) VALUES(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connect.prepareStatement(query);

            stmt.setString(1, ename);
            stmt.setString(2, eaddress);
            stmt.setString(3, ephone);
            stmt.setString(4, email);
            birthdate = new java.sql.Date(edob.getTime());
            stmt.setDate(5, birthdate);
            stmt.setString(6, genderSelection);
            stmt.setString(7, rfid);

            stmt.executeUpdate();
            stmt.close();
            connect.close();

            JOptionPane.showMessageDialog(null, "Data inserted successfully!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error inserting employee data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}