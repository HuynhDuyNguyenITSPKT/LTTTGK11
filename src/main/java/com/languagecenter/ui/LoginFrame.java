package com.languagecenter.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.Student;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.UserRole;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

public class LoginFrame extends JFrame {
    private final AuthService authService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;
    private final RoomService roomService;
    private final ClassService classService;
    private final ScheduleService scheduleService;
    private final EnrollmentService enrollmentService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final AttendanceService attendanceService;
    private final ResultService resultService;

    // UI Components
    private CardLayout cardLayout;
    private JPanel cardPanel;

    // Login fields
    private JTextField txtLoginUser;
    private JPasswordField txtLoginPass;

    // Register fields  
    private JTextField txtRegFullName;
    private JComboBox<String> cboRegGender;
    private JTextField txtRegEmail;
    private JTextField txtRegPhone;
    private JTextField txtRegAddress;
    private JTextField txtRegUsername;
    private JPasswordField txtRegPassword;
    private JPasswordField txtRegConfirmPass;

    private final Color PRIMARY = new Color(79, 70, 229);
    private final Color BORDER = new Color(226, 232, 240);
    private final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private final Color TEXT_SECONDARY = new Color(100, 116, 139);

    public LoginFrame(AuthService authService, StudentService studentService,
                      TeacherService teacherService, CourseService courseService, RoomService roomService,
                      ClassService classService, ScheduleService scheduleService, EnrollmentService enrollmentService,
                      InvoiceService invoiceService, PaymentService paymentService,
                      AttendanceService attendanceService, ResultService resultService) {
        super("System Login");
        this.authService = authService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.courseService = courseService;
        this.roomService = roomService;
        this.classService = classService;
        this.scheduleService = scheduleService;
        this.enrollmentService = enrollmentService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.attendanceService = attendanceService;
        this.resultService = resultService;

        buildUI();

        setSize(1050, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildUI() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);

        // --- LEFT SIDE: LOGO/IMAGE ---
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(450, 0));
        imagePanel.setBackground(PRIMARY);

        JPanel logoContainer = new JPanel();
        logoContainer.setLayout(new BoxLayout(logoContainer, BoxLayout.Y_AXIS));
        logoContainer.setOpaque(false);
        logoContainer.setBorder(new EmptyBorder(80, 50, 80, 50));

        JLabel lblImage = new JLabel();
        java.net.URL imgURL = getClass().getResource("/images/logo2.jpg");
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(new ImageIcon(imgURL).getImage()
                    .getScaledInstance(280, 280, Image.SCALE_SMOOTH));
            lblImage.setIcon(icon);
            lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        } else {
            lblImage.setText("<html><div style='color:white; text-align:center; font-size:48px;'><b>ENGLISH<br/>CENTER</b></div></html>");
            lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        JLabel lblSubtitle = new JLabel("Language Center Management System");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblSubtitle.setForeground(new Color(226, 232, 240));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoContainer.add(lblImage);
        logoContainer.add(Box.createVerticalStrut(20));
        logoContainer.add(lblSubtitle);

        imagePanel.add(logoContainer, BorderLayout.CENTER);

        // --- RIGHT SIDE: FORM WITH CARDLAYOUT ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);

        // Login Panel
        JPanel loginPanel = createLoginPanel();
        cardPanel.add(loginPanel, "LOGIN");

        // Register Panel
        JPanel registerPanel = createRegisterPanel();
        cardPanel.add(registerPanel, "REGISTER");

        container.add(imagePanel, BorderLayout.WEST);
        container.add(cardPanel, BorderLayout.CENTER);

        setContentPane(container);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;

        // Title
        JLabel lblTitle = new JLabel("Sign In");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(TEXT_PRIMARY);
        g.gridy = 0; g.insets = new Insets(0, 0, 10, 0);
        panel.add(lblTitle, g);

        // Subtitle
        JLabel lblSubtitle = new JLabel("Enter your credentials to access your account");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(TEXT_SECONDARY);
        g.gridy = 1; g.insets = new Insets(0, 0, 30, 0);
        panel.add(lblSubtitle, g);

        // Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(TEXT_PRIMARY);
        g.gridy = 2; g.insets = new Insets(0, 0, 8, 0);
        panel.add(lblUser, g);

        txtLoginUser = createTextField("Enter your username", 360);
        g.gridy = 3; g.insets = new Insets(0, 0, 18, 0);
        panel.add(txtLoginUser, g);

        // Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(TEXT_PRIMARY);
        g.gridy = 4; g.insets = new Insets(0, 0, 8, 0);
        panel.add(lblPass, g);

        txtLoginPass = createPasswordField("Enter your password", 360);
        g.gridy = 5; g.insets = new Insets(0, 0, 25, 0);
        panel.add(txtLoginPass, g);

        // Login button
        JButton btnLogin = createPrimaryButton("LOGIN", 360);
        btnLogin.addActionListener(e -> login());
        g.gridy = 6; g.insets = new Insets(0, 0, 20, 0);
        panel.add(btnLogin, g);

        // Register link
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        linkPanel.setOpaque(false);

        JLabel lblNoAccount = new JLabel("Don't have an account?");
        lblNoAccount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNoAccount.setForeground(TEXT_SECONDARY);

        JLabel lblRegister = new JLabel("Register here");
        lblRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRegister.setForeground(PRIMARY);
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegister.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cardLayout.show(cardPanel, "REGISTER");
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblRegister.setText("<html><u>Register here</u></html>");
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblRegister.setText("Register here");
            }
        });

        linkPanel.add(lblNoAccount);
        linkPanel.add(lblRegister);

        g.gridy = 7; g.insets = new Insets(0, 0, 0, 0);
        panel.add(linkPanel, g);

        getRootPane().setDefaultButton(btnLogin);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 50, 20, 50));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;

        // Title
        JLabel lblTitle = new JLabel("Create Account");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(TEXT_PRIMARY);
        g.gridy = 0; g.insets = new Insets(0, 0, 8, 0);
        panel.add(lblTitle, g);

        // Subtitle
        JLabel lblSubtitle = new JLabel("Fill in the information below to register");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(TEXT_SECONDARY);
        g.gridy = 1; g.insets = new Insets(0, 0, 25, 0);
        panel.add(lblSubtitle, g);

        // Full Name
        panel.add(createFieldLabel("Full Name *"), createGBC(g, 2, 0, 0, 6));
        txtRegFullName = createTextField("John Doe", 360);
        panel.add(txtRegFullName, createGBC(g, 3, 0, 0, 14));

        // Gender
        panel.add(createFieldLabel("Gender *"), createGBC(g, 4, 0, 0, 6));
        cboRegGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cboRegGender.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboRegGender.setPreferredSize(new Dimension(360, 40));
        cboRegGender.setBackground(Color.WHITE);
        cboRegGender.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));
        panel.add(cboRegGender, createGBC(g, 5, 0, 0, 14));

        // Email
        panel.add(createFieldLabel("Email *"), createGBC(g, 6, 0, 0, 6));
        txtRegEmail = createTextField("example@email.com", 360);
        panel.add(txtRegEmail, createGBC(g, 7, 0, 0, 14));

        // Phone
        panel.add(createFieldLabel("Phone *"), createGBC(g, 8, 0, 0, 6));
        txtRegPhone = createTextField("0123456789", 360);
        panel.add(txtRegPhone, createGBC(g, 9, 0, 0, 14));

        // Address
        panel.add(createFieldLabel("Address"), createGBC(g, 10, 0, 0, 6));
        txtRegAddress = createTextField("Full address (optional)", 360);
        panel.add(txtRegAddress, createGBC(g, 11, 0, 0, 14));

        // Username
        panel.add(createFieldLabel("Username *"), createGBC(g, 12, 0, 0, 6));
        txtRegUsername = createTextField("Choose a username", 360);
        panel.add(txtRegUsername, createGBC(g, 13, 0, 0, 14));

        // Password
        panel.add(createFieldLabel("Password *"), createGBC(g, 14, 0, 0, 6));
        txtRegPassword = createPasswordField("Enter password", 360);
        panel.add(txtRegPassword, createGBC(g, 15, 0, 0, 14));

        // Confirm Password
        panel.add(createFieldLabel("Confirm Password *"), createGBC(g, 16, 0, 0, 6));
        txtRegConfirmPass = createPasswordField("Re-enter password", 360);
        panel.add(txtRegConfirmPass, createGBC(g, 17, 0, 0, 18));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = createSecondaryButton("Back to Login", 175);
        btnCancel.addActionListener(e -> {
            clearRegisterFields();
            cardLayout.show(cardPanel, "LOGIN");
        });

        JButton btnRegister = createPrimaryButton("REGISTER", 175);
        btnRegister.addActionListener(e -> handleRegistration());

        btnPanel.add(btnCancel);
        btnPanel.add(btnRegister);

        g.gridy = 18; g.insets = new Insets(0, 0, 0, 0);
        panel.add(btnPanel, g);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private GridBagConstraints createGBC(GridBagConstraints g, int gridy, int top, int left, int bottom) {
        g.gridy = gridy;
        g.insets = new Insets(top, left, bottom, 0);
        return (GridBagConstraints) g.clone();
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private JTextField createTextField(String placeholder, int width) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(width, 40));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private JPasswordField createPasswordField(String placeholder, int width) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(width, 40));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private JButton createPrimaryButton(String text, int width) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(width, 44));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 0, true),
                new EmptyBorder(12, 20, 12, 20)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(67, 56, 202));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY);
            }
        });

        return btn;
    }

    private JButton createSecondaryButton(String text, int width) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(width, 44));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(12, 20, 12, 20)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(248, 250, 252));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    private void clearRegisterFields() {
        txtRegFullName.setText("");
        cboRegGender.setSelectedIndex(0);
        txtRegEmail.setText("");
        txtRegPhone.setText("");
        txtRegAddress.setText("");
        txtRegUsername.setText("");
        txtRegPassword.setText("");
        txtRegConfirmPass.setText("");
    }

    private void handleRegistration() {
        try {
            String fullName = txtRegFullName.getText().trim();
            String genderStr = (String) cboRegGender.getSelectedItem();
            String email = txtRegEmail.getText().trim();
            String phone = txtRegPhone.getText().trim();
            String address = txtRegAddress.getText().trim();
            String username = txtRegUsername.getText().trim();
            String password = String.valueOf(txtRegPassword.getPassword());
            String confirmPassword = String.valueOf(txtRegConfirmPass.getPassword());

            // Validation
            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                    username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showError("Please fill in all required fields (marked with *)");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showError("Please enter a valid email address");
                return;
            }

            if (!phone.matches("^[0-9]{10,11}$")) {
                showError("Phone number must be 10-11 digits");
                return;
            }

            if (username.length() < 4) {
                showError("Username must be at least 4 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match");
                return;
            }

            // Parse gender
            com.languagecenter.model.enums.Gender gender = com.languagecenter.model.enums.Gender.valueOf(genderStr);

            // Create Student
            Student student = new Student();
            student.setFullName(fullName);
            student.setGender(gender);
            student.setEmail(email);
            student.setPhone(phone);
            student.setAddress(address.isEmpty() ? null : address);
            student.setRegistrationDate(LocalDate.now());
            student.setStatus(com.languagecenter.model.enums.StudentStatus.Active);

            // Save to database
            studentService.create(student, username, password);

            // Success message
            JOptionPane.showMessageDialog(
                    this,
                    "<html><div style='width:280px;padding:10px;text-align:center;'>" +
                            "<b style='font-size:16px;color:#22c55e;'>Registration Successful!</b><br/><br/>" +
                            "Your account has been created.<br/>" +
                            "You can now login with your credentials." +
                            "</div></html>",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearRegisterFields();
            cardLayout.show(cardPanel, "LOGIN");

        } catch (Exception ex) {
            showError("Registration failed: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                "<html><div style='width:280px;padding:10px;'>" +
                        "<b style='color:#ef4444;'>Error</b><br/><br/>" +
                        message +
                        "</div></html>",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    private void login() {
        try {
            String user = txtLoginUser.getText().trim();
            String pass = new String(txtLoginPass.getPassword());

            UserAccount acc = authService.login(user, pass);
            dispose();

            if (acc.getRole() == UserRole.Admin) {
                new MainFrame(acc, authService, studentService, teacherService, courseService,
                        roomService, classService, scheduleService, enrollmentService,
                        invoiceService, paymentService, attendanceService, resultService).setVisible(true);
            } else if (acc.getRole() == UserRole.Teacher) {
                new TeacherMainFrame(acc, authService, studentService, teacherService, courseService,
                        roomService, classService, scheduleService, enrollmentService,
                        invoiceService, paymentService, attendanceService, resultService).setVisible(true);
            } else if (acc.getRole() == UserRole.Student) {
                new StudentMainFrame(acc, authService, studentService, teacherService, courseService,
                        roomService, classService, scheduleService, enrollmentService,
                        invoiceService, paymentService, attendanceService, resultService).setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}