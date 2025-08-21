package util;

import config.AppConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.*;
import java.sql.*;
import java.text.DateFormatSymbols;
import javax.swing.JOptionPane;

/**
 * CSVExporter - Handles export of attendance data to CSV files
 * Creates CSV reports for individual employees with month/year filtering
 */
public class CSVExporter {

    public static void exportToCSV(String empId, String year, String month) {
        File file = null;
        try {
           // Instead of Paths.get("").toAbsolutePath(), directly use relative path from 'code'
            Path folderPath = Paths.get("exported_data"); // Relative to current working directory, 'RFID/code'

            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }



            int monthNumber = monthNameToNumber(month);
            if (monthNumber == -1) {
                JOptionPane.showMessageDialog(null, "Invalid month selected.");
                return;
            }

            // Now file will be saved inside 'RFID/code/exported_data'
            String fileName = empId + "_attendance_" + month + "_" + year + ".csv";
            file = new File(folderPath.toFile(), fileName);

            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("Date,Time In,Time Out,Worked Hour,Status");

                try (Connection conn = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USERNAME, AppConfig.DB_PASSWORD)) {
                    String query = "SELECT date, timeIn, timeOut, workedHour, status FROM attendance " +
                                   "WHERE employee_id = ? AND YEAR(date) = ? AND MONTH(date) = ? ORDER BY date ASC";

                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setString(1, empId);
                    ps.setString(2, year);
                    ps.setInt(3, monthNumber);

                    ResultSet rs = ps.executeQuery();
                    boolean hasData = false;

                    while (rs.next()) {
                        hasData = true;
                        String date = rs.getDate("date").toString();
                        String timeIn = rs.getString("timeIn");
                        String timeOut = rs.getString("timeOut");
                        double workedHour = rs.getDouble("workedHour");
                        String status = rs.getString("status");

                        pw.printf("%s,%s,%s,%.2f,%s%n",
                            date,
                            timeIn == null ? "N/A" : timeIn,
                            timeOut == null ? "N/A" : timeOut,
                            workedHour,
                            status
                        );
                    }

                    if (!hasData) {
                        JOptionPane.showMessageDialog(null, "No attendance data found for " + month + " " + year);
                        pw.close();
                        if (file.exists() && !file.delete()) {
                            System.err.println("Failed to delete empty file: " + file.getAbsolutePath());
                        }
                        return;
                    }
                }

                JOptionPane.showMessageDialog(null, "Attendance exported to: " + file.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to export CSV: " + e.getMessage());
            if (file != null && file.exists() && file.length() == 0) {
                if (!file.delete()) {
                    System.err.println("Failed to delete file on error: " + file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Converts month name to its numerical representation (January -> 1)
     * @param monthName Full month name
     * @return Month number (1-12) or -1 if invalid
     */
    private static int monthNameToNumber(String monthName) {
        String[] months = new DateFormatSymbols().getMonths();
        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(monthName)) {
                return i + 1;
            }
        }
        return -1;
    }
}
