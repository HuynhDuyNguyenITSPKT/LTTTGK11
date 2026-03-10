package com.languagecenter.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.UserAccount;
import com.languagecenter.service.*;
import com.languagecenter.ui.clas.ClassPanel;
import com.languagecenter.ui.component.CustomHeader;
import com.languagecenter.ui.enrollment.EnrollmentPanel;
import com.languagecenter.ui.schedule.SchedulePanel;
import com.languagecenter.ui.student.StudentPanel;
import com.languagecenter.ui.teacher.TeacherPanel;
import com.languagecenter.ui.course.CoursePanel;
import com.languagecenter.ui.room.RoomPanel;
import com.languagecenter.ui.invoice.InvoicePanel;
import com.languagecenter.ui.payment.PaymentPanel;
import com.languagecenter.ui.admin.AdminDashboardPanel;
import com.languagecenter.ui.result.ResultPanel;
import com.languagecenter.ui.admin.AdminAttendancePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class MainFrame extends JFrame {
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
    private final ResultService resultService;

    private AdminDashboardPanel dashboardPanel;
    private StudentPanel studentPanel;
    private TeacherPanel teacherPanel;
    private CoursePanel coursePanel;
    private RoomPanel roomPanel;
    private ClassPanel classPanel;
    private SchedulePanel schedulePanel;
    private EnrollmentPanel enrollmentPanel;
    private InvoicePanel invoicePanel;
    private PaymentPanel paymentPanel;
    private ResultPanel resultPanel;
    private AdminAttendancePanel attendancePanel;

    public MainFrame(UserAccount acc, AuthService as, StudentService ss, TeacherService ts,
                     CourseService cs, RoomService rs, ClassService cls,
                     ScheduleService sche, EnrollmentService es, InvoiceService is, PaymentService ps,
                     AttendanceService attendanceService, ResultService resultService) {
        super("Language Center Management - Admin Dashboard");

        this.authService = as;
        this.studentService = ss;
        this.teacherService = ts;
        this.courseService = cs;
        this.roomService = rs;
        this.classService = cls;
        this.scheduleService = sche;
        this.enrollmentService = es;
        this.invoiceService = is;
        this.paymentService = ps;
        this.attendanceService = attendanceService;
        this.resultService = resultService;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. TOP BAR (Header) ---
        CustomHeader topBar = new CustomHeader(
            "ADMIN DASHBOARD", 
            acc.getUsername(), 
            new Color(44, 62, 80), 
            e -> handleLogout()
        );
        add(topBar, BorderLayout.NORTH);

        // --- 2. SIDEBAR ---
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setLayout(new BorderLayout());

        // Header chứa Logo và Chữ (Sử dụng BoxLayout để xếp chồng dọc)
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(30, 0, 30, 0));

        // 2a. Logo Image
        try {
            java.net.URL imgURL = getClass().getResource("/images/logo.png");
            if (imgURL != null) {
                ImageIcon logoIcon = new ImageIcon(imgURL);
                Image scaledLogo = logoIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                JLabel lblLogoImg = new JLabel(new ImageIcon(scaledLogo));
                lblLogoImg.setAlignmentX(Component.CENTER_ALIGNMENT);
                brandPanel.add(lblLogoImg);
            }
        } catch (Exception e) {
            System.err.println("Logo not found: " + e.getMessage());
        }

        // Khoảng cách giữa Logo và Chữ
        brandPanel.add(Box.createVerticalStrut(15));

        // 2b. Logo Text
        JLabel lblLogoText = new JLabel("ENGLISH CENTER");
        lblLogoText.setForeground(new Color(52, 152, 219));
        lblLogoText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogoText.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(lblLogoText);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Scrollable menu buttons panel
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(new Color(44, 62, 80));
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        // Menu Buttons
        menuPanel.add(createMenuButton("Dashboard", "DASH"));
        menuPanel.add(createMenuButton("Students", "STUDENT"));
        menuPanel.add(createMenuButton("Teachers", "TEACHER"));
        menuPanel.add(createMenuButton("Courses", "COURSE"));
        menuPanel.add(createMenuButton("Rooms", "ROOM"));
        menuPanel.add(createMenuButton("Classes", "CLASS"));
        menuPanel.add(createMenuButton("Enrollments", "ENROLL"));
        menuPanel.add(createMenuButton("Schedules", "SCHEDULE"));
        menuPanel.add(createMenuButton("Invoices", "INVOICE"));
        menuPanel.add(createMenuButton("Payments", "PAY"));
        menuPanel.add(createMenuButton("Results",  "RESULT"));
        menuPanel.add(createMenuButton("Attendance", "ATTEND"));

        JScrollPane menuScroll = new JScrollPane(menuPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setBorder(null);
        menuScroll.getViewport().setBackground(new Color(44, 62, 80));
        sidebar.add(menuScroll, BorderLayout.CENTER);

        // --- 3. CONTENT PANEL ---
        dashboardPanel = new AdminDashboardPanel(ss, ts, cls, es, is, ps);
        studentPanel = new StudentPanel(ss);
        teacherPanel = new TeacherPanel(ts);
        coursePanel = new CoursePanel(cs);
        roomPanel = new RoomPanel(rs);
        classPanel = new ClassPanel(cls, cs, ts, rs);
        schedulePanel = new SchedulePanel(sche, cls, rs);
        enrollmentPanel = new EnrollmentPanel(es, ss, cls);
        invoicePanel   = new InvoicePanel(is, ss);
        paymentPanel   = new PaymentPanel(ps, ss, is);
        resultPanel    = new ResultPanel(resultService, ss, cls);
        attendancePanel = new AdminAttendancePanel(attendanceService, cls, sche);

        contentPanel.add(dashboardPanel, "DASH");
        contentPanel.add(studentPanel, "STUDENT");
        contentPanel.add(teacherPanel, "TEACHER");
        contentPanel.add(coursePanel, "COURSE");
        contentPanel.add(roomPanel, "ROOM");
        contentPanel.add(classPanel, "CLASS");
        contentPanel.add(schedulePanel, "SCHEDULE");
        contentPanel.add(enrollmentPanel, "ENROLL");
        contentPanel.add(invoicePanel,    "INVOICE");
        contentPanel.add(paymentPanel,    "PAY");
        contentPanel.add(resultPanel,     "RESULT");
        contentPanel.add(attendancePanel, "ATTEND");

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setSize(1300, 850);
        setLocationRelativeTo(null);
    }

    private JButton createMenuButton(String text, String cardName) {
        JButton btn = new JButton("   " + text);
        btn.setPreferredSize(new Dimension(240, 50));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "buttonType:borderless; " +
                        "foreground:#ecf0f1; " +
                        "focusedBackground:#34495e; " +
                        "hoverBackground:#34495e; " +
                        "arc:0");
        btn.addActionListener(e -> {
            reloadPanel(cardName);
            cardLayout.show(contentPanel, cardName);
        });
        return btn;
    }

    private void reloadPanel(String cardName) {
        switch (cardName) {
            case "DASH"     -> { if (dashboardPanel  != null) dashboardPanel.reload(); }
            case "STUDENT"  -> { if (studentPanel    != null) studentPanel.reload(); }
            case "TEACHER"  -> { if (teacherPanel    != null) teacherPanel.reload(); }
            case "COURSE"   -> { if (coursePanel     != null) coursePanel.reload(); }
            case "ROOM"     -> { if (roomPanel       != null) roomPanel.reload(); }
            case "CLASS"    -> { if (classPanel      != null) classPanel.reload(); }
            case "SCHEDULE" -> { if (schedulePanel   != null) schedulePanel.reload(); }
            case "ENROLL"   -> { if (enrollmentPanel != null) enrollmentPanel.reload(); }
            case "INVOICE"  -> { if (invoicePanel    != null) invoicePanel.reload(); }
            case "PAY"      -> { if (paymentPanel    != null) paymentPanel.reload(); }
            case "RESULT"   -> { if (resultPanel     != null) resultPanel.reload();  }
            case "ATTEND"   -> { if (attendancePanel != null) attendancePanel.reload(); }
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            // Quay lại màn hình đăng nhập
            new LoginFrame(authService, studentService, teacherService, courseService,
                    roomService, classService, scheduleService, enrollmentService,
                    invoiceService, paymentService, attendanceService, resultService).setVisible(true);
        }
    }
}