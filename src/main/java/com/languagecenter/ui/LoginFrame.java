package com.languagecenter.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.UserRole;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private final AuthService authService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;

    public LoginFrame(AuthService authService, StudentService studentService,
                      TeacherService teacherService, CourseService courseService) {
        super("Language Center Management System");
        this.authService = authService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.courseService = courseService;

        buildUI();

        setSize(850, 500); // Tăng chiều rộng để chứa ảnh
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildUI() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);

        // --- BÊN TRÁI: HÌNH ẢNH MINH HỌA ---
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(400, 0));
        imagePanel.setBackground(new Color(41, 128, 185));

        // Tải ảnh từ resource (đảm bảo file tồn tại trong src/main/resources/images/login_bg.png)
        JLabel lblImage = new JLabel();
        java.net.URL imgURL = getClass().getResource("/images/logo.png");
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(400, 500, Image.SCALE_SMOOTH));
            lblImage.setIcon(icon);
        } else {
            lblImage.setText("<html><div style='color:white; text-align:center;'><h2>LANGUAGE CENTER</h2><br>Management System</div></html>");
            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        }
        imagePanel.add(lblImage);

        // --- BÊN PHẢI: FORM ĐĂNG NHẬP ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 50, 0, 50));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;

        JLabel lblWelcome = new JLabel("Welcome Back!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        g.gridy = 0; g.insets = new Insets(0, 0, 30, 0);
        formPanel.add(lblWelcome, g);

        // Styling input fields to
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 16);
        txtUser.setFont(inputFont);
        txtUser.setPreferredSize(new Dimension(300, 45));
        txtUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Username");

        txtPass.setFont(inputFont);
        txtPass.setPreferredSize(new Dimension(300, 45));
        txtPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        txtPass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");

        g.insets = new Insets(0, 0, 20, 0);
        g.gridy = 1; formPanel.add(txtUser, g);
        g.gridy = 2; formPanel.add(txtPass, g);

        JButton btnLogin = new JButton("LOGIN");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setPreferredSize(new Dimension(0, 50));
        btnLogin.putClientProperty(FlatClientProperties.STYLE, "background:#2980b9; foreground:#ffffff");

        g.gridy = 3; g.insets = new Insets(10, 0, 0, 0);
        formPanel.add(btnLogin, g);

        container.add(imagePanel, BorderLayout.WEST);
        container.add(formPanel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> login());
        getRootPane().setDefaultButton(btnLogin);
        setContentPane(container);
    }

    private void login() {
        try {
            UserAccount acc = authService.login(txtUser.getText(), new String(txtPass.getPassword()));
            dispose();
            if (acc.getRole() == UserRole.Admin) {
                new MainFrame(studentService, teacherService, courseService).setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Login Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}