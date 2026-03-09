package com.languagecenter.ui.admin;

import com.languagecenter.model.enums.*;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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
        setBorder(new EmptyBorder(20, 25, 20, 25));
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
        // Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Admin Dashboard - System Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        statsPanel.setOpaque(false);

        try {
            long totalStudents = studentService.getAll().stream()
                    .filter(s -> s.getStatus() == StudentStatus.Active).count();
            long totalTeachers = teacherService.getAll().stream()
                    .filter(t -> t.getStatus() == TeacherStatus.Active).count();
            long totalClasses = classService.getAll().size();
            long ongoingClasses = classService.getAll().stream()
                    .filter(c -> c.getStatus() == ClassStatus.Ongoing).count();
            long totalEnrollments = enrollmentService.getAll().size();
            long totalInvoices = invoiceService.getAll().size();

            statsPanel.add(createStatCard("Total Active Students", String.valueOf(totalStudents),
                new Color(52, 152, 219), "👨‍🎓"));
            statsPanel.add(createStatCard("Total Active Teachers", String.valueOf(totalTeachers),
                new Color(46, 204, 113), "👨‍🏫"));
            statsPanel.add(createStatCard("Total Classes", String.valueOf(totalClasses),
                new Color(155, 89, 182), "📚"));
            statsPanel.add(createStatCard("Ongoing Classes", String.valueOf(ongoingClasses),
                new Color(230, 126, 34), "🎯"));
            statsPanel.add(createStatCard("Total Enrollments", String.valueOf(totalEnrollments),
                new Color(26, 188, 156), "✅"));
            statsPanel.add(createStatCard("Total Invoices", String.valueOf(totalInvoices),
                new Color(52, 73, 94), "💰"));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading statistics: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        add(headerPanel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setOpaque(false);

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 36));
        lblValue.setForeground(color);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(127, 140, 141));

        textPanel.add(lblValue);
        textPanel.add(lblTitle);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }
}
