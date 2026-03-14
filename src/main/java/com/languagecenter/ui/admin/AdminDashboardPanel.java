package com.languagecenter.ui.admin;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.Student;
import com.languagecenter.model.Teacher;
import com.languagecenter.model.Class;
import com.languagecenter.model.enums.*;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboardPanel extends JPanel {
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public AdminDashboardPanel(StudentService studentService, TeacherService teacherService,
                               ClassService classService, EnrollmentService enrollmentService,
                               InvoiceService invoiceService, PaymentService paymentService) {
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.classService = classService;
        this.enrollmentService = enrollmentService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;

        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));
        setBackground(new Color(245, 247, 250));

        buildUI();
    }

    public void reload() {
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setOpaque(false);

        // Header Section
        JPanel headerPanel = createHeaderPanel();

        // Stats Panel - 2 rows
        JPanel statsPanel = createStatsPanel();

        // Data Panel (Tabs for Students, Teachers, Classes)
        JPanel dataPanel = createDataPanel();

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerContent = new JPanel(new BorderLayout(0, 15));
        centerContent.setOpaque(false);
        centerContent.add(statsPanel, BorderLayout.NORTH);
        centerContent.add(dataPanel, BorderLayout.CENTER);

        mainPanel.add(centerContent, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel("📊 Admin Dashboard - System Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(103, 58, 183));

        JLabel lblSubtitle = new JLabel("Track center statistics and manage resources");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(127, 140, 141));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(lblSubtitle);

        panel.add(textPanel, BorderLayout.WEST);

        JButton btnRefresh = new JButton("🔄 Refresh Data");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE,
                "background:#673ab7; foreground:#ffffff; arc:8; borderWidth:0");
        btnRefresh.addActionListener(e -> reload());

        JPanel refreshOuter = new JPanel(new BorderLayout());
        refreshOuter.setOpaque(false);
        refreshOuter.add(btnRefresh, BorderLayout.SOUTH);
        panel.add(refreshOuter, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel container = new JPanel(new GridLayout(2, 3, 15, 15));
        container.setOpaque(false);

        long totalStudents = 0, totalTeachers = 0, totalClasses = 0, ongoingClasses = 0, totalEnrollments = 0, totalInvoices = 0;
        try {
            totalStudents = studentService.getAll().stream()
                    .filter(s -> s.getStatus() == StudentStatus.Active).count();
            totalTeachers = teacherService.getAll().stream()
                    .filter(t -> t.getStatus() == TeacherStatus.Active).count();
            totalClasses = classService.getAll().size();
            ongoingClasses = classService.getAll().stream()
                    .filter(c -> c.getStatus() == ClassStatus.Ongoing).count();
            totalEnrollments = enrollmentService.getAll().size();
            totalInvoices = invoiceService.getAll().size();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel lblStudents = new JLabel(String.valueOf(totalStudents));
        JLabel lblTeachers = new JLabel(String.valueOf(totalTeachers));
        JLabel lblClasses = new JLabel(String.valueOf(totalClasses));
        JLabel lblOngoingClasses = new JLabel(String.valueOf(ongoingClasses));
        JLabel lblEnrollments = new JLabel(String.valueOf(totalEnrollments));
        JLabel lblInvoices = new JLabel(String.valueOf(totalInvoices));

        // Row 1
        container.add(createModernStatCard("Active Students", lblStudents,
                new Color(52, 152, 219), "👨‍🎓", "Currently enrolled students"));
        container.add(createModernStatCard("Active Teachers", lblTeachers,
                new Color(46, 204, 113), "👨‍🏫", "Currently employed teachers"));
        container.add(createModernStatCard("Total Classes", lblClasses,
                new Color(155, 89, 182), "📚", "All classes in history"));

        // Row 2
        container.add(createModernStatCard("Ongoing Classes", lblOngoingClasses,
                new Color(230, 126, 34), "🎯", "Classes active right now"));
        container.add(createModernStatCard("Total Enrollments", lblEnrollments,
                new Color(26, 188, 156), "✅", "Total course registrations"));
        container.add(createModernStatCard("Total Invoices", lblInvoices,
                new Color(52, 73, 94), "💰", "Financial records generated"));

        return container;
    }

    private JPanel createModernStatCard(String title, JLabel valueLabel, Color color,
                                        String icon, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                new EmptyBorder(18, 20, 18, 20)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(60, 60));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(70, 70, 70));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitle.setForeground(new Color(150, 150, 150));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblSubtitle);

        card.add(lblIcon, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDataPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setFocusable(false);

        tabbedPane.addTab("Recent Students", createStudentsTable());
        tabbedPane.addTab("Recent Teachers", createTeachersTable());
        tabbedPane.addTab("Active Classes", createClassesTable());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStudentsTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"#", "ID", "Full Name", "Email", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            List<Student> students = studentService.getAll();
            int i = 1;
            for (Student s : (students.size() > 15 ? students.subList(0, 15) : students)) {
                model.addRow(new Object[]{i++, s.getId(), s.getFullName(), s.getEmail(), s.getStatus()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JTable table = createStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(120);

        return wrapTableInPanel(table);
    }

    private JPanel createTeachersTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"#", "ID", "Full Name", "Email", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            List<Teacher> teachers = teacherService.getAll();
            int i = 1;
            for (Teacher t : (teachers.size() > 15 ? teachers.subList(0, 15) : teachers)) {
                model.addRow(new Object[]{i++, t.getId(), t.getFullName(), t.getEmail(), t.getStatus()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JTable table = createStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(120);

        return wrapTableInPanel(table);
    }

    private JPanel createClassesTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"#", "ID", "Class Name", "Course", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            List<Class> classes = classService.getAll();
            int i = 1;
            for (Class c : (classes.size() > 15 ? classes.subList(0, 15) : classes)) {
                model.addRow(new Object[]{i++, c.getId(), c.getClassName(),
                        c.getCourse() != null ? c.getCourse().getCourseName() : "N/A", c.getStatus()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JTable table = createStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setMaxWidth(150);

        return wrapTableInPanel(table);
    }

    private JPanel wrapTableInPanel(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(237, 231, 246));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(103, 58, 183));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Apply center alignment to # and ID columns
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        }
        
        return table;
    }
}
