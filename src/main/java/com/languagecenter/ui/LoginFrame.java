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
    private final RoomService roomService;
    private final ClassService classService;

    public LoginFrame(AuthService authService, StudentService studentService,
                      TeacherService teacherService, CourseService courseService, RoomService roomService,ClassService classService) {
        super("System Login");
        this.authService = authService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.courseService = courseService;
        this.roomService = roomService;
        this.classService = classService;

        buildUI();

        setSize(850, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildUI() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);

        // --- BÊN TRÁI: LOGO/IMAGE ---
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(400, 0));
        imagePanel.setBackground(new Color(41, 128, 185));

        JLabel lblImage = new JLabel();
        java.net.URL imgURL = getClass().getResource("/images/logo.png");
        if (imgURL != null) {
            // Scale ảnh để vừa khít vùng chứa
            ImageIcon icon = new ImageIcon(new ImageIcon(imgURL).getImage()
                    .getScaledInstance(300, 300, Image.SCALE_SMOOTH));
            lblImage.setIcon(icon);
            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            lblImage.setText("<html><div style='color:white; text-align:center;'><h2>ENGLISH CENTER</h2><br>Management System</div></html>");
            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        }
        imagePanel.add(lblImage, BorderLayout.CENTER);

        // --- BÊN PHẢI: FORM ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 50, 0, 50));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;

        JLabel lblWelcome = new JLabel("Welcome Back!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblWelcome.setForeground(new Color(44, 62, 80));
        g.gridy = 0; g.insets = new Insets(0, 0, 30, 0);
        formPanel.add(lblWelcome, g);

        Font inputFont = new Font("Segoe UI", Font.PLAIN, 16);
        txtUser.setFont(inputFont);
        txtUser.setPreferredSize(new Dimension(300, 45));
        txtUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Username");

        txtPass.setFont(inputFont);
        txtPass.setPreferredSize(new Dimension(300, 45));
        txtPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        txtPass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");

        g.insets = new Insets(0, 0, 15, 0);
        g.gridy = 1; formPanel.add(txtUser, g);
        g.gridy = 2; formPanel.add(txtPass, g);

        JButton btnLogin = new JButton("LOGIN");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setPreferredSize(new Dimension(0, 50));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());

            UserAccount acc = authService.login(user, pass);
            dispose();

            if (acc.getRole() == UserRole.Admin) {
                new MainFrame(studentService, teacherService, courseService, roomService, classService).setVisible(true);
            } else if (acc.getRole() == UserRole.Teacher) {
                // Cần tạo TeacherMainFrame tương tự StudentMainFrame
                new TeacherMainFrame(acc, authService, studentService, teacherService, courseService, roomService, classService).setVisible(true);
            } else if (acc.getRole() == UserRole.Student) {
                new StudentMainFrame(acc, authService, studentService, teacherService, courseService, roomService, classService).setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        }
    }
}