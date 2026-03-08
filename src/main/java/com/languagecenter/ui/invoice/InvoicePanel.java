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

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(30, 136, 229));
        toolbar.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JButton btnEdit = createButton("Sửa", new Color(255, 167, 38));
        JButton btnDelete = createButton("Xóa", new Color(244, 67, 54));
        JButton btnRefresh = createButton("Refresh", new Color(120, 144, 156));

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

        JButton btnFilter = createButton("Filter", new Color(33, 150, 243));
        toolbar.add(btnFilter);

        JButton btnShowUnpaid = createButton("Chưa thanh toán", new Color(255, 87, 34));
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
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        return btn;
    }

    private void reload() {

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

        try {

            InvoiceFormDialog dlg =
                    new InvoiceFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this),
                            "Sửa Invoice",
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
                "Bạn chắc chắn muốn xóa?",
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
