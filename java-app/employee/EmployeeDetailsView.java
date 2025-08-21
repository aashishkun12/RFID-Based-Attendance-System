package employee;

import util.AdaptiveRegression;
import util.CSVExporter;

import javax.swing.*;

import config.AppConfig;

import java.awt.*;
import java.sql.*;

/**
 * EmployeeDetailsView - Displays detailed employee information including attendance statistics
 * Shows predicted salary, attendance records, and provides export functionality
 */
public class EmployeeDetailsView {
    private JFrame detailFrame;
    private JLabel presentDaysLabel, workedHoursLabel, salaryLabel;
    private JButton exportBtn;

    public EmployeeDetailsView(String empId, String empName) {
        detailFrame = new JFrame("Employee Detail");
        detailFrame.setSize(550, 350);
        detailFrame.setLocationRelativeTo(null);
        detailFrame.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10,10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Employee ID display
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("EID:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(empId), gbc);

        // Employee name display
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(empName), gbc);

        // Predicted salary display
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Predicted Salary:"), gbc);
        gbc.gridx = 1;
        salaryLabel = new JLabel();
        double predictedSalary = AdaptiveRegression.predictSalary(empId);
        if (predictedSalary >= 0)
            salaryLabel.setText("Rs. " + String.format("%.2f", predictedSalary));
        else
            salaryLabel.setText("Not enough data");
        panel.add(salaryLabel, gbc);

        // Month selection dropdown
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Month:"), gbc);
        gbc.gridx = 1;
        String[] months = {"January", "February", "March", "April", "May", "June",
                           "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthBox = new JComboBox<>(months);
        panel.add(monthBox, gbc);

        // Year selection dropdown
        gbc.gridx = 2;
        panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 3;
        String[] years = {"2025"};
        JComboBox<String> yearBox = new JComboBox<>(years);
        panel.add(yearBox, gbc);

        // Attendance statistics display
        gbc.gridx = 0; gbc.gridy = 4;
        presentDaysLabel = new JLabel("Present Days: 0");
        panel.add(presentDaysLabel, gbc);
        gbc.gridx = 1;
        workedHoursLabel = new JLabel("Worked Hours: 0");
        panel.add(workedHoursLabel, gbc);

        // Export button
        gbc.gridx = 0; gbc.gridy = 5;
        exportBtn = new JButton("Export");
        panel.add(exportBtn, gbc);

        detailFrame.add(panel);
        detailFrame.setVisible(true);

        // Event listeners for dynamic data loading
        monthBox.addActionListener(e -> calculateAttendance(empId, monthBox, yearBox));
        yearBox.addActionListener(e -> calculateAttendance(empId, monthBox, yearBox));

        exportBtn.addActionListener(e -> {
            String selectedMonth = monthBox.getSelectedItem().toString();
            String selectedYear = yearBox.getSelectedItem().toString();
            CSVExporter.exportToCSV(empId, selectedYear, selectedMonth);
        });
    }

    /**
     * Calculates and displays attendance statistics for selected month/year
     * @param empId Employee ID to query attendance for
     * @param monthBox Month selection combo box
     * @param yearBox Year selection combo box
     */
    private void calculateAttendance(String empId, JComboBox<String> monthBox, JComboBox<String> yearBox) {
        String selectedYear = yearBox.getSelectedItem().toString();

        try (Connection conn = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USERNAME, AppConfig.DB_PASSWORD)) {
            String query = "SELECT SUM(workedHour) AS totalWorkedHours, COUNT(CASE WHEN status = 'Present' THEN 1 END) AS presentDays " +
                           "FROM attendance WHERE employee_id = ? AND YEAR(date) = ? AND MONTH(date) = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, empId);
            pst.setString(2, selectedYear);
            pst.setString(3, String.valueOf(monthBox.getSelectedIndex() + 1));

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int totalWorkedHours = rs.getInt("totalWorkedHours");
                int presentDays = rs.getInt("presentDays");
                workedHoursLabel.setText("Worked Hours: " + totalWorkedHours);
                presentDaysLabel.setText("Present Days: " + presentDays);    
            } else {
                workedHoursLabel.setText("Worked Hours: 0");
                presentDaysLabel.setText("Present Days: 0");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(detailFrame, "Error fetching data: " + ex.getMessage());
        }
    }
}