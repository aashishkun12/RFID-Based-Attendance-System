package employee;

import dashboard.Dashboard;

import javax.swing.*;
import javax.swing.table.*;

import config.AppConfig;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * EmployeeViewer - Displays employee data in a table format with view functionality
 * Provides interactive table with action buttons to view detailed employee information
 */
public class EmployeeViewer {
    JTable table;
    DefaultTableModel model;
    JLabel heading;
    JFrame frame;

    public EmployeeViewer() {
        frame = new JFrame("Attendance System");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        heading = new JLabel("<html><h1>Employee Details:</h1></html>");
        heading.setHorizontalAlignment(JLabel.CENTER);
        frame.setLayout(new BorderLayout());
        frame.add(heading, BorderLayout.NORTH);

        model = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return column == getColumnCount() - 1;
            }
        };

        table = new JTable(model);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        loadDataFromDatabase();

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
     * Loads employee data from database and populates the table
     * Adds action column with view buttons for each employee
     */
    private void loadDataFromDatabase() {

        try (Connection conn = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USERNAME, AppConfig.DB_PASSWORD)) {
            String query = "SELECT * FROM employee";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            int columns = rsmd.getColumnCount();

            for (int i = 1; i <= columns; i++) {
                model.addColumn(rsmd.getColumnName(i));
            }
            model.addColumn("Action");

            while (rs.next()) {
                Object[] rowData = new Object[columns + 1];
                for (int i = 0; i < columns; i++) {
                    rowData[i] = rs.getObject(i + 1);
                }
                rowData[columns] = "View";
                model.addRow(rowData);
            }

            table.getColumn("Action").setCellRenderer(new ButtonRenderer());
            table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage());
        }
    }

    /**
     * Custom renderer for action buttons in table cells
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "View" : value.toString());
            return this;
        }
    }

    /**
     * Custom editor for action buttons with click handling
     */
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, 
                boolean isSelected, int row, int column) {
            label = (value == null) ? "View" : value.toString();
            button.setText(label);
            clicked = true;
            selectedRow = row;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                String empId = table.getValueAt(selectedRow, 0).toString();
                String empName = table.getValueAt(selectedRow, 1).toString();
                new EmployeeDetailsView(empId, empName);
            }
            clicked = false;
            return label;
        }

        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
}