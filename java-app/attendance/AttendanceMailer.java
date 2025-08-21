package attendance;

import config.AppConfig;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * AttendanceMailer - Handles sending attendance reports via email
 * Processes monthly attendance data and sends CSV reports to employees
 */
public class AttendanceMailer {

    /**
     * Main method to send attendance reports for previous month
     * Shows progress feedback and remains in application after completion
     */
    public static void sendMonthlyReports() {
        // Use SwingWorker for background processing to avoid UI freeze
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
    protected Void doInBackground() throws Exception {
        try (Connection conn = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USERNAME, AppConfig.DB_PASSWORD)) {
            
            YearMonth prevMonth = YearMonth.now().minusMonths(1);
            LocalDate fromDate = prevMonth.atDay(1);
            LocalDate toDate = prevMonth.atEndOfMonth();

            String empQuery = "SELECT employee_id, e_name, e_email FROM employee";
            PreparedStatement empStmt = conn.prepareStatement(empQuery);
            ResultSet empRs = empStmt.executeQuery();

            while (empRs.next()) {
                int empId = empRs.getInt("employee_id");
                String empName = empRs.getString("e_name");
                String empEmail = empRs.getString("e_email");

                String attQuery = "SELECT date, timeIn, timeOut, workedHour, status FROM attendance " +
                        "WHERE employee_id = ? AND date BETWEEN ? AND ?";
                PreparedStatement attStmt = conn.prepareStatement(attQuery);
                attStmt.setInt(1, empId);
                attStmt.setDate(2, Date.valueOf(fromDate));
                attStmt.setDate(3, Date.valueOf(toDate));

                ResultSet attRs = attStmt.executeQuery();

                if (!attRs.next()) {
                    publish("No data for: " + empName + " (" + empEmail + ")");
                    attRs.close();
                    attStmt.close();
                    continue;
                }

                String fileName = "attendance_" + empId + ".csv";
                File csvFile = new File(fileName);
                try (FileWriter fw = new FileWriter(csvFile)) {
                    fw.write("Date,Time In,Time Out,Worked Hours,Status\n");
                    do {
                        fw.write(String.format("%s,%s,%s,%.2f,%s\n",
                                attRs.getDate("date"),
                                attRs.getTime("timeIn"),
                                attRs.getTime("timeOut"),
                                attRs.getDouble("workedHour"),
                                attRs.getString("status")));
                    } while (attRs.next());
                }

                try {
                    sendEmailWithAttachment(empEmail, empName, fileName);
                    publish("Sent to: " + empName + " (" + empEmail + ")");
                } catch (Exception e) {
                    publish("Failed to send to: " + empName + " - " + e.getMessage());
                }

                attRs.close();
                attStmt.close();
                csvFile.delete();
            }

            empRs.close();
            empStmt.close();
            
        } catch (Exception e) {
            publish("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        for (String message : chunks) {
            if (message.startsWith("Sent to:")) {
                JOptionPane.showMessageDialog(null, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else if (message.startsWith("No data for:")) {
                JOptionPane.showMessageDialog(null, message, "No Data", JOptionPane.INFORMATION_MESSAGE);
            } else if (message.startsWith("Failed") || message.startsWith("Error")) {
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void done() {
        JOptionPane.showMessageDialog(null, 
            "All attendance reports processed successfully!", 
            "Process Complete", 
            JOptionPane.INFORMATION_MESSAGE);
    }
};

worker.execute();
    }

    /**
     * Sends email with attendance report attachment
     * @param to Recipient email address
     * @param name Employee name
     * @param filePath Path to CSV attachment file
     */
    private static void sendEmailWithAttachment(String to, String name, String filePath) throws Exception {
        final String senderEmail = AppConfig.senderEmail;
        final String appPassword = AppConfig.appPassword;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", AppConfig.SMTP_HOST);
        props.put("mail.smtp.port", AppConfig.SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        
        String monthName = YearMonth.now().minusMonths(1).getMonth().toString();
        monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();
        message.setSubject("Your Attendance Report for " + monthName);

        String bodyText = String.format(
            "Hello %s,\n\nHere is your attendance details for the month of %s.\n\nRegards,\nHR Team",
            name, monthName);
        
        BodyPart bodyPart = new MimeBodyPart();
        bodyPart.setText(bodyText);

        MimeBodyPart attachmentPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filePath);
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName(new File(filePath).getName());

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(bodyPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);
        Transport.send(message);
    }
}