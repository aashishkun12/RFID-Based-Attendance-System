package attendance;

import config.AppConfig;
import dashboard.Dashboard;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;

/**
 * AttendanceViewer - Displays attendance records with filtering capabilities
 * Shows present and absent employees separately with date-based filtering
 */
public class AttendanceViewer {

    JTable presentTable, absentTable;
    DefaultTableModel presentModel, absentModel;
    JLabel totalPresentLabel, totalAbsentLabel;
    JButton filterButton;
    JDateChooser dateChooser;
    JFrame frame;

    public AttendanceViewer() {
        frame = new JFrame("Attendance System");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new java.util.Date());
        topPanel.add(new JLabel("Select Date:"));
        topPanel.add(dateChooser);

        filterButton = new JButton("Filter");
        topPanel.add(filterButton);

        frame.add(topPanel, BorderLayout.NORTH);

        presentModel = new DefaultTableModel();
        absentModel = new DefaultTableModel();

        presentTable = new JTable(presentModel);
        presentModel.addColumn("Employee ID");
        presentModel.addColumn("Employee Name");
        presentModel.addColumn("Date");
        presentModel.addColumn("Time In");
        presentModel.addColumn("Time Out");
        presentModel.addColumn("Worked Hours");

        absentTable = new JTable(absentModel);
        absentModel.addColumn("Employee ID");
        absentModel.addColumn("Employee Name");
        absentModel.addColumn("Date");

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new GridLayout(2, 1));
        tablePanel.add(new JScrollPane(presentTable));
        tablePanel.add(new JScrollPane(absentTable));
        frame.add(tablePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());

        totalPresentLabel = new JLabel("Total Present: 0");
        totalAbsentLabel = new JLabel("Total Absent: 0");
        bottomPanel.add(totalPresentLabel);
        bottomPanel.add(totalAbsentLabel);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        filterButton.addActionListener(e -> loadAttendanceData());

        loadAttendanceData();

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
     * Loads attendance data from database based on selected date
     * Populates present and absent tables with employee attendance records
     */
    private void loadAttendanceData() {
        String selectedDate = null;
        if (dateChooser.getDate() != null) {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            selectedDate = dateFormat.format(dateChooser.getDate());
        }

        try (Connection conn = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USERNAME, AppConfig.DB_PASSWORD)) {
            presentModel.setRowCount(0);
            absentModel.setRowCount(0);

            String presentQuery = "SELECT e.employee_id, e.e_name, a.date, a.timeIn, a.timeOut, a.workedHour "
                    + "FROM attendance a JOIN employee e ON a.employee_id = e.employee_id "
                    + "WHERE a.status = 'Present' ";
            String absentQuery = "SELECT e.employee_id, e.e_name, a.date "
                    + "FROM attendance a JOIN employee e ON a.employee_id = e.employee_id "
                    + "WHERE a.status = 'Absent' ";

            if (selectedDate != null) {
                presentQuery += "AND a.date = '" + selectedDate + "' ";
                absentQuery += "AND a.date = '" + selectedDate + "' ";
            }

            try (PreparedStatement pst = conn.prepareStatement(presentQuery)) {
                ResultSet rs = pst.executeQuery();
                int totalPresent = 0;

                while (rs.next()) {
                    Object[] row = new Object[6];
                    row[0] = rs.getInt("employee_id");
                    row[1] = rs.getString("e_name");
                    row[2] = rs.getDate("date");
                    row[3] = rs.getTime("timeIn");
                    row[4] = rs.getTime("timeOut");
                    row[5] = rs.getDouble("workedHour");

                    presentModel.addRow(row);
                    totalPresent++;
                }

                totalPresentLabel.setText("Total Present: " + totalPresent);
            }

            try (PreparedStatement pst = conn.prepareStatement(absentQuery)) {
                ResultSet rs = pst.executeQuery();
                int totalAbsent = 0;

                while (rs.next()) {
                    Object[] row = new Object[3];
                    row[0] = rs.getInt("employee_id");
                    row[1] = rs.getString("e_name");
                    row[2] = rs.getDate("date");

                    absentModel.addRow(row);
                    totalAbsent++;
                }

                totalAbsentLabel.setText("Total Absent: " + totalAbsent);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage());
        }
    }
}