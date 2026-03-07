package com.languagecenter.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.UserAccount;
import com.languagecenter.service.*;
import com.languagecenter.ui.student.StudentProfilePage;
import com.languagecenter.ui.student.StudentSchedulePanel;

import javax.swing.*;
import java.awt.*;

public class StudentMainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final AuthService authService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;
    private final RoomService roomService;
    private final ClassService classService;
    private final ScheduleService scheduleService;
    private final EnrollmentService enrollmentService;

    public StudentMainFrame(UserAccount acc, AuthService as, StudentService ss, TeacherService ts, CourseService cs, RoomService rs, ClassService cls,ScheduleService sche,EnrollmentService enrollmentService) {
        super("Student Portal - " + acc.getStudent().getFullName());
        this.authService = as;
        this.studentService = ss;
        this.teacherService = ts;
        this.courseService = cs;
        this.roomService = rs;
        this.classService = cls;
        this.scheduleService = sche;
        this.enrollmentService = enrollmentService;
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Chạy toàn màn hình
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // TOP BAR
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setPreferredSize(new Dimension(0, 60));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JLabel lblUser = new JLabel("  Chào học viên, " + acc.getStudent().getFullName());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 15));
        topBar.add(lblUser, BorderLayout.WEST);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "buttonType:borderless; foreground:#e74c3c; font:bold");
        btnLogout.addActionListener(e -> handleLogout());
        topBar.add(btnLogout, BorderLayout.EAST);

        // SIDEBAR
        JPanel sidebar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(new Color(103, 58, 183)); // Màu tím Student

        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createMenuBtn("Bảng điều khiển", "DASH"));
        sidebar.add(createMenuBtn("Hồ sơ cá nhân", "PROFILE"));
        sidebar.add(createMenuBtn("Khóa Học", "CSs"));

        // CONTENT
        contentPanel.add(new JLabel("Welcome Dashboard", SwingConstants.CENTER), "DASH");
        contentPanel.add(new StudentProfilePage(acc.getStudent(), acc.getUsername(), ss), "PROFILE");
        contentPanel.add(new StudentSchedulePanel(scheduleService,acc.getId()), "CSs");

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createMenuBtn(String text, String card) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(210, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.putClientProperty(FlatClientProperties.STYLE, "buttonType:borderless; foreground:#ffffff; arc:0; focusedBackground:#9575cd");
        btn.addActionListener(e -> cardLayout.show(contentPanel, card));
        return btn;
    }

    private void handleLogout() {
        if (JOptionPane.showConfirmDialog(this, "Đăng xuất khỏi hệ thống?", "Logout", 0) == 0) {
            this.dispose();
            // Quay lại màn hình Login với đầy đủ các Service ban đầu
            new LoginFrame(authService, studentService, teacherService, courseService, roomService, classService,scheduleService,enrollmentService).setVisible(true);
        }
    }
}