package com.languagecenter.ui;

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

    public LoginFrame(AuthService authService, StudentService studentService, TeacherService teacherService,
                      CourseService courseService, RoomService roomService) {
        super("Language Center Management - Login");

        this.authService = authService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.courseService = courseService;
        this.roomService = roomService;


        // Giữ nguyên giao diện hệ điều hành
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        buildUI();

        // Tăng chiều cao lên một chút để form không bị ép
        setSize(450, 320);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildUI(){
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        /* --- 1. HEADER (LOGO & TITLE IN ENGLISH) --- */
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        java.net.URL imgURL = getClass().getResource("/images/logo.png");
        ImageIcon logoIcon;
        if (imgURL != null) {
            ImageIcon originalIcon = new ImageIcon(imgURL);
            Image img = originalIcon.getImage();
            Image resizedImg = img.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH);
            logoIcon = new ImageIcon(resizedImg);
        } else {
            logoIcon = new ImageIcon();
        }

        JLabel lblLogo = new JLabel(logoIcon);

        // Cập nhật tiêu đề tiếng Anh
        JLabel lblTitle = new JLabel("<html><div style='text-align: center;'><b style='font-size:16px; color:#2980b9;'>Language Center</b><br><span style='font-size:12px; color:#7f8c8d;'>Management System</span></div></html>");

        headerPanel.add(lblLogo, BorderLayout.WEST);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        /* --- 2. FORM (USERNAME & PASSWORD) --- */
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 14);
        Font fontField = new Font("Segoe UI", Font.PLAIN, 14);
        Color textColor = new Color(50, 50, 50);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(fontLabel);
        lblUser.setForeground(textColor);

        txtUser.setFont(fontField);
        txtUser.setForeground(textColor);
        // FIX: Cố định kích thước để ô không bị xẹp
        txtUser.setPreferredSize(new Dimension(200, 35));

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(fontLabel);
        lblPass.setForeground(textColor);

        txtPass.setFont(fontField);
        txtPass.setForeground(textColor);
        // FIX: Cố định kích thước để ô không bị xẹp
        txtPass.setPreferredSize(new Dimension(200, 35));

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0; // Label không giãn
        formPanel.add(lblUser, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1.0; // Text field tự động giãn
        formPanel.add(txtUser, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(lblPass, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.weightx = 1.0;
        formPanel.add(txtPass, gbc);

        /* --- 3. BUTTON PANEL --- */
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(200, 40));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // FIX: Xử lý triệt để lỗi màu nút bấm trên Windows
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setContentAreaFilled(false); // Ép Windows không vẽ nền mặc định
        btnLogin.setOpaque(true);             // Cho phép vẽ màu nền xanh của chúng ta

        btnLogin.addActionListener(e -> login());
        getRootPane().setDefaultButton(btnLogin);
        bottomPanel.add(btnLogin);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void login(){
        try{
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            if (user.trim().isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both Username and Password!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            UserAccount acc = authService.login(user, pass);
            dispose();

            if(acc.getRole() == UserRole.Admin){
                new MainFrame(studentService,teacherService,courseService,roomService).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "UI for this role is not yet implemented.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }

        }catch(Exception ex){
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}