package com.languagecenter.ui.student;

import com.languagecenter.model.Invoice;
import com.languagecenter.model.Payment;
import com.languagecenter.model.enums.PaymentStatus;
import com.languagecenter.service.InvoiceService;
import com.languagecenter.service.PaymentService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class StudentInvoiceHistoryPanel extends JPanel {
    private final Long studentId;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    private final JTable paymentTable = new JTable();
    private final DefaultTableModel paymentTableModel = new DefaultTableModel(
            new String[]{"Chọn", "STT", "Payment ID", "Lớp học", "Số tiền", "Phương thức", "Ngày thanh toán", "Trạng thái"}, 0
    ) {
        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0; // Only checkbox column is editable
        }
    };

    private final JTable invoiceDetailTable = new JTable();
    private final DefaultTableModel invoiceDetailTableModel = new DefaultTableModel(
            new String[]{"Thông tin", "Giá trị"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JLabel lblTotalPayments = new JLabel("0");
    private final JLabel lblTotalPaid = new JLabel("0 VNĐ");
    private final JLabel lblCompletedPayments = new JLabel("0");
    private final JLabel lblPendingPayments = new JLabel("0");

    private final Map<Integer, Payment> rowToPaymentMap = new HashMap<>();
    private List<Payment> allPayments;
    private List<Payment> filteredPayments;
    private JComboBox<String> classFilterCombo;

    public StudentInvoiceHistoryPanel(Long studentId, InvoiceService invoiceService, PaymentService paymentService) {
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
        JLabel lblTitle = new JLabel("Hóa đơn và Thanh toán của tôi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(103, 58, 183));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Stats Panel (4 cards)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("Tổng thanh toán", lblTotalPayments, new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Tổng số tiền", lblTotalPaid, new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Hoàn thành", lblCompletedPayments, new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Đang xử lý", lblPendingPayments, new Color(243, 156, 18)));

        // Split Panel for Payments and Invoice Details (HORIZONTAL - 2 columns)
        // Payment table takes 2/3 of the space (left), Invoice detail takes 1/3 (right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.67); // payment table 2/3, invoice detail 1/3
        splitPane.setBorder(null);
        splitPane.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && splitPane.isShowing()) {
                SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.67));
            }
        });

        // Payment Panel (LEFT)
        JPanel paymentPanel = new JPanel(new BorderLayout(0, 10));
        paymentPanel.setBackground(Color.WHITE);
        paymentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel paymentHeaderPanel = new JPanel(new BorderLayout());
        paymentHeaderPanel.setBackground(Color.WHITE);

        JLabel lblPaymentTitle = new JLabel("💳 Lịch sử thanh toán");
        lblPaymentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPaymentTitle.setForeground(new Color(44, 62, 80));

        // Filter and export panel
        JPanel filterExportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterExportPanel.setBackground(Color.WHITE);

        JLabel lblFilter = new JLabel("Lọc theo lớp:");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        classFilterCombo = new JComboBox<>();
        classFilterCombo.setPreferredSize(new Dimension(200, 30));
        classFilterCombo.addActionListener(e -> filterPaymentsByClass());

        JButton btnExportSelected = new JButton("📄 Xuất đã chọn");
        btnExportSelected.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportSelected.setBackground(new Color(52, 152, 219));
        btnExportSelected.setForeground(Color.WHITE);
        btnExportSelected.setFocusPainted(false);
        btnExportSelected.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnExportSelected.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportSelected.addActionListener(e -> exportSelectedPayments());

        JButton btnSelectAll = new JButton("Chọn tất cả");
        btnSelectAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSelectAll.setBackground(new Color(149, 165, 166));
        btnSelectAll.setForeground(Color.WHITE);
        btnSelectAll.setFocusPainted(false);
        btnSelectAll.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        btnSelectAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSelectAll.addActionListener(e -> selectAllPayments(true));

        JButton btnDeselectAll = new JButton("Bỏ chọn");
        btnDeselectAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDeselectAll.setBackground(new Color(149, 165, 166));
        btnDeselectAll.setForeground(Color.WHITE);
        btnDeselectAll.setFocusPainted(false);
        btnDeselectAll.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        btnDeselectAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeselectAll.addActionListener(e -> selectAllPayments(false));

        filterExportPanel.add(lblFilter);
        filterExportPanel.add(classFilterCombo);
        filterExportPanel.add(btnSelectAll);
        filterExportPanel.add(btnDeselectAll);
        filterExportPanel.add(btnExportSelected);

        paymentHeaderPanel.add(lblPaymentTitle, BorderLayout.WEST);
        paymentHeaderPanel.add(filterExportPanel, BorderLayout.EAST);

        paymentTable.setModel(paymentTableModel);
        paymentTable.setRowHeight(35);
        paymentTable.setFont(new Font("Arial", Font.PLAIN, 13));
        paymentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        paymentTable.getTableHeader().setBackground(new Color(46, 204, 113));
        paymentTable.getTableHeader().setForeground(Color.WHITE);
        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentTable.setGridColor(new Color(230, 230, 230));

        // Set column widths
        paymentTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // Checkbox
        paymentTable.getColumnModel().getColumn(0).setMaxWidth(50);
        paymentTable.getColumnModel().getColumn(1).setPreferredWidth(50);  // STT
        paymentTable.getColumnModel().getColumn(1).setMaxWidth(70);

        // Center align cells (except checkbox)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < paymentTable.getColumnCount(); i++) {
            paymentTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Add selection listener
        paymentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadInvoiceDetailForSelectedPayment();
            }
        });

        JScrollPane scrollPayment = new JScrollPane(paymentTable);
        scrollPayment.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        paymentPanel.add(paymentHeaderPanel, BorderLayout.NORTH);
        paymentPanel.add(scrollPayment, BorderLayout.CENTER);

        // Invoice Detail Panel (RIGHT)
        JPanel invoiceDetailPanel = new JPanel(new BorderLayout(0, 10));
        invoiceDetailPanel.setBackground(Color.WHITE);
        invoiceDetailPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel invoiceHeaderPanel = new JPanel(new BorderLayout());
        invoiceHeaderPanel.setBackground(Color.WHITE);

        JLabel lblInvoiceTitle = new JLabel("📄 Chi tiết hóa đơn");
        lblInvoiceTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblInvoiceTitle.setForeground(new Color(44, 62, 80));

        JButton btnExportInvoice = new JButton("📄 Xuất hóa đơn PDF");
        btnExportInvoice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportInvoice.setBackground(new Color(52, 152, 219));
        btnExportInvoice.setForeground(Color.WHITE);
        btnExportInvoice.setFocusPainted(false);
        btnExportInvoice.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnExportInvoice.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportInvoice.addActionListener(e -> exportInvoiceToPdf());

        invoiceHeaderPanel.add(lblInvoiceTitle, BorderLayout.WEST);
        invoiceHeaderPanel.add(btnExportInvoice, BorderLayout.EAST);

        invoiceDetailTable.setModel(invoiceDetailTableModel);
        invoiceDetailTable.setRowHeight(30);
        invoiceDetailTable.setFont(new Font("Arial", Font.PLAIN, 13));
        invoiceDetailTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        invoiceDetailTable.getTableHeader().setBackground(new Color(103, 58, 183));
        invoiceDetailTable.getTableHeader().setForeground(Color.WHITE);
        invoiceDetailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceDetailTable.setGridColor(new Color(230, 230, 230));

        // Set column widths
        invoiceDetailTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        invoiceDetailTable.getColumnModel().getColumn(1).setPreferredWidth(400);

        JScrollPane scrollInvoice = new JScrollPane(invoiceDetailTable);
        scrollInvoice.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        invoiceDetailPanel.add(invoiceHeaderPanel, BorderLayout.NORTH);
        invoiceDetailPanel.add(scrollInvoice, BorderLayout.CENTER);

        splitPane.setLeftComponent(paymentPanel);
        splitPane.setRightComponent(invoiceDetailPanel);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(splitPane, BorderLayout.CENTER);

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

    public void reload() {
        loadData();
    }

    private void loadData() {
        try {
            // Load all invoices for this student
            List<Invoice> studentInvoices = invoiceService.getByStudentId(studentId);
            Set<Long> studentInvoiceIds = new HashSet<>();
            for (Invoice inv : studentInvoices) {
                studentInvoiceIds.add(inv.getId());
            }

            // Load all payments and filter by student's invoices
            allPayments = paymentService.getAll().stream()
                    .filter(p -> p.getStudent() != null
                              && p.getStudent().getId().equals(studentId)
                              && p.getInvoice() != null
                              && studentInvoiceIds.contains(p.getInvoice().getId()))
                    .sorted((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate())) // Newest first
                    .toList();

            filteredPayments = new ArrayList<>(allPayments);

            // Populate class filter dropdown
            populateClassFilter();

            // Display filtered payments
            displayPayments(filteredPayments);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải dữ liệu: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateClassFilter() {
        classFilterCombo.removeAllItems();
        classFilterCombo.addItem("Tất cả lớp học");

        Set<String> classNames = new HashSet<>();
        for (Payment payment : allPayments) {
            if (payment.getInvoice() != null && payment.getInvoice().getEnrollment() != null) {
                String className = payment.getInvoice().getEnrollment().getClassEntity().getClassName();
                classNames.add(className);
            }
        }

        List<String> sortedClassNames = new ArrayList<>(classNames);
        Collections.sort(sortedClassNames);
        for (String className : sortedClassNames) {
            classFilterCombo.addItem(className);
        }
    }

    private void filterPaymentsByClass() {
        String selectedClass = (String) classFilterCombo.getSelectedItem();

        if (selectedClass == null || selectedClass.equals("Tất cả lớp học")) {
            filteredPayments = new ArrayList<>(allPayments);
        } else {
            filteredPayments = allPayments.stream()
                    .filter(p -> p.getInvoice() != null
                              && p.getInvoice().getEnrollment() != null
                              && selectedClass.equals(p.getInvoice().getEnrollment().getClassEntity().getClassName()))
                    .toList();
        }

        displayPayments(filteredPayments);
    }

    private void displayPayments(List<Payment> payments) {
        paymentTableModel.setRowCount(0);
        rowToPaymentMap.clear();

        double totalPaid = 0;
        long completedCount = 0;
        long pendingCount = 0;

        int stt = 1;
        int rowIndex = 0;
        for (Payment payment : payments) {
            String className = "N/A";
            if (payment.getInvoice() != null && payment.getInvoice().getEnrollment() != null) {
                className = payment.getInvoice().getEnrollment().getClassEntity().getClassName();
            }

            paymentTableModel.addRow(new Object[]{
                    false, // Checkbox
                    stt++,
                    payment.getId(),
                    className,
                    formatCurrency(payment.getAmount()),
                    payment.getPaymentMethod().toString(),
                    payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    payment.getStatus().toString()
            });

            rowToPaymentMap.put(rowIndex, payment);

            if (payment.getStatus() == PaymentStatus.Completed) {
                totalPaid += payment.getAmount();
                completedCount++;
            } else if (payment.getStatus() == PaymentStatus.Pending) {
                pendingCount++;
            }
            rowIndex++;
        }

        lblTotalPayments.setText(String.valueOf(allPayments.size()));
        lblTotalPaid.setText(formatCurrency(totalPaid));
        lblCompletedPayments.setText(String.valueOf(completedCount));
        lblPendingPayments.setText(String.valueOf(pendingCount));
    }

    private void loadInvoiceDetailForSelectedPayment() {
        int selectedRow = paymentTable.getSelectedRow();
        invoiceDetailTableModel.setRowCount(0);

        if (selectedRow < 0) {
            return;
        }

        Payment selectedPayment = rowToPaymentMap.get(selectedRow);
        if (selectedPayment == null || selectedPayment.getInvoice() == null) {
            invoiceDetailTableModel.addRow(new Object[]{"Thông báo", "Thanh toán này không liên kết với hóa đơn nào"});
            return;
        }

        Invoice invoice = selectedPayment.getInvoice();

        try {
            // Calculate paid amount for this invoice
            Double totalPaid = calculatePaidAmountForInvoice(invoice.getId());
            Double remaining = invoice.getTotalAmount() - totalPaid;

            // Display invoice details
            invoiceDetailTableModel.addRow(new Object[]{"Invoice ID", invoice.getId()});
            invoiceDetailTableModel.addRow(new Object[]{"Lớp học", invoice.getEnrollment().getClassEntity().getClassName()});
            invoiceDetailTableModel.addRow(new Object[]{"Khóa học",
                invoice.getEnrollment().getClassEntity().getCourse() != null ?
                invoice.getEnrollment().getClassEntity().getCourse().getCourseName() : "N/A"});
            invoiceDetailTableModel.addRow(new Object[]{"Giáo viên",
                invoice.getEnrollment().getClassEntity().getTeacher() != null ?
                invoice.getEnrollment().getClassEntity().getTeacher().getFullName() : "N/A"});
            invoiceDetailTableModel.addRow(new Object[]{"Tổng học phí", formatCurrency(invoice.getTotalAmount())});
            invoiceDetailTableModel.addRow(new Object[]{"Đã thanh toán", formatCurrency(totalPaid)});
            invoiceDetailTableModel.addRow(new Object[]{"Còn lại", formatCurrency(remaining)});
            invoiceDetailTableModel.addRow(new Object[]{"Ngày phát hành",
                invoice.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))});
            invoiceDetailTableModel.addRow(new Object[]{"Trạng thái hóa đơn", invoice.getStatus().toString()});

            if (invoice.getNote() != null && !invoice.getNote().isEmpty()) {
                invoiceDetailTableModel.addRow(new Object[]{"Ghi chú", invoice.getNote()});
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải chi tiết hóa đơn: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private Double calculatePaidAmountForInvoice(Long invoiceId) {
        try {
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

    private void selectAllPayments(boolean select) {
        for (int i = 0; i < paymentTableModel.getRowCount(); i++) {
            paymentTableModel.setValueAt(select, i, 0);
        }
    }

    private void exportSelectedPayments() {
        List<Payment> selectedPayments = new ArrayList<>();
        for (int i = 0; i < paymentTableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) paymentTableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                selectedPayments.add(rowToPaymentMap.get(i));
            }
        }

        if (selectedPayments.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn ít nhất một thanh toán để xuất!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get unique invoices from selected payments
        Set<Long> invoiceIds = new HashSet<>();
        List<Invoice> uniqueInvoices = new ArrayList<>();

        for (Payment payment : selectedPayments) {
            if (payment.getInvoice() != null && !invoiceIds.contains(payment.getInvoice().getId())) {
                invoiceIds.add(payment.getInvoice().getId());
                uniqueInvoices.add(payment.getInvoice());
            }
        }

        if (uniqueInvoices.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Các thanh toán đã chọn không có hóa đơn liên kết!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu hóa đơn và thanh toán PDF");

            if (uniqueInvoices.size() == 1) {
                fileChooser.setSelectedFile(new File("Invoice_Payment_" + uniqueInvoices.get(0).getId() + ".pdf"));
            } else {
                fileChooser.setSelectedFile(new File("Invoice_Payments_" + System.currentTimeMillis() + ".pdf"));
            }

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                }

                // Export all invoices with their payments
                int successCount = 0;
                for (Invoice invoice : uniqueInvoices) {
                    File invoiceFile;
                    if (uniqueInvoices.size() == 1) {
                        invoiceFile = fileToSave;
                    } else {
                        String dir = fileToSave.getParent();
                        String name = fileToSave.getName().replace(".pdf", "");
                        invoiceFile = new File(dir + File.separator + name + "_Invoice_" + invoice.getId() + ".pdf");
                    }

                    try {
                        exportInvoiceWithPaymentsJasper(invoice, invoiceFile, selectedPayments);
                        successCount++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (successCount > 0) {
                    JOptionPane.showMessageDialog(this,
                            String.format("Xuất thành công %d/%d hóa đơn PDF (bao gồm thông tin thanh toán)!\nĐường dẫn: %s",
                                    successCount, uniqueInvoices.size(), fileToSave.getParent()),
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Không thể xuất hóa đơn nào!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi xuất PDF: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportInvoiceWithPaymentsJasper(Invoice invoice, File outputFile, List<Payment> selectedPayments) throws Exception {
        try {
            // Load the JRXML template from resources
            InputStream reportStream = getClass().getResourceAsStream("/reports/invoice_template.jrxml");

            if (reportStream == null) {
                // If template doesn't exist, create a simple programmatic report
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

            // Filter payments for this invoice from selected payments
            List<Payment> invoicePayments = selectedPayments.stream()
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

    private void exportInvoiceToPdf() {
        int selectedRow = paymentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một thanh toán để xuất hóa đơn!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Payment selectedPayment = rowToPaymentMap.get(selectedRow);
        if (selectedPayment == null || selectedPayment.getInvoice() == null) {
            JOptionPane.showMessageDialog(this,
                    "Thanh toán này không có hóa đơn liên kết!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Invoice invoice = selectedPayment.getInvoice();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu hóa đơn PDF");
        fileChooser.setSelectedFile(new File("Invoice_" + invoice.getId() + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
            }

            try {
                exportInvoiceWithJasper(invoice, fileToSave);
                JOptionPane.showMessageDialog(this,
                        "Xuất hóa đơn PDF thành công!\nĐường dẫn: " + fileToSave.getAbsolutePath(),
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xuất hóa đơn PDF: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportInvoiceWithJasper(Invoice invoice, File outputFile) throws Exception {
        try {
            // Load the JRXML template from resources
            InputStream reportStream = getClass().getResourceAsStream("/reports/invoice_template.jrxml");

            if (reportStream == null) {
                // If template doesn't exist, create a simple programmatic report
                exportInvoiceWithSimpleJasper(invoice, outputFile);
                return;
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
            List<Payment> invoicePayments = allPayments.stream()
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
            // Fallback to simple report if there's an error
            exportInvoiceWithSimpleJasper(invoice, outputFile);
        }
    }

    private void exportInvoiceWithSimpleJasper(Invoice invoice, File outputFile) throws Exception {
        // Create a simple report programmatically without JRXML
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("TITLE", "HÓA ĐƠN HỌC PHÍ - LANGUAGE CENTER");
        parameters.put("INVOICE_ID", "Invoice ID: " + invoice.getId());
        parameters.put("ISSUE_DATE", "Ngày phát hành: " + invoice.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("STUDENT_NAME", "Sinh viên: " + invoice.getStudent().getFullName());
        parameters.put("STUDENT_EMAIL", "Email: " + invoice.getStudent().getEmail());
        parameters.put("STUDENT_PHONE", "SĐT: " + invoice.getStudent().getPhone());
        parameters.put("CLASS_NAME", "Lớp học: " + invoice.getEnrollment().getClassEntity().getClassName());
        parameters.put("COURSE_NAME", "Khóa học: " + (invoice.getEnrollment().getClassEntity().getCourse() != null ?
                invoice.getEnrollment().getClassEntity().getCourse().getCourseName() : "N/A"));

        Double totalPaid = calculatePaidAmountForInvoice(invoice.getId());
        Double remaining = invoice.getTotalAmount() - totalPaid;

        parameters.put("TOTAL_AMOUNT", "Tổng học phí: " + formatCurrency(invoice.getTotalAmount()));
        parameters.put("PAID_AMOUNT", "Đã thanh toán: " + formatCurrency(totalPaid));
        parameters.put("REMAINING_AMOUNT", "Còn lại: " + formatCurrency(remaining));
        parameters.put("STATUS", "Trạng thái: " + invoice.getStatus().toString());

        // Use a collection with the parameters as data
        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList.add(parameters);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

        // Since we don't have a template, we'll create a basic text-based PDF using JasperReports API
        // For simplicity, let's just use the standard text export method instead
        throw new Exception("Template not found. Please create invoice_template.jrxml in src/main/resources/reports/");
    }

    private void exportAllPayments() {
        if (allPayments == null || allPayments.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có thanh toán nào để xuất!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ask user what they want to export
        String[] options = {"Xuất tất cả hóa đơn PDF", "Hủy"};
        int choice = JOptionPane.showOptionDialog(this,
                "Bạn có " + allPayments.size() + " thanh toán.\nXuất hóa đơn PDF cho các thanh toán này?",
                "Xuất lịch sử thanh toán",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice != 0) {
            return;
        }

        try {
            // Get unique invoices from all payments
            Set<Long> invoiceIds = new HashSet<>();
            List<Invoice> uniqueInvoices = new ArrayList<>();

            for (Payment payment : allPayments) {
                if (payment.getInvoice() != null && !invoiceIds.contains(payment.getInvoice().getId())) {
                    invoiceIds.add(payment.getInvoice().getId());
                    uniqueInvoices.add(payment.getInvoice());
                }
            }

            if (uniqueInvoices.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không có hóa đơn nào để xuất!",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu hóa đơn PDF");
            fileChooser.setSelectedFile(new File("Payment_History_" + studentId + ".pdf"));

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                }

                // Export all invoices
                int successCount = 0;
                for (Invoice invoice : uniqueInvoices) {
                    File invoiceFile;
                    if (uniqueInvoices.size() == 1) {
                        invoiceFile = fileToSave;
                    } else {
                        String dir = fileToSave.getParent();
                        String name = fileToSave.getName().replace(".pdf", "");
                        invoiceFile = new File(dir + File.separator + name + "_Invoice_" + invoice.getId() + ".pdf");
                    }

                    try {
                        exportInvoiceWithJasper(invoice, invoiceFile);
                        successCount++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (successCount > 0) {
                    JOptionPane.showMessageDialog(this,
                            String.format("Xuất thành công %d/%d hóa đơn PDF!\nĐường dẫn: %s",
                                    successCount, uniqueInvoices.size(), fileToSave.getParent()),
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Không thể xuất hóa đơn nào!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi xuất lịch sử thanh toán: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatCurrency(Double amount) {
        return String.format("%,.0f VNĐ", amount);
    }
}
