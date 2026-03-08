package com.languagecenter.ui.student;

import com.languagecenter.model.Enrollment;
import com.languagecenter.model.Invoice;
import com.languagecenter.model.enums.InvoiceStatus;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentDashboardPanel extends JPanel {
    private final Long studentId;
    private final EnrollmentService enrollmentService;
    private final InvoiceService invoiceService;

    private final JLabel lblTotalClasses = new JLabel("0");
    private final JLabel lblTotalInvoices = new JLabel("0");
    private final JLabel lblPaidInvoices = new JLabel("0");

    private final JTable tableEnrollments = new JTable();
    private final DefaultTableModel enrollmentTableModel = new DefaultTableModel(
            new String[]{"Class Name", "Course", "Teacher", "Status", "Result"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tableInvoices = new JTable();
    private final DefaultTableModel invoiceTableModel = new DefaultTableModel(
            new String[]{"Invoice ID", "Class", "Amount", "Issue Date", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public StudentDashboardPanel(Long studentId, EnrollmentService enrollmentService,
                                 InvoiceService invoiceService) {
        this.studentId = studentId;
        this.enrollmentService = enrollmentService;
        this.invoiceService = invoiceService;

        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 247, 250));

        buildUI();
        loadData();
    }

    private void buildUI() {
        // Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Student Dashboard - My Learning Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(103, 58, 183));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("My Classes", lblTotalClasses, new Color(52, 152, 219), "📚"));
        statsPanel.add(createStatCard("Total Invoices", lblTotalInvoices, new Color(230, 126, 34), "💰"));
        statsPanel.add(createStatCard("Paid Invoices", lblPaidInvoices, new Color(46, 204, 113), "✅"));

        // Tables Panel
        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        tablesPanel.setOpaque(false);

        // Enrollments Table
        JPanel enrollmentPanel = new JPanel(new BorderLayout(0, 10));
        enrollmentPanel.setOpaque(false);
        JLabel lblEnrollmentTitle = new JLabel("My Enrollments");
        lblEnrollmentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblEnrollmentTitle.setForeground(new Color(44, 62, 80));

        tableEnrollments.setModel(enrollmentTableModel);
        tableEnrollments.setRowHeight(30);
        tableEnrollments.setFont(new Font("Arial", Font.PLAIN, 13));
        tableEnrollments.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        // Center align all cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tableEnrollments.getColumnCount(); i++) {
            tableEnrollments.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollEnrollments = new JScrollPane(tableEnrollments);
        scrollEnrollments.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollEnrollments.setPreferredSize(new Dimension(0, 200));

        enrollmentPanel.add(lblEnrollmentTitle, BorderLayout.NORTH);
        enrollmentPanel.add(scrollEnrollments, BorderLayout.CENTER);

        // Invoices Table
        JPanel invoicePanel = new JPanel(new BorderLayout(0, 10));
        invoicePanel.setOpaque(false);
        JLabel lblInvoiceTitle = new JLabel("My Invoices");
        lblInvoiceTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblInvoiceTitle.setForeground(new Color(44, 62, 80));

        tableInvoices.setModel(invoiceTableModel);
        tableInvoices.setRowHeight(30);
        tableInvoices.setFont(new Font("Arial", Font.PLAIN, 13));
        tableInvoices.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        // Center align all cells
        DefaultTableCellRenderer centerRenderer2 = new DefaultTableCellRenderer();
        centerRenderer2.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tableInvoices.getColumnCount(); i++) {
            tableInvoices.getColumnModel().getColumn(i).setCellRenderer(centerRenderer2);
        }

        JScrollPane scrollInvoices = new JScrollPane(tableInvoices);
        scrollInvoices.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollInvoices.setPreferredSize(new Dimension(0, 200));

        invoicePanel.add(lblInvoiceTitle, BorderLayout.NORTH);
        invoicePanel.add(scrollInvoices, BorderLayout.CENTER);

        tablesPanel.add(enrollmentPanel);
        tablesPanel.add(invoicePanel);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(tablesPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setOpaque(false);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(127, 140, 141));

        textPanel.add(valueLabel);
        textPanel.add(lblTitle);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private void loadData() {
        try {
            // Load Enrollments
            List<Enrollment> enrollments = enrollmentService.getAll().stream()
                    .filter(e -> e.getStudent().getId().equals(studentId))
                    .toList();

            enrollmentTableModel.setRowCount(0);
            for (Enrollment enrollment : enrollments) {
                enrollmentTableModel.addRow(new Object[]{
                        enrollment.getClassEntity().getClassName(),
                        enrollment.getClassEntity().getCourse() != null ?
                                enrollment.getClassEntity().getCourse().getCourseName() : "N/A",
                        enrollment.getClassEntity().getTeacher() != null ?
                                enrollment.getClassEntity().getTeacher().getFullName() : "N/A",
                        enrollment.getStatus(),
                        enrollment.getResult()
                });
            }

            // Load Invoices
            List<Invoice> invoices = invoiceService.getByStudentId(studentId);
            long paidCount = invoices.stream()
                    .filter(i -> i.getStatus() == InvoiceStatus.Paid)
                    .count();

            invoiceTableModel.setRowCount(0);
            for (Invoice invoice : invoices) {
                invoiceTableModel.addRow(new Object[]{
                        invoice.getId(),
                        invoice.getEnrollment().getClassEntity().getClassName(),
                        String.format("%.2f", invoice.getTotalAmount()),
                        invoice.getIssueDate(),
                        invoice.getStatus()
                });
            }

            lblTotalClasses.setText(String.valueOf(enrollments.size()));
            lblTotalInvoices.setText(String.valueOf(invoices.size()));
            lblPaidInvoices.setText(String.valueOf(paidCount));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading dashboard data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
