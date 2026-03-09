package com.languagecenter.ui.student;

import com.languagecenter.model.Invoice;
import com.languagecenter.model.Payment;
import com.languagecenter.model.enums.InvoiceStatus;
import com.languagecenter.model.enums.PaymentMethod;
import com.languagecenter.model.enums.PaymentStatus;
import com.languagecenter.service.InvoiceService;
import com.languagecenter.service.PaymentService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class StudentPaymentPanel extends JPanel {
    private final Long studentId;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    private final JTable invoiceTable = new JTable();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Chọn", "Lớp học", "Tổng tiền", "Đã trả", "Còn lại", "Ngày phát hành", "Trạng thái"}, 0
    ) {
        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // Chỉ cho phép edit cột checkbox (column 0)
            return column == 0;
        }
    };

    private final JLabel lblTotalDebt = new JLabel("0 VNĐ");
    private final JLabel lblPaidAmount = new JLabel("0 VNĐ");
    private final JLabel lblRemainingAmount = new JLabel("0 VNĐ");
    private final JLabel lblSelectedAmount = new JLabel("0 VNĐ");

    private final Map<Integer, Invoice> rowToInvoiceMap = new HashMap<>();
    private List<Invoice> unpaidInvoices = new ArrayList<>();

    public StudentPaymentPanel(Long studentId, InvoiceService invoiceService, PaymentService paymentService) {
        this.studentId = studentId;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;

        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 247, 250));

        buildUI();
        loadData();
    }

    public void reload() {
        loadData();
    }

    private void buildUI() {
        // Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Thanh toán học phí");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(103, 58, 183));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Stats Panel (4 cards)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("Tổng nợ học phí", lblTotalDebt, new Color(231, 76, 60)));
        statsPanel.add(createStatCard("Đã thanh toán", lblPaidAmount, new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Còn phải trả", lblRemainingAmount, new Color(243, 156, 18)));
        statsPanel.add(createStatCard("Đã chọn thanh toán", lblSelectedAmount, new Color(52, 152, 219)));

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Danh sách hóa đơn chưa thanh toán");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTableTitle.setForeground(new Color(44, 62, 80));

        invoiceTable.setModel(tableModel);
        invoiceTable.setRowHeight(35);
        invoiceTable.setFont(new Font("Arial", Font.PLAIN, 13));
        invoiceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        invoiceTable.getTableHeader().setBackground(new Color(103, 58, 183));
        invoiceTable.getTableHeader().setForeground(Color.WHITE);
        invoiceTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        invoiceTable.setGridColor(new Color(230, 230, 230));

        // Center align cells (except checkbox)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < invoiceTable.getColumnCount(); i++) {
            invoiceTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Set column widths
        invoiceTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        invoiceTable.getColumnModel().getColumn(0).setMaxWidth(50);

        // Add table model listener to update selected amount
        tableModel.addTableModelListener(e -> updateSelectedAmount());

        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.setBackground(Color.WHITE);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setOpaque(false);

        JButton btnSelectAll = new JButton("Chọn tất cả");
        btnSelectAll.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSelectAll.setBackground(new Color(52, 152, 219));
        btnSelectAll.setForeground(Color.WHITE);
        btnSelectAll.setFocusPainted(false);
        btnSelectAll.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnSelectAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSelectAll.addActionListener(e -> selectAllInvoices(true));

        JButton btnDeselectAll = new JButton("Bỏ chọn tất cả");
        btnDeselectAll.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDeselectAll.setBackground(new Color(149, 165, 166));
        btnDeselectAll.setForeground(Color.WHITE);
        btnDeselectAll.setFocusPainted(false);
        btnDeselectAll.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnDeselectAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeselectAll.addActionListener(e -> selectAllInvoices(false));

        JButton btnPay = new JButton("Thanh toán các mục đã chọn");
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPay.setBackground(new Color(46, 204, 113));
        btnPay.setForeground(Color.WHITE);
        btnPay.setFocusPainted(false);
        btnPay.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnPay.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPay.addActionListener(e -> handlePayment());

        buttonPanel.add(btnSelectAll);
        buttonPanel.add(btnDeselectAll);
        buttonPanel.add(btnPay);

        tablePanel.add(lblTableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        // Main Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        textPanel.setOpaque(false);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(new Color(127, 140, 141));

        textPanel.add(valueLabel);
        textPanel.add(lblTitle);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private void loadData() {
        try {
            List<Invoice> allInvoices = invoiceService.getByStudentId(studentId);

            // Filter unpaid invoices (not Paid and not Cancelled)
            unpaidInvoices = allInvoices.stream()
                    .filter(inv -> inv.getStatus() != InvoiceStatus.Paid
                                && inv.getStatus() != InvoiceStatus.Cancelled)
                    .toList();

            tableModel.setRowCount(0);
            rowToInvoiceMap.clear();

            double totalDebt = 0;
            double paidAmount = 0;
            double remainingAmount = 0;

            int rowIndex = 0;
            for (Invoice invoice : unpaidInvoices) {
                Double totalPaid = calculatePaidAmountForInvoice(invoice.getId());
                Double remaining = invoice.getTotalAmount() - totalPaid;

                tableModel.addRow(new Object[]{
                        false, // Checkbox default unchecked
                        invoice.getEnrollment().getClassEntity().getClassName(),
                        formatCurrency(invoice.getTotalAmount()),
                        formatCurrency(totalPaid),
                        formatCurrency(remaining),
                        invoice.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        invoice.getStatus().toString()
                });

                rowToInvoiceMap.put(rowIndex, invoice);
                totalDebt += invoice.getTotalAmount();
                paidAmount += totalPaid;
                remainingAmount += remaining;
                rowIndex++;
            }

            lblTotalDebt.setText(formatCurrency(totalDebt));
            lblPaidAmount.setText(formatCurrency(paidAmount));
            lblRemainingAmount.setText(formatCurrency(remainingAmount));
            lblSelectedAmount.setText("0 VNĐ");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải dữ liệu: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private Double calculatePaidAmountForInvoice(Long invoiceId) {
        try {
            List<Payment> allPayments = paymentService.getAll();
            return allPayments.stream()
                    .filter(p -> p.getInvoice() != null
                              && p.getInvoice().getId().equals(invoiceId)
                              && p.getStatus() == PaymentStatus.Completed)
                    .mapToDouble(Payment::getAmount)
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private void selectAllInvoices(boolean select) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(select, i, 0);
        }
    }

    private void updateSelectedAmount() {
        double selectedAmount = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                Invoice invoice = rowToInvoiceMap.get(i);
                if (invoice != null) {
                    Double totalPaid = calculatePaidAmountForInvoice(invoice.getId());
                    Double remaining = invoice.getTotalAmount() - totalPaid;
                    selectedAmount += remaining;
                }
            }
        }
        lblSelectedAmount.setText(formatCurrency(selectedAmount));
    }

    private void handlePayment() {
        List<Invoice> selectedInvoices = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                selectedInvoices.add(rowToInvoiceMap.get(i));
            }
        }

        if (selectedInvoices.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn ít nhất một hóa đơn để thanh toán!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show payment dialog
        showPaymentDialog(selectedInvoices);
    }

    private void showPaymentDialog(List<Invoice> selectedInvoices) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xác nhận thanh toán", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);

        // Title
        JLabel lblTitle = new JLabel("Chi tiết thanh toán");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(103, 58, 183));
        contentPanel.add(lblTitle, BorderLayout.NORTH);

        // Invoice details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);

        double totalAmount = 0;
        for (Invoice invoice : selectedInvoices) {
            Double totalPaid = calculatePaidAmountForInvoice(invoice.getId());
            Double remaining = invoice.getTotalAmount() - totalPaid;
            totalAmount += remaining;

            JPanel invoiceRow = new JPanel(new GridLayout(1, 2));
            invoiceRow.setBackground(new Color(245, 247, 250));
            invoiceRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    new EmptyBorder(10, 10, 10, 10)
            ));

            JLabel lblClass = new JLabel(invoice.getEnrollment().getClassEntity().getClassName());
            lblClass.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JLabel lblAmount = new JLabel(formatCurrency(remaining));
            lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblAmount.setForeground(new Color(231, 76, 60));
            lblAmount.setHorizontalAlignment(SwingConstants.RIGHT);

            invoiceRow.add(lblClass);
            invoiceRow.add(lblAmount);

            detailsPanel.add(invoiceRow);
            detailsPanel.add(Box.createVerticalStrut(10));
        }

        // Total amount
        JPanel totalPanel = new JPanel(new GridLayout(1, 2));
        totalPanel.setBackground(new Color(103, 58, 183));
        totalPanel.setBorder(new EmptyBorder(15, 10, 15, 10));

        JLabel lblTotalLabel = new JLabel("Tổng cộng:");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalLabel.setForeground(Color.WHITE);

        JLabel lblTotalValue = new JLabel(formatCurrency(totalAmount));
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalValue.setForeground(Color.WHITE);
        lblTotalValue.setHorizontalAlignment(SwingConstants.RIGHT);

        totalPanel.add(lblTotalLabel);
        totalPanel.add(lblTotalValue);

        detailsPanel.add(totalPanel);

        // Payment method selection
        JPanel methodPanel = new JPanel(new GridLayout(3, 1, 5, 10));
        methodPanel.setBackground(Color.WHITE);
        methodPanel.setBorder(BorderFactory.createTitledBorder("Chọn phương thức thanh toán"));

        ButtonGroup paymentGroup = new ButtonGroup();
        JRadioButton rbCash = new JRadioButton("Tiền mặt (Cash)");
        JRadioButton rbBank = new JRadioButton("Chuyển khoản ngân hàng (Bank Transfer)");
        JRadioButton rbMomo = new JRadioButton("Ví điện tử Momo");
        JRadioButton rbZaloPay = new JRadioButton("Ví điện tử ZaloPay");
        JRadioButton rbCard = new JRadioButton("Thẻ tín dụng/ghi nợ (Card)");
        JRadioButton rbOther = new JRadioButton("Khác (Other)");

        rbCash.setBackground(Color.WHITE);
        rbBank.setBackground(Color.WHITE);
        rbMomo.setBackground(Color.WHITE);
        rbZaloPay.setBackground(Color.WHITE);
        rbCard.setBackground(Color.WHITE);
        rbOther.setBackground(Color.WHITE);

        paymentGroup.add(rbCash);
        paymentGroup.add(rbBank);
        paymentGroup.add(rbMomo);
        paymentGroup.add(rbZaloPay);
        paymentGroup.add(rbCard);
        paymentGroup.add(rbOther);

        rbCash.setSelected(true);

        methodPanel.add(rbCash);
        methodPanel.add(rbBank);
        methodPanel.add(rbMomo);
        methodPanel.add(rbZaloPay);
        methodPanel.add(rbCard);
        methodPanel.add(rbOther);

        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(methodPanel);

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(null);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnConfirm = new JButton("Xác nhận thanh toán");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setBackground(new Color(46, 204, 113));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));

        double finalTotalAmount = totalAmount;
        btnConfirm.addActionListener(e -> {
            PaymentMethod selectedMethod = PaymentMethod.Cash;
            if (rbBank.isSelected()) selectedMethod = PaymentMethod.Bank;
            else if (rbMomo.isSelected()) selectedMethod = PaymentMethod.Momo;
            else if (rbZaloPay.isSelected()) selectedMethod = PaymentMethod.ZaloPay;
            else if (rbCard.isSelected()) selectedMethod = PaymentMethod.Card;
            else if (rbOther.isSelected()) selectedMethod = PaymentMethod.Other;

            processPayments(selectedInvoices, selectedMethod, finalTotalAmount);
            dialog.dispose();
        });

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setBackground(new Color(231, 76, 60));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private void processPayments(List<Invoice> selectedInvoices, PaymentMethod method, double totalAmount) {
        try {
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMessages = new StringBuilder();
            List<Invoice> successInvoices = new ArrayList<>();

            for (Invoice invoice : selectedInvoices) {
                try {
                    Double totalPaid = calculatePaidAmountForInvoice(invoice.getId());
                    Double remaining = invoice.getTotalAmount() - totalPaid;

                    if (remaining > 0) {
                        Payment payment = new Payment();
                        payment.setStudent(invoice.getStudent());
                        payment.setEnrollment(invoice.getEnrollment());
                        payment.setInvoice(invoice);
                        payment.setAmount(remaining);
                        payment.setPaymentDate(LocalDateTime.now());
                        payment.setPaymentMethod(method);
                        payment.setStatus(PaymentStatus.Completed);
                        payment.setReferenceCode("PAY-" + System.currentTimeMillis() + "-" + invoice.getId());

                        paymentService.create(payment);
                        successCount++;
                        successInvoices.add(invoice);
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessages.append("- ").append(invoice.getEnrollment().getClassEntity().getClassName())
                            .append(": ").append(e.getMessage()).append("\n");
                }
            }

            // Show result and ask for export
            if (failCount == 0) {
                int option = JOptionPane.showOptionDialog(this,
                        String.format("Thanh toán thành công %d hóa đơn!\nTổng số tiền: %s\nPhương thức: %s\n\nBạn có muốn xuất hóa đơn PDF không?",
                                successCount, formatCurrency(totalAmount), method),
                        "Thanh toán thành công",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new Object[]{"Xuất PDF", "Không"},
                        "Xuất PDF");

                if (option == JOptionPane.YES_OPTION) {
                    exportInvoicesPdf(successInvoices);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        String.format("Kết quả thanh toán:\n- Thành công: %d\n- Thất bại: %d\n\nChi tiết lỗi:\n%s",
                                successCount, failCount, errorMessages.toString()),
                        "Kết quả thanh toán",
                        JOptionPane.WARNING_MESSAGE);

                if (successCount > 0) {
                    int option = JOptionPane.showConfirmDialog(this,
                            "Có " + successCount + " hóa đơn đã thanh toán thành công.\nBạn có muốn xuất hóa đơn PDF không?",
                            "Xuất hóa đơn",
                            JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        exportInvoicesPdf(successInvoices);
                    }
                }
            }

            // Reload data
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi xử lý thanh toán: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportInvoicesPdf(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return;
        }

        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu hóa đơn PDF");

            if (invoices.size() == 1) {
                fileChooser.setSelectedFile(new File("Invoice_" + invoices.get(0).getId() + ".pdf"));
            } else {
                fileChooser.setSelectedFile(new File("Invoices_" + System.currentTimeMillis() + ".pdf"));
            }

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                }

                // Export all invoices
                for (Invoice invoice : invoices) {
                    File invoiceFile;
                    if (invoices.size() == 1) {
                        invoiceFile = fileToSave;
                    } else {
                        String dir = fileToSave.getParent();
                        String name = fileToSave.getName().replace(".pdf", "");
                        invoiceFile = new File(dir + File.separator + name + "_Invoice_" + invoice.getId() + ".pdf");
                    }

                    exportInvoiceWithJasper(invoice, invoiceFile);
                }

                JOptionPane.showMessageDialog(this,
                        "Xuất hóa đơn PDF thành công!\nĐường dẫn: " + fileToSave.getParent(),
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi xuất hóa đơn PDF: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportInvoiceWithJasper(Invoice invoice, File outputFile) throws Exception {
        try {
            // Load the JRXML template from resources
            InputStream reportStream = getClass().getResourceAsStream("/reports/invoice_template.jrxml");

            if (reportStream == null) {
                throw new Exception("Không tìm thấy template hóa đơn (invoice_template.jrxml)");
            }

            // Compile the report
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Prepare parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("INVOICE_ID", invoice.getId());
            parameters.put("ISSUE_DATE", invoice.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            parameters.put("STUDENT_NAME", invoice.getStudent().getFullName());
            parameters.put("STUDENT_EMAIL", invoice.getStudent().getEmail());
            parameters.put("STUDENT_PHONE", invoice.getStudent().getPhone());
            parameters.put("CLASS_NAME", invoice.getEnrollment().getClassEntity().getClassName());
            parameters.put("COURSE_NAME", invoice.getEnrollment().getClassEntity().getCourse() != null ?
                    invoice.getEnrollment().getClassEntity().getCourse().getCourseName() : "N/A");
            parameters.put("TEACHER_NAME", invoice.getEnrollment().getClassEntity().getTeacher() != null ?
                    invoice.getEnrollment().getClassEntity().getTeacher().getFullName() : "N/A");

            Double totalPaid = calculatePaidAmountForInvoice(invoice.getId());
            Double remaining = invoice.getTotalAmount() - totalPaid;

            parameters.put("TOTAL_AMOUNT", formatCurrency(invoice.getTotalAmount()));
            parameters.put("PAID_AMOUNT", formatCurrency(totalPaid));
            parameters.put("REMAINING_AMOUNT", formatCurrency(remaining));
            parameters.put("STATUS", invoice.getStatus().toString());
            parameters.put("NOTE", invoice.getNote() != null ? invoice.getNote() : "");

            // Get payment history
            List<Payment> invoicePayments = paymentService.getAll().stream()
                    .filter(p -> p.getInvoice() != null && p.getInvoice().getId().equals(invoice.getId()))
                    .toList();

            // Create data source for payments
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                    invoicePayments.isEmpty() ? Collections.singletonList(new Object()) : invoicePayments
            );

            // Fill the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export to PDF
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Lỗi khi tạo PDF: " + e.getMessage());
        }
    }

    private String formatCurrency(Double amount) {
        return String.format("%,.0f VNĐ", amount);
    }
}
