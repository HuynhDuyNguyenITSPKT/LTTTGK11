package com.languagecenter.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.UserAccount;
import com.languagecenter.service.*;
import com.languagecenter.ui.component.CustomHeader;
import com.languagecenter.ui.teacher.TeacherProfilePage;
import com.languagecenter.ui.teacher.TeacherSchedulePanel;
import com.languagecenter.ui.teacher.TeacherDashboardPanel;
import com.languagecenter.ui.teacher.AttendancePanel;
import com.languagecenter.ui.teacher.TeacherResultPanel;

import javax.swing.*;
import java.awt.*;

public class TeacherMainFrame extends JFrame {
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
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final AttendanceService attendanceService;
    private final com.languagecenter.service.ResultService resultService;

    private TeacherDashboardPanel dashboardPanel;
    private TeacherSchedulePanel schedulePanel;
    private AttendancePanel attendancePanel;
    private TeacherResultPanel resultPanel;

    public TeacherMainFrame(UserAccount acc, AuthService as, StudentService ss,
                            TeacherService ts, CourseService cs, RoomService rs,
                            ClassService cls, ScheduleService scheduleService, EnrollmentService enrollmentService,
                            InvoiceService invoiceService, PaymentService paymentService,
                            AttendanceService attendanceService,
                            com.languagecenter.service.ResultService resultService) {
        super("Teacher Portal - " + acc.getTeacher().getFullName());
        this.authService = as;
        this.studentService = ss;
        this.teacherService = ts;
        this.courseService = cs;
        this.roomService = rs;
        this.classService = cls;
        this.scheduleService = scheduleService;
        this.enrollmentService = enrollmentService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.attendanceService = attendanceService;
        this.resultService = resultService;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // TOP BAR
        CustomHeader topBar = new CustomHeader(
            "TEACHER PORTAL", 
            acc.getTeacher().getFullName(), 
            new Color(103, 58, 183), 
            e -> handleLogout()
        );
        add(topBar, BorderLayout.NORTH);

        // SIDEBAR
        JPanel sidebar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(new Color(30, 130, 76));

        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createMenuBtn("Dashboard",  "DASH"));
        sidebar.add(createMenuBtn("My Schedule", "SCHEDULE"));
        sidebar.add(createMenuBtn("Attendance",  "ATTENDANCE"));
        sidebar.add(createMenuBtn("Results",     "RESULT"));
        sidebar.add(createMenuBtn("My Profile",  "PROFILE"));

        // CONTENT
        dashboardPanel  = new TeacherDashboardPanel(acc.getTeacher().getId(), classService, enrollmentService);
        schedulePanel   = new TeacherSchedulePanel(scheduleService, acc.getTeacher().getId(), enrollmentService);
        attendancePanel = new AttendancePanel(acc.getTeacher().getId(), classService, enrollmentService, attendanceService, scheduleService);
        resultPanel     = new TeacherResultPanel(acc.getTeacher().getId(), classService, enrollmentService, resultService);

        contentPanel.add(dashboardPanel, "DASH");
        contentPanel.add(schedulePanel, "SCHEDULE");
        contentPanel.add(attendancePanel, "ATTENDANCE");
        contentPanel.add(resultPanel, "RESULT");
        contentPanel.add(new TeacherProfilePage(acc.getTeacher(), acc.getUsername(), ts), "PROFILE");

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createMenuBtn(String text, String card) {
        JButton btn = new JButton("  " + text);
        btn.setPreferredSize(new Dimension(220, 50));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.putClientProperty(FlatClientProperties.STYLE, "buttonType:borderless; foreground:#ffffff; arc:0; focusedBackground:#27ae60; hoverBackground:#27ae60");
        btn.addActionListener(e -> {
            reloadPanel(card);
            cardLayout.show(contentPanel, card);
        });
        return btn;
    }

    private void reloadPanel(String card) {
        switch (card) {
            case "DASH"       -> { if (dashboardPanel  != null) dashboardPanel.reload(); }
            case "SCHEDULE"   -> { if (schedulePanel   != null) schedulePanel.reload(); }
            case "ATTENDANCE" -> { if (attendancePanel != null) attendancePanel.reload(); }
            case "RESULT"     -> { if (resultPanel     != null) resultPanel.reload(); }
        }
    }

    private void handleLogout() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION) == 0) {
            this.dispose();
            new LoginFrame(authService, studentService, teacherService, courseService,
                    roomService, classService, scheduleService, enrollmentService,
                    invoiceService, paymentService, attendanceService, resultService).setVisible(true);
        }
    }
}