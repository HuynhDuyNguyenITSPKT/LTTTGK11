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

    public MainFrame(UserAccount acc, AuthService as, StudentService ss, TeacherService ts,
                     CourseService cs, RoomService rs, ClassService cls,
                     ScheduleService sche, EnrollmentService es, InvoiceService is, PaymentService ps) {
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
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0)); // FlowLayout chính

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

        sidebar.add(brandPanel);

        // Menu Buttons
        sidebar.add(createMenuButton("Dashboard", "DASH"));
        sidebar.add(createMenuButton("Students", "STUDENT"));
        sidebar.add(createMenuButton("Teachers", "TEACHER"));
        sidebar.add(createMenuButton("Courses", "COURSE"));
        sidebar.add(createMenuButton("Rooms", "ROOM"));
        sidebar.add(createMenuButton("Classes", "CLASS"));
        sidebar.add(createMenuButton("Enrollments", "ENROLL"));
        sidebar.add(createMenuButton("Schedules", "SCHEDULE"));
        sidebar.add(createMenuButton("Invoices", "INVOICE"));
        sidebar.add(createMenuButton("Payments", "PAY"));

        // --- 3. CONTENT PANEL ---
        // Dashboard Home Panel (Thay vì để trống)
        JPanel dashHome = new JPanel(new GridBagLayout());
        dashHome.setBackground(new Color(245, 247, 250));
        JLabel welcomeLabel = new JLabel("Welcome to Admin Management System");
        welcomeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        welcomeLabel.setForeground(Color.GRAY);
        dashHome.add(welcomeLabel);

        contentPanel.add(dashHome, "DASH");
        contentPanel.add(new StudentPanel(ss), "STUDENT");
        contentPanel.add(new TeacherPanel(ts), "TEACHER");
        contentPanel.add(new CoursePanel(cs), "COURSE");
        contentPanel.add(new RoomPanel(rs), "ROOM");
        contentPanel.add(new ClassPanel(cls, cs, ts, rs), "CLASS");
        contentPanel.add(new SchedulePanel(sche, cls, rs), "SCHEDULE");
        contentPanel.add(new EnrollmentPanel(es, ss, cls), "ENROLL");
        contentPanel.add(new InvoicePanel(is, ss), "INVOICE");
        contentPanel.add(new PaymentPanel(ps, ss, is), "PAY");

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setSize(1300, 850);
        setLocationRelativeTo(null);
    }

    private JButton createMenuButton(String text, String cardName) {
        JButton btn = new JButton("   " + text);
        btn.setPreferredSize(new Dimension(220, 48));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Style FlatLaf
        btn.putClientProperty(FlatClientProperties.STYLE,
                "buttonType:borderless; " +
                        "foreground:#ecf0f1; " +
                        "focusedBackground:#34495e; " +
                        "hoverBackground:#34495e; " +
                        "arc:0");

        btn.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        return btn;
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
                    invoiceService, paymentService).setVisible(true);
        }
    }
}