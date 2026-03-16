package com.languagecenter.ui.payment;

import com.languagecenter.model.Payment;
import com.languagecenter.model.enums.PaymentMethod;
import com.languagecenter.model.enums.PaymentStatus;
import com.languagecenter.service.InvoiceService;
import com.languagecenter.service.PaymentService;
import com.languagecenter.service.StudentService;
import com.languagecenter.stream.PaymentStreamQueries;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class PaymentPanel extends JPanel {

    private final PaymentService service;
    private final StudentService studentService;
    private final InvoiceService invoiceService;

    private final PaymentTableModel tableModel = new PaymentTableModel();
    private final JTable table = new JTable(tableModel);

    private List<Payment> allData;

    private final JTextField txtStudentName = new JTextField(12);
    private final JTextField txtInvoiceId = new JTextField(8);
    private final JTextField txtReferenceCode = new JTextField(10);
    private final JComboBox<PaymentStatus> cboStatus = new JComboBox<>();
    private final JComboBox<PaymentMethod> cboPaymentMethod = new JComboBox<>();

    public PaymentPanel(PaymentService service,
                       StudentService studentService,
                       InvoiceService invoiceService) {

        this.service = service;
        this.studentService = studentService;
        this.invoiceService = invoiceService;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        buildToolbar();
        buildTable();

        reload();
    }

    private void buildToolbar() {

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JButton btnAdd = createButton("Add", new Color(34, 197, 94));
        JButton btnEdit = createButton("Edit", new Color(245, 158, 11));
        JButton btnDelete = createButton("Delete", new Color(239, 68, 68));
        JButton btnRefresh = createButton("Refresh", new Color(100, 116, 139));

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        toolbar.add(new JLabel(" Student:"));
        toolbar.add(txtStudentName);

        toolbar.add(new JLabel(" Inv ID:"));
        toolbar.add(txtInvoiceId);

        toolbar.add(new JLabel(" Ref:"));
        toolbar.add(txtReferenceCode);

        toolbar.add(new JLabel(" Status:"));
        cboStatus.addItem(null);
        for (PaymentStatus status : PaymentStatus.values()) {
            cboStatus.addItem(status);
        }
        toolbar.add(cboStatus);

        toolbar.add(new JLabel(" Method:"));
        cboPaymentMethod.addItem(null);
        for (PaymentMethod method : PaymentMethod.values()) {
            cboPaymentMethod.addItem(method);
        }
        toolbar.add(cboPaymentMethod);

        JButton btnFilter = createButton("Filter", new Color(79, 70, 229));
        toolbar.add(btnFilter);

        add(toolbar, BorderLayout.NORTH);

        btnRefresh.addActionListener(e -> reload());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnFilter.addActionListener(e -> applyFilter());
    }

    private void buildTable() {

        table.setRowHeight(32);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(new Color(220, 220, 220));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        table.setSelectionBackground(new Color(200, 230, 255));

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JButton createButton(String text, Color color) {

        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    public void reload() {

        try {

            allData = service.getAll();
            tableModel.setData(allData);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading payments: " + e.getMessage());
        }
    }

    private void applyFilter() {

        List<Payment> result = allData;

        if (!txtStudentName.getText().isBlank()) {
            result = PaymentStreamQueries.filterByStudentName(result, txtStudentName.getText().trim());
        }

        if (!txtInvoiceId.getText().isBlank()) {
            try {
                Long invId = Long.parseLong(txtInvoiceId.getText().trim());
                result = PaymentStreamQueries.filterByInvoiceId(result, invId);
            } catch (NumberFormatException ignored) {}
        }

        if (!txtReferenceCode.getText().isBlank()) {
            result = PaymentStreamQueries.filterByReferenceCode(result, txtReferenceCode.getText().trim());
        }

        if (cboStatus.getSelectedItem() != null) {
            result = PaymentStreamQueries.filterByStatus(result, (PaymentStatus) cboStatus.getSelectedItem());
        }

        if (cboPaymentMethod.getSelectedItem() != null) {
            result = PaymentStreamQueries.filterByPaymentMethod(result, (PaymentMethod) cboPaymentMethod.getSelectedItem());
        }

        tableModel.setData(result);
    }

    private void onAdd() {

        try {

            PaymentFormDialog dlg =
                    new PaymentFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this),
                            "Add Payment",
                            null,
                            studentService.getAll(),
                            invoiceService.getAll(),
                            service
                    );

            dlg.setVisible(true);

            if (dlg.isSaved()) {

                Payment payment = dlg.getPayment();

                // Kiểm tra payment phải có invoice
                if (payment.getInvoice() == null) {
                    JOptionPane.showMessageDialog(this,
                            "Payment must have an Invoice!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                service.create(payment);
                reload();

                JOptionPane.showMessageDialog(this,
                        "Payment created!\nInvoice status will be updated if fully paid.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void onEdit() {

        int row = table.getSelectedRow();

        if (row < 0) return;

        Payment payment = tableModel.getPaymentAt(row);

        // Thanh toán đã hoàn tất → chỉ xem, không chỉnh sửa
        if (payment.getStatus() == PaymentStatus.Completed) {
            JOptionPane.showMessageDialog(this,
                    "Payment #" + payment.getId() + " đã ở trạng thái COMPLETED.\nKhông thể chỉnh sửa thanh toán đã hoàn tất.",
                    "Không thể chỉnh sửa",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {

            PaymentFormDialog dlg =
                    new PaymentFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this),
                            "Edit Payment",
                            payment,
                            studentService.getAll(),
                            invoiceService.getAll(),
                            service
                    );

            dlg.setVisible(true);

            if (dlg.isSaved()) {

                service.update(dlg.getPayment());
                reload();

                JOptionPane.showMessageDialog(this,
                        "Payment updated!\nInvoice status has been re-checked.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void onDelete() {

        int row = table.getSelectedRow();

        if (row < 0) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete?\nInvoice status will be re-checked after deletion.",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION)
            return;

        Payment payment = tableModel.getPaymentAt(row);

        try {

            service.delete(payment.getId());
            reload();

            JOptionPane.showMessageDialog(this,
                    "Payment đã được xóa!\nInvoice status đã được cập nhật lại.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
