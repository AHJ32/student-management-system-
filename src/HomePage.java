import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomePage extends JFrame implements ActionListener {
    private JButton studentManagementBtn;
    private JButton attendanceBtn;
    private JButton gradesBtn;
    private JButton settingsBtn;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel welcomeLabel;
    
    public HomePage() {
        setTitle("Student Management System - Home");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize components
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        titleLabel = new JLabel("Student Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Welcome message
        welcomeLabel = new JLabel("Welcome, Admin", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Buttons Panel
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 200, 30, 200));
        
        studentManagementBtn = createMenuButton("Student Management");
        attendanceBtn = createMenuButton("Attendance");
        gradesBtn = createMenuButton("Grades");
        settingsBtn = createMenuButton("Settings");
        
        buttonPanel.add(studentManagementBtn);
        buttonPanel.add(attendanceBtn);
        buttonPanel.add(gradesBtn);
        buttonPanel.add(settingsBtn);
        
        // Add components to main panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Add some styling
        mainPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setOpaque(false);
        
        add(mainPanel);
        
        // Add action listeners
        studentManagementBtn.addActionListener(this);
        attendanceBtn.addActionListener(this);
        gradesBtn.addActionListener(this);
        settingsBtn.addActionListener(this);
        
        // Set application icon
        try {
            // You can replace this with your own icon
            // ImageIcon icon = new ImageIcon("path/to/your/icon.png");
            // setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setPreferredSize(new Dimension(300, 60));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(65, 105, 225)); // Darker blue on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180)); // Original color
            }
        });
        
        return button;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == studentManagementBtn) {
            // Open Student Management
            SwingUtilities.invokeLater(() -> {
                StudentManagementSystem sms = new StudentManagementSystem();
                sms.setVisible(true);
                this.dispose();
            });
        } else if (e.getSource() == attendanceBtn) {
            // Open Attendance
            SwingUtilities.invokeLater(() -> {
                AttendancePage attendancePage = new AttendancePage();
                attendancePage.setVisible(true);
                this.dispose();
            });
        } else if (e.getSource() == gradesBtn) {
            // Open Grades
            SwingUtilities.invokeLater(() -> {
                GradesPage gradesPage = new GradesPage();
                gradesPage.setVisible(true);
                this.dispose();
            });
        } else if (e.getSource() == settingsBtn) {
            // Open Settings
            SwingUtilities.invokeLater(() -> {
                SettingsPage settingsPage = new SettingsPage();
                settingsPage.setVisible(true);
                this.dispose();
            });
        }
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            HomePage homePage = new HomePage();
            homePage.setVisible(true);
        });
    }
}
