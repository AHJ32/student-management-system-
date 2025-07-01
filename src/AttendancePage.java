import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(400, 30));
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(Color.BLACK);
        searchField.setCaretColor(Color.BLACK);
        searchField.setOpaque(true);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // Padding
        ));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // Padding inside the field
        ));

        searchField.setToolTipText("Search by ID, Name, or Department");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void insertUpdate(DocumentEvent e) { filterTable(); }
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
        gbc.gridx = 9; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        controlPanel.add(searchField, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        
        gbc.gridx = 10; gbc.gridy = 0; controlPanel.add(Box.createHorizontalStrut(20), gbc);
        
        gbc.gridx = 11; gbc.gridy = 0; controlPanel.add(saveButton, gbc);
        gbc.gridx = 12; gbc.gridy = 0; controlPanel.add(refreshButton, gbc);
        
        // Summary label
        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 13; gbc.anchor = GridBagConstraints.CENTER;
        controlPanel.add(summaryLabel, gbc);
        
        // Table setup
        String[] columns = {"Student ID", "Student Name", "Department", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only status column is editable
            }
        };
        
        attendanceTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                boolean isSelected = isRowSelected(row) && isColumnSelected(column);
                
                if (isSelected) {
                    c.setBackground(new Color(51, 122, 183)); // Darker blue for selection
                    c.setForeground(Color.WHITE);
                } else if (column == 3) { // Status column
                    String status = getValueAt(row, column).toString();
                    if ("Present".equals(status)) {
                        c.setBackground(new Color(200, 255, 200));
                        c.setForeground(Color.BLACK);
                    } else if ("Absent".equals(status)) {
                        c.setBackground(new Color(255, 200, 200));
                        c.setForeground(Color.BLACK);
                    } else if ("Late".equals(status)) {
                        c.setBackground(new Color(255, 255, 150));
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
            
            @Override
            public void changeSelection(int row, int column, boolean toggle, boolean extend) {
                super.changeSelection(row, column, toggle, extend);
                if (row != -1) {
                    scrollRectToVisible(getCellRect(row, column, true));
                }
            }
        };
        
        attendanceTable.setFont(new Font("Arial", Font.PLAIN, 14));
        attendanceTable.setRowHeight(25);
        attendanceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add status combo box renderer and editor
        TableColumn statusColumn = attendanceTable.getColumnModel().getColumn(3);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present", "Absent", "Late"});
        statusColumn.setCellEditor(new DefaultCellEditor(statusCombo));
        
        // Initialize row sorter with custom row filter for search
        sorter = new TableRowSorter<>(tableModel);
        attendanceTable.setRowSorter(sorter);
        
        // Set up search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
        
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
                         "COALESCE(a.status, 'Absent') as status " +
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
                
                tableModel.addRow(new Object[]{studentId, name, department, status});
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
                String studentId = (String) tableModel.getValueAt(row, 0);
                String status = (String) tableModel.getValueAt(row, 3);
                
                // Get student ID from the database using student ID
                String studentDbId = getStudentId(studentId);
                if (studentDbId == null) continue;
                
                // Check if attendance record already exists
                String query = "SELECT id FROM attendance WHERE student_id = ? AND date = ?";
                try (PreparedStatement pst = connection.prepareStatement(query)) {
                    pst.setString(1, studentDbId);
                    pst.setString(2, selectedDate);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            // Update existing attendance
                            String updateQuery = "UPDATE attendance SET status = ? WHERE id = ?";
                            try (PreparedStatement updatePst = connection.prepareStatement(updateQuery)) {
                                updatePst.setString(1, status);
                                updatePst.setInt(2, rs.getInt("id"));
                                updated += updatePst.executeUpdate();
                            }
                        } else {
                            // Insert new attendance
                            String insertQuery = "INSERT INTO attendance (student_id, date, status) VALUES (?, ?, ?)";
                            try (PreparedStatement insertPst = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                                insertPst.setString(1, studentDbId);
                                insertPst.setString(2, selectedDate);
                                insertPst.setString(3, status);
                                insertPst.executeUpdate();
                                
                                // Get the generated attendance ID
                                try (ResultSet insertRs = insertPst.getGeneratedKeys()) {
                                    if (insertRs.next()) {
                                        inserted++;
                                    }
                                }
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
            String status = tableModel.getValueAt(i, 3).toString();
            switch (status) {
                case "Present": present++; break;
                case "Absent": absent++; break;
                case "Late": late++; break;
            }
        }
        
        summaryLabel.setText(String.format("Total: %d | Present: %d | Absent: %d | Late: %d", total, present, absent, late));
    }
            
    private void filterTable() {
        String searchText = searchField.getText().trim().toLowerCase();
        String departmentFilterText = departmentFilter.getSelectedItem().toString();
        
        // Create a list to hold all filters
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        
        // Apply department filter if not "All"
        if (!departmentFilterText.equals("All")) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(departmentFilterText), 2));
        }
        
        // Apply search text filter if not empty
        if (!searchText.isEmpty()) {
            RowFilter<Object, Object> searchFilter = new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    // Search in Student ID (0), Name (1), and Department (2)
                    for (int i = 0; i <= 2; i++) {
                        String cellValue = entry.getValue(i) != null ? 
                            entry.getValue(i).toString().toLowerCase() : "";
                        if (cellValue.contains(searchText)) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            filters.add(searchFilter);
        }
        
        // Apply all filters
        try {
            sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        } catch (Exception e) {
            sorter.setRowFilter(null);
        }
        
        updateSummary();
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
