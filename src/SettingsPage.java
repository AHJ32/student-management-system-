import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsPage extends JFrame implements ActionListener {
    private JButton backButton;
    private JButton saveButton;
    private JComboBox<String> themeCombo;
    
    public SettingsPage() {
        setTitle("Student Management System - Settings");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Settings", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Back button
        backButton = new JButton("Back to Home");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.addActionListener(this);
        
        // Save button
        saveButton = new JButton("Save Settings");
        saveButton.setFont(new Font("Arial", Font.PLAIN, 14));
        saveButton.addActionListener(this);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(backButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(saveButton);
        
        // Settings panel
        JPanel settingsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Theme selection
        settingsPanel.add(new JLabel("Theme:"));
        String[] themes = {"Light", "Dark", "System Default"};
        themeCombo = new JComboBox<>(themes);
        themeCombo.setSelectedItem("Light");
        settingsPanel.add(themeCombo);
        
        // Add some spacing
        for (int i = 0; i < 8; i++) {
            settingsPanel.add(Box.createGlue());
        }
        
        // Add components to frame
        add(titleLabel, BorderLayout.NORTH);
        add(settingsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            // Return to home page
            HomePage homePage = new HomePage();
            homePage.setVisible(true);
            this.dispose();
        } else if (e.getSource() == saveButton) {
            // Save settings (placeholder)
            String selectedTheme = (String) themeCombo.getSelectedItem();
            JOptionPane.showMessageDialog(this, 
                "Settings saved!\nSelected Theme: " + selectedTheme,
                "Settings Saved",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
