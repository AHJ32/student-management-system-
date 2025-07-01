import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class GradesPage extends JFrame implements ActionListener {
    private JButton backButton;
    private JButton addGradeButton;
    private JButton generateReportButton;
    private JTable gradesTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> subjectCombo;
    private JComboBox<String> semesterCombo;
    
    public GradesPage() {
        setTitle("Student Management System - Grades");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        JLabel titleLabel = new JLabel("Student Grades Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        // Back button
        backButton = new JButton("Back to Home");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.addActionListener(this);
        
        // Subject filter
        JLabel subjectLabel = new JLabel("Subject:");
        String[] subjects = {"All Subjects", "Mathematics", "Physics", "Chemistry", "Computer Science", "English"};
        subjectCombo = new JComboBox<>(subjects);
        subjectCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        subjectCombo.addActionListener(e -> filterTable());
        
        // Semester filter
        JLabel semesterLabel = new JLabel("Semester:");
        String[] semesters = {"All Semesters", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};
        semesterCombo = new JComboBox<>(semesters);
        semesterCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        semesterCombo.addActionListener(e -> filterTable());
        
        // Action buttons
        addGradeButton = new JButton("Add/Edit Grade");
        addGradeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        addGradeButton.addActionListener(e -> showAddGradeDialog());
        
        generateReportButton = new JButton("Generate Report");
        generateReportButton.setFont(new Font("Arial", Font.PLAIN, 14));
        generateReportButton.addActionListener(e -> generateReport());
        
        // Add components to control panel
        controlPanel.add(backButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(subjectLabel);
        controlPanel.add(subjectCombo);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(semesterLabel);
        controlPanel.add(semesterCombo);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(addGradeButton);
        controlPanel.add(generateReportButton);
        
        // Table setup
        String[] columns = {"Student ID", "Student Name", "Subject", "Semester", "Marks", "Grade", "Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        gradesTable = new JTable(tableModel);
        gradesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        gradesTable.setRowHeight(25);
        gradesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add sample data
        addSampleData();
        
        // Enable sorting
        gradesTable.setAutoCreateRowSorter(true);
        
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        // Add components to frame
        add(titlePanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
        
        // Set some styling
        getContentPane().setBackground(new Color(240, 240, 240));
        controlPanel.setBackground(new Color(240, 240, 240));
        titlePanel.setBackground(new Color(240, 240, 240));
    }
    
    private void addSampleData() {
        // Sample data - in a real app, this would come from a database
        String[][] data = {
            {"1001", "John Doe", "Mathematics", "3rd", "85", "A", "Excellent"},
            {"1001", "John Doe", "Physics", "3rd", "78", "B+", "Good"},
            {"1002", "Jane Smith", "Mathematics", "3rd", "92", "A+", "Outstanding"},
            {"1002", "Jane Smith", "Physics", "3rd", "85", "A", "Excellent"},
            {"1003", "Bob Johnson", "Mathematics", "4th", "65", "C+", "Satisfactory"},
            {"1003", "Bob Johnson", "Chemistry", "4th", "72", "B-", "Good"},
            {"1004", "Alice Brown", "Computer Science", "5th", "88", "A", "Excellent"},
            {"1005", "Charlie Wilson", "English", "2nd", "95", "A+", "Outstanding"}
        };
        
        for (String[] row : data) {
            tableModel.addRow(row);
        }
    }
    
    private void filterTable() {
        // In a real app, this would filter the data from the database
        // For now, we'll just show a message
        String subject = (String) subjectCombo.getSelectedItem();
        String semester = (String) semesterCombo.getSelectedItem();
        
        if ((!subject.equals("All Subjects") || !semester.equals("All Semesters"))) {
            JOptionPane.showMessageDialog(this,
                "Filtering by: " + subject + ", " + semester + "\n" +
                "(In a real app, this would filter the table data)",
                "Filter Applied",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showAddGradeDialog() {
        JDialog dialog = new JDialog(this, "Add/Edit Grade", true);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        // Form fields
        JTextField studentIdField = new JTextField();
        JTextField studentNameField = new JTextField();
        JComboBox<String> subjectField = new JComboBox<>(
            new String[]{"Mathematics", "Physics", "Chemistry", "Computer Science", "English"});
        JComboBox<String> semesterField = new JComboBox<>(
            new String[]{"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"});
        JTextField marksField = new JTextField();
        
        // Add components to dialog
        dialog.add(new JLabel("Student ID:"));
        dialog.add(studentIdField);
        dialog.add(new JLabel("Student Name:"));
        dialog.add(studentNameField);
        dialog.add(new JLabel("Subject:"));
        dialog.add(subjectField);
        dialog.add(new JLabel("Semester:"));
        dialog.add(semesterField);
        dialog.add(new JLabel("Marks (0-100):"));
        dialog.add(marksField);
        
        // Add buttons
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            // In a real app, this would save to the database
            JOptionPane.showMessageDialog(dialog,
                "Grade saved successfully!\n" +
                "(In a real app, this would update the database)",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        dialog.add(new JLabel()); // Empty cell for layout
        dialog.add(buttonPanel);
        
        dialog.setVisible(true);
    }
    
    private void generateReport() {
        // In a real app, this would generate a PDF or Excel report
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Grade Report");
        fileChooser.setSelectedFile(new java.io.File("Grade_Report.pdf"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this,
                "Report generated successfully at:\n" + fileToSave.getAbsolutePath() + 
                "\n\n(In a real app, this would be a PDF/Excel file)",
                "Report Generated",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            // Return to home page
            SwingUtilities.invokeLater(() -> {
                HomePage homePage = new HomePage();
                homePage.setVisible(true);
                this.dispose();
            });
        }
    }
}
