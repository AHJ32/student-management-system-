import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class StudentManagementSystem extends JFrame implements ActionListener {
    // Database configuration
    private static final String DB_HOST = "localhost:3306";
    private static final String DB_NAME = "sms";
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + "/" + DB_NAME + "?useSSL=false";
    private static final String DB_ROOT_URL = "jdbc:mysql://" + DB_HOST + "/?useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private Connection connection;
    
    // UI Components
    JLabel jtitle;
    JLabel studentName, studentID, departmentLabel, semesterLabel, genderLabel, contactLabel, emailLabel;
    JTextField jstudentName, jstudentID, contactField, emailField, searchField;
    JComboBox<String> departmentCombo, semesterCombo;
    JRadioButton maleRadio, femaleRadio;
    ButtonGroup genderGroup;
    JButton addStudent, reset, deleteRecord, searchButton, editProfileButton;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JButton backButton;

    public StudentManagementSystem() {
        setTitle("Student Management System by Group 5");
        setLayout(null);
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        jtitle = new JLabel("STUDENT MANAGEMENT SYSTEM");
        jtitle.setBounds(250, 25, 700, 50);
        jtitle.setFont(new Font("Comic Sans MS", Font.PLAIN, 30));

        studentName = new JLabel("Student Name");
        studentName.setBounds(100, 80, 150, 30);

        studentID = new JLabel("Student ID");
        studentID.setBounds(100, 120, 150, 30);

        departmentLabel = new JLabel("Department");
        departmentLabel.setBounds(100, 160, 150, 30);

        semesterLabel = new JLabel("Semester");
        semesterLabel.setBounds(100, 200, 150, 30);

        genderLabel = new JLabel("Gender");
        genderLabel.setBounds(100, 240, 150, 30);

        contactLabel = new JLabel("Contact Number");
        contactLabel.setBounds(100, 280, 150, 30);

        emailLabel = new JLabel("Email");
        emailLabel.setBounds(100, 320, 150, 30);

        jstudentName = new JTextField();
        jstudentName.setBounds(250, 80, 200, 30);

        jstudentID = new JTextField();
        jstudentID.setBounds(250, 120, 200, 30);

        // Department dropdown
        String[] departments = {"Computer", "Civil", "Mechanical", "Architecture", "Electrical"};
        departmentCombo = new JComboBox<>(departments);
        departmentCombo.setBounds(250, 160, 200, 30);

        // Semester dropdown
        String[] semesters = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};
        semesterCombo = new JComboBox<>(semesters);
        semesterCombo.setBounds(250, 200, 200, 30);

        maleRadio = new JRadioButton("Male");
        maleRadio.setBounds(250, 240, 80, 30);

        femaleRadio = new JRadioButton("Female");
        femaleRadio.setBounds(340, 240, 100, 30);

        genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);

        contactField = new JTextField();
        contactField.setBounds(250, 280, 200, 30);

        emailField = new JTextField();
        emailField.setBounds(250, 320, 200, 30);

        addStudent = new JButton("Add Student");
        addStudent.setBounds(650, 150, 150, 30);

        reset = new JButton("Reset");
        reset.setBounds(650, 200, 150, 30);

        deleteRecord = new JButton("Delete Record");
        deleteRecord.setBounds(650, 250, 150, 30);
        
        editProfileButton = new JButton("Edit Profile");
        editProfileButton.setBounds(650, 300, 150, 30);

        backButton = new JButton("Back to Home");
        backButton.setBounds(20, 20, 120, 30);
        backButton.addActionListener(this);

        searchField = new JTextField();
        searchField.setBounds(100, 360, 300, 30);

        searchButton = new JButton("Search by ID");
        searchButton.setBounds(410, 360, 150, 30);

        add(jtitle);
        add(studentName);
        add(jstudentName);
        add(studentID);
        add(jstudentID);
        add(departmentLabel);
        add(departmentCombo);
        add(semesterLabel);
        add(semesterCombo);
        add(genderLabel);
        add(maleRadio);
        add(femaleRadio);
        add(contactLabel);
        add(contactField);
        add(emailLabel);
        add(emailField);
        add(addStudent);
        add(reset);
        add(deleteRecord);
        add(editProfileButton);
        add(backButton);
        add(searchField);
        add(searchButton);

        String[] columnNames = {"Student Name", "Student ID", "Department", "Semester", "Gender", "Contact Name", "Email"};
        tableModel = new DefaultTableModel(columnNames, 0);

        studentTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make all cells non-editable
                return false;
            }
        };
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBounds(100, 420, 800, 150);
        add(scrollPane);

        addStudent.addActionListener(this);
        reset.addActionListener(this);
        deleteRecord.addActionListener(this);
        searchButton.addActionListener(this);
        editProfileButton.addActionListener(this);

        // Initialize database connection and load data
        if (connectToDatabase()) {
            loadStudentDataFromDatabase();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to connect to database. The application will now exit.",
                "Fatal Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Make the window visible after all components are added
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            // Return to home page
            SwingUtilities.invokeLater(() -> {
                HomePage homePage = new HomePage();
                homePage.setVisible(true);
                this.dispose();
            });
            return;
        } else if (e.getSource() == addStudent) {
            String name = jstudentName.getText();
            String id = jstudentID.getText();
            String department = (String) departmentCombo.getSelectedItem();
            String semester = (String) semesterCombo.getSelectedItem();
            String contact = contactField.getText();
            String email = emailField.getText();
            String gender = maleRadio.isSelected() ? "Male" : "Female";

            if (name.isEmpty() || contact.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email address.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!isNumeric(id)) {
                JOptionPane.showMessageDialog(this, "Invalid student ID. It should be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!isValidContactNumber(contact)) {
                JOptionPane.showMessageDialog(this, "Invalid contact number. It should be numeric.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                String[] data = {name, id, department, semester, gender, contact, email};
                tableModel.addRow(data);

                jstudentName.setText("");
                jstudentID.setText("");
                departmentCombo.setSelectedIndex(0);
                semesterCombo.setSelectedIndex(0);
                genderGroup.clearSelection();
                contactField.setText("");
                emailField.setText("");
                insertStudentData(name, id, department, semester, gender, contact, email);
            }
        }

        if (e.getSource() == reset) {
            jstudentName.setText("");
            jstudentID.setText("");
            departmentCombo.setSelectedIndex(0);
            semesterCombo.setSelectedIndex(0);
            genderGroup.clearSelection();
            contactField.setText("");
            emailField.setText("");
        }

        if (e.getSource() == deleteRecord) {
            String studentIDToDelete = null; 
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow >= 0) {
                studentIDToDelete = tableModel.getValueAt(selectedRow, 1).toString();
                tableModel.removeRow(selectedRow);
                
                deleteStudentData(studentIDToDelete);
            }
        }
        
        if (e.getSource() == searchButton) {
            String searchId = searchField.getText();
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                if (tableModel.getValueAt(row, 1).equals(searchId)) {
                    studentTable.setRowSelectionInterval(row, row);
                    studentTable.setSelectionBackground(Color.YELLOW);
                    studentTable.setSelectionForeground(Color.BLACK);
                    break;
                }
            }
        } else if (e.getSource() == editProfileButton) {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow >= 0) {
                editStudentProfile(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a student to edit.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    public boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }
    
    private boolean isValidContactNumber(String contact) {
        return contact.matches("^\\d+$");
    }
    

    private void closeDatabaseConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private boolean connectToDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void loadStudentDataFromDatabase() {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM students LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {

            // Get metadata to discover column names
            java.sql.ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Print column names for debugging
            System.out.println("Available columns in 'students' table:");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println(i + ". " + metaData.getColumnName(i));
            }

            // Now fetch all data with the correct column names
            try (PreparedStatement stmtAll = connection.prepareStatement("SELECT * FROM students");
                 ResultSet rsAll = stmtAll.executeQuery()) {

                while (rsAll.next()) {
                    // Use the actual column names from your database
                    String name = rsAll.getString("name");
                    String id = rsAll.getString("id");
                    String department = rsAll.getString("department");
                    String semester = rsAll.getString("semester");
                    String gender = rsAll.getString("gender");
                    String contact = rsAll.getString("contact");
                    String email = rsAll.getString("email");

                    tableModel.addRow(new Object[]{name, id, department, semester, gender, contact, email});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading student data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void insertStudentData(String name, String id, String department, String semester, 
                                 String gender, String contact, String email) {
        String sql = "INSERT INTO students (name, id, department, semester, gender, contact, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, id);
            pstmt.setString(3, department);
            pstmt.setString(4, semester);
            pstmt.setString(5, gender);
            pstmt.setString(6, contact);
            pstmt.setString(7, email);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error saving student data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editStudentProfile(int rowIndex) {
        // Get student data from the selected row
        String id = tableModel.getValueAt(rowIndex, 1).toString();
        
        // Create a dialog for editing
        JDialog editDialog = new JDialog(this, "Edit Student Profile", true);
        editDialog.setLayout(new GridLayout(0, 2, 5, 5));
        editDialog.setSize(400, 300);
        editDialog.setLocationRelativeTo(this);
        
        // Create form fields with current data
        JTextField nameField = new JTextField(tableModel.getValueAt(rowIndex, 0).toString());
        JTextField idField = new JTextField(id);
        // Allow editing ID since it's not a database primary key
        JComboBox<String> deptCombo = new JComboBox<>(new String[]{"Computer", "Civil", "Mechanical", "Architecture", "Electrical"});
        deptCombo.setSelectedItem(tableModel.getValueAt(rowIndex, 2).toString());
        
        JComboBox<String> semCombo = new JComboBox<>(new String[]{"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"});
        semCombo.setSelectedItem(tableModel.getValueAt(rowIndex, 3).toString());
        
        JRadioButton maleRadio = new JRadioButton("Male");
        JRadioButton femaleRadio = new JRadioButton("Female");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        String gender = tableModel.getValueAt(rowIndex, 4).toString();
        if (gender.equals("Male")) maleRadio.setSelected(true);
        else femaleRadio.setSelected(true);
        
        JTextField contactField = new JTextField(tableModel.getValueAt(rowIndex, 5).toString());
        JTextField emailField = new JTextField(tableModel.getValueAt(rowIndex, 6).toString());
        
        // Add components to dialog
        editDialog.add(new JLabel("Name:"));
        editDialog.add(nameField);
        editDialog.add(new JLabel("ID:"));
        editDialog.add(idField);
        editDialog.add(new JLabel("Department:"));
        editDialog.add(deptCombo);
        editDialog.add(new JLabel("Semester:"));
        editDialog.add(semCombo);
        editDialog.add(new JLabel("Gender:"));
        JPanel genderPanel = new JPanel();
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        editDialog.add(genderPanel);
        editDialog.add(new JLabel("Contact:"));
        editDialog.add(contactField);
        editDialog.add(new JLabel("Email:"));
        editDialog.add(emailField);
        
        // Add save and cancel buttons
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(ae -> {
            String newId = idField.getText().trim();
            
            // Validate the new ID
            if (newId.isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, 
                    "Student ID cannot be empty.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!isNumeric(newId)) {
                JOptionPane.showMessageDialog(editDialog, 
                    "Student ID must be a number.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if ID is being changed to a value that already exists
            if (!newId.equals(id)) {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT COUNT(*) FROM students WHERE id = ?")) {
                    stmt.setString(1, newId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(editDialog, 
                                "A student with this ID already exists.", 
                                "Duplicate ID", 
                                JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(editDialog, 
                        "Error checking student ID: " + ex.getMessage(), 
                        "Database Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // If ID was changed, we need to delete the old record and insert a new one
            if (!newId.equals(id)) {
                try {
                    // Start a transaction
                    connection.setAutoCommit(false);
                    
                    // Delete the old record
                    deleteStudentData(id);
                    
                    // Insert a new record with the updated ID
                    insertStudentData(
                        nameField.getText(),
                        newId,
                        deptCombo.getSelectedItem().toString(),
                        semCombo.getSelectedItem().toString(),
                        maleRadio.isSelected() ? "Male" : "Female",
                        contactField.getText(),
                        emailField.getText()
                    );
                    
                    connection.commit();
                } catch (SQLException ex) {
                    try {
                        connection.rollback();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(editDialog, 
                        "Error updating student: " + ex.getMessage(), 
                        "Database Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                } finally {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                
                // Update the table with the new ID
                tableModel.setValueAt(newId, rowIndex, 1);
            } else {
                // Just update the existing record if ID wasn't changed
                updateStudentInDatabase(
                    id,
                    nameField.getText(),
                    deptCombo.getSelectedItem().toString(),
                    semCombo.getSelectedItem().toString(),
                    maleRadio.isSelected() ? "Male" : "Female",
                    contactField.getText(),
                    emailField.getText()
                );
            }
            
            // Update the rest of the table data
            tableModel.setValueAt(nameField.getText(), rowIndex, 0);
            tableModel.setValueAt(deptCombo.getSelectedItem().toString(), rowIndex, 2);
            tableModel.setValueAt(semCombo.getSelectedItem().toString(), rowIndex, 3);
            tableModel.setValueAt(maleRadio.isSelected() ? "Male" : "Female", rowIndex, 4);
            tableModel.setValueAt(contactField.getText(), rowIndex, 5);
            tableModel.setValueAt(emailField.getText(), rowIndex, 6);
            
            editDialog.dispose();
        });
        
        cancelButton.addActionListener(ae -> editDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        editDialog.add(new JLabel()); // Empty cell for layout
        editDialog.add(buttonPanel);
        
        editDialog.setVisible(true);
    }
    
    private void updateStudentInDatabase(String id, String name, String department, 
                                       String semester, String gender, String contact, String email) {
        String sql = "UPDATE students SET name = ?, department = ?, semester = ?, " +
                    "gender = ?, contact = ?, email = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, department);
            pstmt.setString(3, semester);
            pstmt.setString(4, gender);
            pstmt.setString(5, contact);
            pstmt.setString(6, email);
            pstmt.setString(7, id);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error updating student: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteStudentData(String studentId) {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM students WHERE id = ?")) {
            pstmt.setString(1, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error deleting student: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    {
        // Add shutdown hook to properly close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                closeDatabaseConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public static void main(String[] args) {
        // This is now handled by HomePage
        SwingUtilities.invokeLater(() -> {
            HomePage homePage = new HomePage();
            homePage.setVisible(true);
        });
    }
}
