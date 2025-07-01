import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AttendancePage extends JFrame implements ActionListener {
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sms?useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // UI Components
    private JButton backButton, saveButton, refreshButton;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> dateCombo, departmentFilter;
    private JTextField searchField;
    private JLabel summaryLabel;
    private Connection connection;
    private Map<String, Integer> studentIdToDbId = new HashMap<>();
    private TableRowSorter<TableModel> sorter;
    
    public AttendancePage() {
        setTitle("Student Management System - Attendance");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        // Initialize database connection
        connectToDatabase();
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel titleLabel = new JLabel("Attendance Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Control Panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Back button
        backButton = createStyledButton("Back to Home", new Color(70, 130, 180));
        backButton.addActionListener(this);
        
        // Date selection
        JLabel dateLabel = new JLabel("Select Date:");
        dateCombo = new JComboBox<>();
        populateDateCombo();
        dateCombo.addActionListener(e -> loadAttendanceData());
        
        // Department filter
        JLabel deptLabel = new JLabel("Department:");
        String[] departments = {"All", "Computer", "Civil", "Mechanical", "Architecture", "Electrical"};
        departmentFilter = new JComboBox<>(departments);
        departmentFilter.addActionListener(e -> filterTable());
        
        // Search field
        JLabel searchLabel = new JLabel("Search:");
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        
        // Buttons
        saveButton = createStyledButton("Save Attendance", new Color(34, 139, 34));
        saveButton.addActionListener(e -> saveAttendance());
        
        refreshButton = createStyledButton("Refresh", new Color(70, 130, 180));
        refreshButton.addActionListener(e -> loadAttendanceData());
        
        // Add components to control panel
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        controlPanel.add(backButton, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; controlPanel.add(Box.createHorizontalStrut(20), gbc);
        
        gbc.gridx = 2; gbc.gridy = 0; controlPanel.add(dateLabel, gbc);
        gbc.gridx = 3; gbc.gridy = 0; controlPanel.add(dateCombo, gbc);
        
        gbc.gridx = 4; gbc.gridy = 0; controlPanel.add(Box.createHorizontalStrut(20), gbc);
        
        gbc.gridx = 5; gbc.gridy = 0; controlPanel.add(deptLabel, gbc);
        gbc.gridx = 6; gbc.gridy = 0; controlPanel.add(departmentFilter, gbc);
        
        gbc.gridx = 7; gbc.gridy = 0; controlPanel.add(Box.createHorizontalStrut(20), gbc);
        
        gbc.gridx = 8; gbc.gridy = 0; controlPanel.add(searchLabel, gbc);
        gbc.gridx = 9; gbc.gridy = 0; controlPanel.add(searchField, gbc);
        
        gbc.gridx = 10; gbc.gridy = 0; controlPanel.add(Box.createHorizontalStrut(20), gbc);
        
        gbc.gridx = 11; gbc.gridy = 0; controlPanel.add(saveButton, gbc);
        gbc.gridx = 12; gbc.gridy = 0; controlPanel.add(refreshButton, gbc);
        
        // Summary label
        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 13; gbc.anchor = GridBagConstraints.CENTER;
        controlPanel.add(summaryLabel, gbc);
        
        // Table setup
        String[] columns = {"ID", "Student ID", "Student Name", "Department", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only status column is editable
            }
        };
        
        attendanceTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column == 4) { // Status column
                    String status = getValueAt(row, column).toString();
                    if ("Present".equals(status)) {
                        c.setBackground(new Color(200, 255, 200));
                    } else if ("Absent".equals(status)) {
                        c.setBackground(new Color(255, 200, 200));
                    } else if ("Late".equals(status)) {
                        c.setBackground(new Color(255, 255, 150));
                    }
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        
        attendanceTable.setFont(new Font("Arial", Font.PLAIN, 14));
        attendanceTable.setRowHeight(25);
        attendanceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add status combo box renderer and editor
        TableColumn statusColumn = attendanceTable.getColumnModel().getColumn(4);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present", "Absent", "Late"});
        statusColumn.setCellEditor(new DefaultCellEditor(statusCombo));
        
        // Initialize row sorter
        sorter = new TableRowSorter<>(tableModel);
        attendanceTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Add components to frame
        add(titlePanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
        
        // Set some styling
        getContentPane().setBackground(new Color(240, 240, 240));
        controlPanel.setBackground(new Color(240, 240, 240));
        titlePanel.setBackground(new Color(240, 240, 240));
        
        // Load initial data
        loadAttendanceData();
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 35));
        return button;
    }
    
    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error connecting to database: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void populateDateCombo() {
        // Add today and previous 6 days to the combo box
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        
        dateCombo.removeAllItems();
        for (int i = 0; i < 7; i++) {
            dateCombo.addItem(sdf.format(cal.getTime()));
            cal.add(Calendar.DATE, -1);
        }
        
        // Set default to today
        dateCombo.setSelectedIndex(0);
    }
    
    private void loadAttendanceData() {
        String selectedDate = (String) dateCombo.getSelectedItem();
        if (selectedDate == null) return;
        
        try {
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Get all students for the selected date
            String query = "SELECT s.id as student_id, s.name, s.department, " +
                         "COALESCE(a.status, 'Absent') as status, a.id as attendance_id " +
                         "FROM students s " +
                         "LEFT JOIN attendance a ON s.id = a.student_id AND a.date = ? " +
                         "ORDER BY s.department, s.name";
            
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, selectedDate);
            ResultSet rs = pst.executeQuery();
            
            // Add rows to table
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String name = rs.getString("name");
                String department = rs.getString("department");
                String status = rs.getString("status");
                int attendanceId = rs.getInt("attendance_id");
                
                // Store attendance ID in the first column (hidden)
                tableModel.addRow(new Object[]{attendanceId > 0 ? attendanceId : null, 
                                             studentId, name, department, status});
            }
            
            // Update summary
            updateSummary();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading attendance data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void saveAttendance() {
        String selectedDate = (String) dateCombo.getSelectedItem();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a date first.");
            return;
        }
        
        try {
            connection.setAutoCommit(false);
            int updated = 0;
            int inserted = 0;
            
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                Integer attendanceId = (Integer) tableModel.getValueAt(row, 0);
                String studentId = (String) tableModel.getValueAt(row, 1);
                String status = (String) tableModel.getValueAt(row, 4);
                
                // Get student ID from the database using student ID
                String studentDbId = getStudentId(studentId);
                if (studentDbId == null) continue;
                
                if (attendanceId != null) {
                    // Update existing attendance
                    String updateQuery = "UPDATE attendance SET status = ? WHERE id = ?";
                    try (PreparedStatement pst = connection.prepareStatement(updateQuery)) {
                        pst.setString(1, status);
                        pst.setInt(2, attendanceId);
                        updated += pst.executeUpdate();
                    }
                } else {
                    // Insert new attendance
                    String insertQuery = "INSERT INTO attendance (student_id, date, status) VALUES (?, ?, ?)";
                    try (PreparedStatement pst = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        pst.setString(1, studentDbId);
                        pst.setString(2, selectedDate);
                        pst.setString(3, status);
                        pst.executeUpdate();
                        
                        // Get the generated attendance ID
                        try (ResultSet rs = pst.getGeneratedKeys()) {
                            if (rs.next()) {
                                tableModel.setValueAt(rs.getInt(1), row, 0);
                                inserted++;
                            }
                        }
                    }
                }
            }
            
            connection.commit();
            JOptionPane.showMessageDialog(this, 
                String.format("Attendance saved successfully!\nUpdated: %d\nNew: %d", updated, inserted),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
                
            // Refresh the view to update any calculated fields
            loadAttendanceData();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, 
                "Error saving attendance: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String getStudentId(String studentId) {
        // Since student_id is the primary key in the students table,
        // we can directly return the input if it exists in the database
        try {
            String query = "SELECT id FROM students WHERE id = ?";
            try (PreparedStatement pst = connection.prepareStatement(query)) {
                pst.setString(1, studentId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void updateSummary() {
        int total = tableModel.getRowCount();
        int present = 0;
        int absent = 0;
        int late = 0;
        
        for (int i = 0; i < total; i++) {
            String status = tableModel.getValueAt(i, 4).toString();
            switch (status) {
                case "Present": present++; break;
                case "Absent": absent++; break;
                case "Late": late++; break;
            }
        }
        
        summaryLabel.setText(String.format("Total: %d | Present: %d | Absent: %d | Late: %d", 
            total, present, absent, late));
    }
    
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase();
        String selectedDept = (String) departmentFilter.getSelectedItem();
        
        RowFilter<TableModel, Integer> filter = new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                TableModel model = entry.getModel();
                int row = entry.getIdentifier();
                
                // Check department filter
                String dept = model.getValueAt(row, 3).toString(); // Department is at index 3
                if (!"All".equals(selectedDept) && !dept.equals(selectedDept)) {
                    return false;
                }
                
                // Check search text
                if (searchText.isEmpty()) {
                    return true;
                }
                
                // Search in ID, Name, and Department
                String id = model.getValueAt(row, 1).toString().toLowerCase();
                String name = model.getValueAt(row, 2).toString().toLowerCase();
                return id.contains(searchText) || 
                       name.contains(searchText) || 
                       dept.toLowerCase().contains(searchText);
            }
        };
        
        sorter.setRowFilter(filter);
    }
            
    // This method is called when the search field is updated
    private void refreshAttendanceData() {
        loadAttendanceData();
    }
    
    // Handles database rollback operations
    private void handleRollback() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error rolling back transaction: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cleanup() {
        try {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error cleaning up database connection: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            // Show home page when closing
            HomePage homePage = new HomePage();
            homePage.setVisible(true);
            this.dispose();
        }
    }
    
    // Handle button click events
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            // Return to home page
            HomePage homePage = new HomePage();
            homePage.setVisible(true);
            this.dispose();
        } else if (e.getSource() == saveButton) {
            // Save attendance data
            saveAttendance();
        } else if (e.getSource() == refreshButton) {
            // Refresh attendance data
            refreshAttendanceData();
        }
    }
    
    // Main method to launch the application
    public static void main(String[] args) {
        // Use the event dispatch thread for Swing components
        SwingUtilities.invokeLater(() -> {
            try {
                // Create and display the attendance page
                AttendancePage frame = new AttendancePage();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            } catch (Exception e) {
                // Show error message if initialization fails
                JOptionPane.showMessageDialog(null,
                    "Error initializing Attendance Page: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
