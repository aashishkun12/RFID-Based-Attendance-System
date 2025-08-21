package employee;

import config.AppConfig;
import dashboard.Dashboard;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.*;
import com.toedter.calendar.JDateChooser;
import java.util.Calendar;
import java.util.Date;

/**
 * UpdateManager - Handles employee data modification and deletion
 * Provides interface for updating employee details and removing records
 */
public class UpdateManager {

    JFrame frame;
    JTextField namebox;
    JTextField rfidbox;
    JTextField phonebox;
    JTextField addressbox;
    JTextField emailbox;
    JRadioButton maleRadio;
    JRadioButton femaleRadio;
    JDateChooser dobBox;
    String empId;

    public UpdateManager(String empId, String empName) {
        this.empId = empId;
        
        frame = new JFrame();
        JLabel title = new JLabel("<html><h2>Update Employee</h2></html>");
        JLabel subtitle1 = new JLabel("<html><u>Employee Details:</u></html>");

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
        JButton submit = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");

        maleRadio = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");

        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);

        frame.setSize(800, 600);
        frame.setTitle("Employee Management System");
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

        submit.setBounds(350, 480, 100, 40);
        frame.add(submit);

        deleteBtn.setBounds(465, 480, 100, 40);
        frame.add(deleteBtn);

        frame.setLayout(null);
        
        loadEmployeeData(empId);

        submit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEmployeeData();
            }
        });
        
        deleteBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteEmployeeData();
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Dashboard(); 
                frame.dispose();
            }
        });

        frame.setVisible(true);
    }

    /**
     * Loads existing employee data from database into form fields
     * @param empId Employee ID to load data for
     */
    public void loadEmployeeData(String empId) {
        try {
            Connection connect = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USERNAME, AppConfig.DB_PASSWORD);
            String query = "SELECT * FROM employee WHERE employee_id = ?";
            PreparedStatement stmt = connect.prepareStatement(query);
            stmt.setString(1, empId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                namebox.setText(rs.getString("e_name"));
                rfidbox.setText(rs.getString("e_rfid"));
                phonebox.setText(rs.getString("e_phone"));
                addressbox.setText(rs.getString("e_address"));
                emailbox.setText(rs.getString("e_email"));

                java.sql.Date dob = rs.getDate("e_dob");
                if (dob != null) {
                    dobBox.setDate(dob);
                } else {
                    dobBox.setDate(new java.util.Date());
                }

                String gender = rs.getString("e_gender");
                if ("Male".equals(gender)) {
                    maleRadio.setSelected(true);
                } else if ("Female".equals(gender)) {
                    femaleRadio.setSelected(true);
                }
            } else {
                JOptionPane.showMessageDialog(null, "No employee found with ID: " + empId);
            }
            stmt.close();
            connect.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading employee data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates and updates employee data in the database
     */
    public void updateEmployeeData() {
        String ename = namebox.getText();
        String eaddress = addressbox.getText();
        String ephone = phonebox.getText();
        String rfid = rfidbox.getText();
        String email = emailbox.getText();
        Date edob = dobBox.getDate();
        String genderSelection = maleRadio.isSelected() ? "Male" : "Female";

        if (ename.isEmpty() || rfid.isEmpty() || eaddress.isEmpty() || ephone.isEmpty() || genderSelection.isEmpty() || !isValidEmail(email)) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields correctly.");
            return;
        }

        if (!ephone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(null, "Please enter a valid 10-digit phone number.");
            return;
        }

        try {
            Connection connect = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USERNAME, AppConfig.DB_PASSWORD);
            String query = "UPDATE employee SET e_name = ?, e_address = ?, e_phone = ?, e_email = ?, e_dob = ?, e_gender = ?, e_rfid = ? WHERE employee_id = ?";
            PreparedStatement stmt = connect.prepareStatement(query);

            stmt.setString(1, ename);
            stmt.setString(2, eaddress);
            stmt.setString(3, ephone);
            stmt.setString(4, email);
            stmt.setDate(5, new java.sql.Date(edob.getTime()));
            stmt.setString(6, genderSelection);
            stmt.setString(7, rfid);
            stmt.setString(8, empId);

            stmt.executeUpdate();
            stmt.close();
            connect.close();

            JOptionPane.showMessageDialog(null, "Employee updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating employee data.");
            e.printStackTrace();
        }
    }

    /**
     * Handles employee deletion with confirmation and related data cleanup
     */
    public void deleteEmployeeData(){
        int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this employee?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                Connection connect = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USERNAME, AppConfig.DB_PASSWORD);
                String deleteAttendance = "DELETE FROM attendance WHERE employee_id = ?";
                String deleteSalarySummary = "DELETE FROM salarysummary WHERE employee_id = ?";
                String deleteEmployee = "DELETE FROM employee WHERE employee_id = ?";

                connect.setAutoCommit(false);

                PreparedStatement deleteAttendanceStmt = connect.prepareStatement(deleteAttendance);
                deleteAttendanceStmt.setString(1, empId);
                deleteAttendanceStmt.executeUpdate();

                 PreparedStatement deleteSalarySummaryStmt = connect.prepareStatement(deleteSalarySummary);
                 deleteSalarySummaryStmt.setString(1, empId);
                 deleteSalarySummaryStmt.executeUpdate(); 

                PreparedStatement deleteEmployeeStmt = connect.prepareStatement(deleteEmployee);
                deleteEmployeeStmt.setString(1, empId);
                int rowsAffected =deleteEmployeeStmt.executeUpdate();

                connect.commit();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Employee deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "No employee found with the given ID.");
                }

                connect.close();
                frame.dispose();
                new Dashboard();
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, "Error deleting employee data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Validates email format using regex pattern matching
     * @param email Email address to validate
     * @return true if email format is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
