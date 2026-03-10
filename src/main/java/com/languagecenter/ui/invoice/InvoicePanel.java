package com.languagecenter.ui.invoice;

import com.languagecenter.model.Invoice;
import com.languagecenter.model.enums.InvoiceStatus;
import com.languagecenter.service.InvoiceService;
import com.languagecenter.service.StudentService;
import com.languagecenter.stream.InvoiceStreamQueries;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class InvoicePanel extends JPanel {

    private final InvoiceService service;
    private final StudentService studentService;

    private final InvoiceTableModel tableModel = new InvoiceTableModel();
    private final JTable table = new JTable(tableModel);

    private List<Invoice> allData;

    private final JTextField txtStudentName = new JTextField(12);
    private final JTextField txtClassName = new JTextField(12);
    private final JComboBox<InvoiceStatus> cboStatus = new JComboBox<>();

    public InvoicePanel(InvoiceService service,
                       StudentService studentService) {

        this.service = service;
        this.studentService = studentService;

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

        JButton btnEdit = createButton("Edit", new Color(245, 158, 11));
        JButton btnDelete = createButton("Delete", new Color(239, 68, 68));
        JButton btnRefresh = createButton("Refresh", new Color(100, 116, 139));

        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        toolbar.add(new JLabel(" Student:"));
        toolbar.add(txtStudentName);

        toolbar.add(new JLabel(" Class:"));
        toolbar.add(txtClassName);

        toolbar.add(new JLabel(" Status:"));
        cboStatus.addItem(null);
        for (InvoiceStatus status : InvoiceStatus.values()) {
            cboStatus.addItem(status);
        }
        toolbar.add(cboStatus);

        JButton btnFilter = createButton("Filter", new Color(79, 70, 229));
        toolbar.add(btnFilter);

        JButton btnShowUnpaid = createButton("Unpaid", new Color(239, 68, 68));
        toolbar.add(btnShowUnpaid);

        add(toolbar, BorderLayout.NORTH);

        btnRefresh.addActionListener(e -> reload());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnFilter.addActionListener(e -> applyFilter());
        btnShowUnpaid.addActionListener(e -> showUnpaidInvoices());
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
            JOptionPane.showMessageDialog(this, "Error loading invoices: " + e.getMessage());
        }
    }

    private void applyFilter() {

        List<Invoice> result = allData;

        if (!txtStudentName.getText().isBlank()) {
            result = InvoiceStreamQueries.filterByStudentName(result, txtStudentName.getText());
        }

        if (!txtClassName.getText().isBlank()) {
            result = InvoiceStreamQueries.filterByClassName(result, txtClassName.getText());
        }

        if (cboStatus.getSelectedItem() != null) {
            result = InvoiceStreamQueries.filterByStatus(result, (InvoiceStatus) cboStatus.getSelectedItem());
        }

        tableModel.setData(result);
    }

    private void showUnpaidInvoices() {
        List<Invoice> unpaid = InvoiceStreamQueries.getUnpaidInvoices(allData);
        tableModel.setData(unpaid);
    }

    private void onEdit() {

        int row = table.getSelectedRow();

        if (row < 0) return;

        Invoice invoice = tableModel.getInvoiceAt(row);

        // Hóa đơn đã thanh toán → chỉ xem, không chỉnh sửa
        if (invoice.getStatus() == InvoiceStatus.Paid) {
            JOptionPane.showMessageDialog(this,
                    "Invoice #" + invoice.getId() + " đã ở trạng thái PAID.\nKhông thể chỉnh sửa hóa đơn đã thanh toán.",
                    "Không thể chỉnh sửa",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {

            InvoiceFormDialog dlg =
                    new InvoiceFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this),
                            "Edit Invoice",
                            invoice,
                            studentService.getAll()
                    );

            dlg.setVisible(true);

            if (dlg.isSaved()) {

                service.update(dlg.getInvoice());
                reload();
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
                "Are you sure you want to delete this?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION)
            return;

        Invoice invoice = tableModel.getInvoiceAt(row);

        try {

            service.delete(invoice.getId());
            reload();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
