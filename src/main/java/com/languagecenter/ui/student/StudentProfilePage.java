package com.languagecenter.ui.student;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.Student;
import com.languagecenter.service.StudentService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class StudentProfilePage extends JPanel {
    private final Student student;
    private final StudentService studentService;
    private final String username;

    private JTextField txtPhone, txtEmail, txtAddress;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");

    public StudentProfilePage(Student student, String username, StudentService ss) {
        this.student = student;
        this.studentService = ss;
        this.username = username;

        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 40, 25, 40));
        buildUI();
    }

    private void buildUI() {
        // Main container with scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 247, 250));
        
        // Header Section
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Account Information Section
        JPanel accountPanel = createAccountInfoPanel();
        mainPanel.add(accountPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Personal Information Section
        JPanel personalPanel = createPersonalInfoPanel();
        mainPanel.add(personalPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Contact Information Section
        JPanel contactPanel = createContactInfoPanel();
        mainPanel.add(contactPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Action Buttons
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(103, 58, 183), 2),
            new EmptyBorder(20, 25, 20, 25)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:15");
        
        // Avatar section (icon)
        JLabel avatarIcon = new JLabel("👨‍🎓");
        avatarIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        avatarIcon.setHorizontalAlignment(SwingConstants.CENTER);
        avatarIcon.setPreferredSize(new Dimension(100, 100));
        avatarIcon.setOpaque(true);
        avatarIcon.setBackground(new Color(237, 231, 246));
        avatarIcon.setBorder(BorderFactory.createLineBorder(new Color(103, 58, 183), 2));
        
        // Info section
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel lblName = new JLabel(student.getFullName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblName.setForeground(new Color(103, 58, 183));
        
        JLabel lblId = new JLabel("Student ID: STD-" + String.format("%04d", student.getId()));
        lblId.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblId.setForeground(new Color(100, 100, 100));
        
        JLabel lblStatus = new JLabel("Status: " + student.getStatus().name());
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setForeground(student.getStatus().name().equals("ACTIVE") ? 
            new Color(46, 204, 113) : new Color(231, 76, 60));
        
        infoPanel.add(lblName);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblId);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblStatus);
        
        panel.add(avatarIcon, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createAccountInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "📋 Account Information", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16), new Color(103, 58, 183)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:15");
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        addInfoRow(panel, gbc, "Username:", username, 0);
        addInfoRow(panel, gbc, "Date of Birth:", 
            student.getDateOfBirth() != null ? student.getDateOfBirth().format(formatter) : "N/A", 1);
        addInfoRow(panel, gbc, "Gender:", 
            student.getGender() != null ? student.getGender().name() : "N/A", 2);
        addInfoRow(panel, gbc, "Registration Date:", 
            student.getRegistrationDate() != null ? student.getRegistrationDate().format(formatter) : "N/A", 3);
        addInfoRow(panel, gbc, "Account Status:", 
            student.getStatus() != null ? student.getStatus().name() : "N/A", 4);
        
        return panel;
    }

    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "👤 Contact Information (Editable)", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16), new Color(103, 58, 183)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:15");
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        txtPhone = new JTextField(student.getPhone());
        txtEmail = new JTextField(student.getEmail());
        
        styleTextField(txtPhone);
        styleTextField(txtEmail);
        
        addEditRow(panel, gbc, "📱 Phone Number:", txtPhone, 0, 
            "10-11 digit phone number");
        addEditRow(panel, gbc, "📧 Email Address:", txtEmail, 1, 
            "Valid email format required");
        
        return panel;
    }

    private JPanel createContactInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "🏠 Address Information", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16), new Color(103, 58, 183)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:15");
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        txtAddress = new JTextField(student.getAddress());
        styleTextField(txtAddress);
        
        addEditRow(panel, gbc, "📍 Full Address:", txtAddress, 0, 
            "Your current residential address");
        
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setOpaque(false);
        
        JButton btnSave = new JButton("💾 Save Changes");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setPreferredSize(new Dimension(180, 45));
        btnSave.putClientProperty(FlatClientProperties.STYLE, 
            "background:#673ab7; foreground:#ffffff; arc:10; borderWidth:0");
        btnSave.addActionListener(e -> saveProfile());
        
        JButton btnReset = new JButton("🔄 Reset");
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnReset.setPreferredSize(new Dimension(120, 45));
        btnReset.putClientProperty(FlatClientProperties.STYLE, 
            "background:#7f8c8d; foreground:#ffffff; arc:10; borderWidth:0");
        btnReset.addActionListener(e -> resetFields());
        
        panel.add(btnSave);
        panel.add(btnReset);
        
        return panel;
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKey.setForeground(new Color(70, 70, 70));
        panel.add(lblKey, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblValue.setForeground(new Color(100, 100, 100));
        panel.add(lblValue, gbc);
    }

    private void addEditRow(JPanel panel, GridBagConstraints gbc, String label, 
                           JTextField field, int row, String hint) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKey.setForeground(new Color(70, 70, 70));
        panel.add(lblKey, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 5));
        fieldPanel.setOpaque(false);
        fieldPanel.add(field, BorderLayout.CENTER);
        
        JLabel lblHint = new JLabel(hint);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(new Color(150, 150, 150));
        fieldPanel.add(lblHint, BorderLayout.SOUTH);
        
        panel.add(fieldPanel, gbc);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(400, 38));
        field.putClientProperty(FlatClientProperties.STYLE, 
            "arc:8; borderWidth:2; focusedBorderColor:#673ab7");
    }

    private boolean validateInputs() {
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();
        
        if (phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "All fields are required!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            JOptionPane.showMessageDialog(this, 
                "Invalid phone number! Must be 10-11 digits.", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPhone.requestFocus();
            return false;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, 
                "Invalid email format!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }
        
        if (address.length() < 10) {
            JOptionPane.showMessageDialog(this, 
                "Address must be at least 10 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtAddress.requestFocus();
            return false;
        }
        
        return true;
    }

    private void saveProfile() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            student.setPhone(txtPhone.getText().trim());
            student.setEmail(txtEmail.getText().trim());
            student.setAddress(txtAddress.getText().trim());

            studentService.update(student, username, null);
            
            JOptionPane.showMessageDialog(this, 
                "✅ Profile updated successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "❌ Update error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFields() {
        txtPhone.setText(student.getPhone());
        txtEmail.setText(student.getEmail());
        txtAddress.setText(student.getAddress());
    }
}