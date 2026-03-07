package com.languagecenter.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.UserAccount;
import com.languagecenter.service.*;
import com.languagecenter.ui.teacher.TeacherProfilePage;
import javax.swing.*;
import java.awt.*;

public class TeacherMainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    // Lưu trữ các service để truyền lại cho LoginFrame khi Logout
    private final AuthService authService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;
    private final RoomService roomService;
    private final ClassService classService;
    private final ScheduleService scheduleService;
    private final EnrollmentService enrollmentService;

    public TeacherMainFrame(UserAccount acc, AuthService as, StudentService ss,
                            TeacherService ts, CourseService cs, RoomService rs
            , ClassService cls, ScheduleService scheduleService, EnrollmentService enrollmentService) {
        super("Teacher Portal - " + acc.getTeacher().getFullName());
        this.authService = as;
        this.studentService = ss;
        this.teacherService = ts;
        this.courseService = cs;
        this.roomService = rs;
        this.classService = cls;
        this.scheduleService = scheduleService;
        this.enrollmentService = enrollmentService;

        setExtendedState(JFrame.MAXIMIZED_BOTH); // Chạy toàn màn hình
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // TOP BAR
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setPreferredSize(new Dimension(0, 60));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JLabel lblInfo = new JLabel("  GIẢNG VIÊN: " + acc.getTeacher().getFullName());
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblInfo.setForeground(new Color(30, 130, 76));
        topBar.add(lblInfo, BorderLayout.WEST);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "buttonType:borderless; foreground:#e74c3c; font:bold");
        btnLogout.addActionListener(e -> handleLogout());
        topBar.add(btnLogout, BorderLayout.EAST);

        // SIDEBAR
        JPanel sidebar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(new Color(30, 130, 76));

        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createMenuBtn("Lịch dạy của tôi", "DASH"));
        sidebar.add(createMenuBtn("Hồ sơ cá nhân", "PROFILE"));

        // CONTENT
        contentPanel.add(new JLabel("Welcome Dashboard Teacher", SwingConstants.CENTER), "DASH");
        // Truyền Teacher object lấy từ UserAccount đã đăng nhập
        contentPanel.add(new TeacherProfilePage(acc.getTeacher(), acc.getUsername(), ts), "PROFILE");

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createMenuBtn(String text, String card) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(210, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.putClientProperty(FlatClientProperties.STYLE, "buttonType:borderless; foreground:#ffffff; arc:0; focusedBackground:#27ae60");
        btn.addActionListener(e -> cardLayout.show(contentPanel, card));
        return btn;
    }

    private void handleLogout() {
        if (JOptionPane.showConfirmDialog(this, "Bạn có muốn đăng xuất không?", "Logout", JOptionPane.YES_NO_OPTION) == 0) {
            this.dispose();
            // Quay lại LoginFrame với đầy đủ services
            new LoginFrame(authService, studentService, teacherService, courseService, roomService, classService, scheduleService,enrollmentService).setVisible(true);
        }
    }
}