package com.languagecenter.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.UserAccount;
import com.languagecenter.service.*;
import com.languagecenter.ui.component.CustomHeader;
import com.languagecenter.ui.student.StudentProfilePage;
import com.languagecenter.ui.student.StudentSchedulePanel;
import com.languagecenter.ui.student.StudentCourseRegisterPanel;
import com.languagecenter.ui.student.StudentDashboardPanel;
import com.languagecenter.ui.student.StudentInvoicePaymentPanel;
import com.languagecenter.ui.student.StudentPaymentPanel;
import com.languagecenter.ui.student.StudentInvoiceHistoryPanel;
import com.languagecenter.ui.student.StudentResultPanel;
import com.languagecenter.ui.student.StudentAttendancePanel;

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
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final AttendanceService attendanceService;
    private final com.languagecenter.service.ResultService resultService;
    private StudentDashboardPanel dashboardPanel;
    private StudentSchedulePanel schedulePanel;
    private StudentPaymentPanel paymentPanel;
    private StudentInvoiceHistoryPanel historyPanel;
    private StudentCourseRegisterPanel enrollmentPanel;
    private StudentResultPanel resultPanel;
    private StudentAttendancePanel attendancePanel;

    public StudentMainFrame(UserAccount acc, AuthService as, StudentService ss, TeacherService ts,
                           CourseService cs, RoomService rs, ClassService cls, ScheduleService sche,
                           EnrollmentService enrollmentService, InvoiceService invoiceService,
                           PaymentService paymentService, AttendanceService attendanceService,
                           com.languagecenter.service.ResultService resultService) {
        super("Student Portal - " + acc.getStudent().getFullName());
        this.authService = as;
        this.studentService = ss;
        this.teacherService = ts;
        this.courseService = cs;
        this.roomService = rs;
        this.classService = cls;
        this.scheduleService = sche;
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
            "STUDENT PORTAL", 
            acc.getStudent().getFullName(), 
            new Color(103, 58, 183), 
            e -> handleLogout()
        );
        add(topBar, BorderLayout.NORTH);

        // SIDEBAR
        JPanel sidebar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(new Color(103, 58, 183)); // Màu tím Student

        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createMenuBtn("Dashboard", "DASH"));
        sidebar.add(createMenuBtn("Courses", "COURSES"));
        sidebar.add(createMenuBtn("Tuition Payment", "PAYMENT"));
        sidebar.add(createMenuBtn("Invoice & Payment", "HISTORY"));
        sidebar.add(createMenuBtn("My Results",        "RESULTS"));
        sidebar.add(createMenuBtn("Điểm danh",          "ATTENDANCE"));
        sidebar.add(createMenuBtn("My Profile",        "PROFILE"));
        sidebar.add(createMenuBtn("Enroll Class",      "ENROLLMENT"));

        // CONTENT - Create panel instances
        dashboardPanel  = new StudentDashboardPanel(acc.getStudent().getId(), enrollmentService);
        schedulePanel   = new StudentSchedulePanel(scheduleService, acc.getStudent().getId());
        paymentPanel    = new StudentPaymentPanel(acc.getStudent().getId(), invoiceService, paymentService);
        historyPanel    = new StudentInvoiceHistoryPanel(acc.getStudent().getId(), invoiceService, paymentService);
        enrollmentPanel = new StudentCourseRegisterPanel(acc.getStudent().getId(), courseService, classService, scheduleService, enrollmentService);
        resultPanel     = new StudentResultPanel(acc.getStudent().getId(), resultService);
        attendancePanel = new StudentAttendancePanel(acc.getStudent().getId(), attendanceService);

        contentPanel.add(dashboardPanel,  "DASH");
        contentPanel.add(schedulePanel,   "COURSES");
        contentPanel.add(paymentPanel,    "PAYMENT");
        contentPanel.add(historyPanel,    "HISTORY");
        contentPanel.add(resultPanel,     "RESULTS");
        contentPanel.add(attendancePanel, "ATTENDANCE");
        contentPanel.add(new StudentProfilePage(acc.getStudent(), acc.getUsername(), ss), "PROFILE");
        contentPanel.add(enrollmentPanel, "ENROLLMENT");

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createMenuBtn(String text, String card) {
        JButton btn = new JButton("  " + text);
        btn.setPreferredSize(new Dimension(220, 50));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.putClientProperty(FlatClientProperties.STYLE, "buttonType:borderless; foreground:#ffffff; arc:0; focusedBackground:#9575cd; hoverBackground:#9575cd");
        btn.addActionListener(e -> {
            reloadPanelData(card);
            cardLayout.show(contentPanel, card);
        });
        return btn;
    }

    private void reloadPanelData(String card) {
        switch (card) {
            case "DASH"       -> { if (dashboardPanel  != null) dashboardPanel.reload(); }
            case "COURSES"    -> { if (schedulePanel   != null) schedulePanel.reload(); }
            case "PAYMENT"    -> { if (paymentPanel    != null) paymentPanel.reload(); }
            case "HISTORY"    -> { if (historyPanel    != null) historyPanel.reload(); }
            case "RESULTS"    -> { if (resultPanel     != null) resultPanel.reload(); }
            case "ATTENDANCE" -> { if (attendancePanel != null) attendancePanel.reload(); }
            case "ENROLLMENT" -> { if (enrollmentPanel != null) enrollmentPanel.reload(); }
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