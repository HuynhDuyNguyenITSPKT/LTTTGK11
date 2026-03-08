package com.languagecenter.ui.student;

import com.languagecenter.model.Invoice;
import com.languagecenter.model.Payment;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentInvoicePaymentPanel extends JPanel {
    private final Long studentId;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    private final JTable invoiceTable = new JTable();
    private final DefaultTableModel invoiceTableModel = new DefaultTableModel(
            new String[]{"Invoice ID", "Enrollment", "Total Amount", "Issue Date", "Status", "Note"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable paymentTable = new JTable();
    private final DefaultTableModel paymentTableModel = new DefaultTableModel(
            new String[]{"Payment ID", "Invoice ID", "Amount", "Payment Date", "Method", "Status", "Reference"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JLabel lblTotalAmount = new JLabel("0.00");
    private final JLabel lblPaidAmount = new JLabel("0.00");
    private final JLabel lblPendingAmount = new JLabel("0.00");

    public StudentInvoicePaymentPanel(Long studentId, InvoiceService invoiceService,
                                     PaymentService paymentService) {
        this.studentId = studentId;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;

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
        JLabel lblTitle = new JLabel("My Invoices & Payments");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(103, 58, 183));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("Total Amount", lblTotalAmount, new Color(52, 152, 219), "💰"));
        statsPanel.add(createStatCard("Paid Amount", lblPaidAmount, new Color(46, 204, 113), "✅"));
        statsPanel.add(createStatCard("Pending Amount", lblPendingAmount, new Color(231, 76, 60), "⏳"));

        // Split Pane for Invoices and Payments
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);

        // Invoices Panel
        JPanel invoicePanel = new JPanel(new BorderLayout(0, 10));
        invoicePanel.setBackground(Color.WHITE);
        invoicePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Invoices"),
                new EmptyBorder(10, 10, 10, 10)
        ));

        invoiceTable.setModel(invoiceTableModel);
        invoiceTable.setRowHeight(30);
        invoiceTable.setFont(new Font("Arial", Font.PLAIN, 13));
        invoiceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center align all cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < invoiceTable.getColumnCount(); i++) {
            invoiceTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Add selection listener to show payments for selected invoice
        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && invoiceTable.getSelectedRow() != -1) {
                Long invoiceId = (Long) invoiceTableModel.getValueAt(invoiceTable.getSelectedRow(), 0);
                loadPaymentsForInvoice(invoiceId);
            }
        });

        JScrollPane invoiceScrollPane = new JScrollPane(invoiceTable);
        invoicePanel.add(invoiceScrollPane, BorderLayout.CENTER);

        // Payments Panel
        JPanel paymentPanel = new JPanel(new BorderLayout(0, 10));
        paymentPanel.setBackground(Color.WHITE);
        paymentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Payments (Select an invoice to view payments)"),
                new EmptyBorder(10, 10, 10, 10)
        ));

        paymentTable.setModel(paymentTableModel);
        paymentTable.setRowHeight(30);
        paymentTable.setFont(new Font("Arial", Font.PLAIN, 13));
        paymentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center align all cells
        DefaultTableCellRenderer centerRenderer2 = new DefaultTableCellRenderer();
        centerRenderer2.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < paymentTable.getColumnCount(); i++) {
            paymentTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer2);
        }

        JScrollPane paymentScrollPane = new JScrollPane(paymentTable);
        paymentPanel.add(paymentScrollPane, BorderLayout.CENTER);

        splitPane.setTopComponent(invoicePanel);
        splitPane.setBottomComponent(paymentPanel);

        // Main Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(splitPane, BorderLayout.CENTER);

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

        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
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
            List<Invoice> invoices = invoiceService.getByStudentId(studentId);

            invoiceTableModel.setRowCount(0);
            double totalAmount = 0;
            double paidAmount = 0;

            for (Invoice invoice : invoices) {
                invoiceTableModel.addRow(new Object[]{
                        invoice.getId(),
                        invoice.getEnrollment().getClassEntity().getClassName(),
                        String.format("%.2f", invoice.getTotalAmount()),
                        invoice.getIssueDate(),
                        invoice.getStatus(),
                        invoice.getNote()
                });

                totalAmount += invoice.getTotalAmount();
                if (invoice.getStatus() == com.languagecenter.model.enums.InvoiceStatus.Paid) {
                    paidAmount += invoice.getTotalAmount();
                }
            }

            double pendingAmount = totalAmount - paidAmount;

            lblTotalAmount.setText(String.format("%.2f", totalAmount));
            lblPaidAmount.setText(String.format("%.2f", paidAmount));
            lblPendingAmount.setText(String.format("%.2f", pendingAmount));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading invoices: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPaymentsForInvoice(Long invoiceId) {
        try {
            List<Payment> allPayments = paymentService.getAll();
            List<Payment> invoicePayments = allPayments.stream()
                    .filter(p -> p.getInvoice() != null && p.getInvoice().getId().equals(invoiceId))
                    .toList();

            paymentTableModel.setRowCount(0);

            for (Payment payment : invoicePayments) {
                paymentTableModel.addRow(new Object[]{
                        payment.getId(),
                        payment.getInvoice().getId(),
                        String.format("%.2f", payment.getAmount()),
                        payment.getPaymentDate(),
                        payment.getPaymentMethod(),
                        payment.getStatus(),
                        payment.getReferenceCode()
                });
            }

            if (invoicePayments.isEmpty()) {
                paymentTableModel.addRow(new Object[]{
                        null, null, "No payments found for this invoice", null, null, null, null
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading payments: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
