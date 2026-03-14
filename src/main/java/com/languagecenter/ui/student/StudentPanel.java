package com.languagecenter.ui.student;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.export.*;
import com.languagecenter.model.Student;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.StudentStatus;
import com.languagecenter.service.StudentService;
import com.languagecenter.stream.StudentStreamQueries;
import com.languagecenter.ui.CustomTableRenderer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StudentPanel extends JPanel {
    private final StudentService studentService;
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<StudentStatus> cboStatus = new JComboBox<>(StudentStatus.values());
    private final JTable table = new JTable();
    private final StudentTableModel tableModel = new StudentTableModel();

    public StudentPanel(StudentService studentService) {
        this.studentService = studentService;
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        buildUI();
        reload();
    }

    private void buildUI() {
        // --- TOP BAR ---
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Student Directory");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search name...");
        txtSearch.setPreferredSize(new Dimension(250, 38));
        cboStatus.setPreferredSize(new Dimension(140, 38));

        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cboStatus);
        filterPanel.add(txtSearch);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(filterPanel, BorderLayout.EAST);

        // --- TOOLBAR ---
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);
        Dimension btnSize = new Dimension(110, 40);

        JButton btnAdd = createBtn("Add New", "#27ae60", btnSize);
        JButton btnView = createBtn("View", "#34495e", btnSize);
        JButton btnEdit = createBtn("Edit", "#2980b9", btnSize);
        JButton btnDelete = createBtn("Delete", "#e74c3c", btnSize);
        JButton btnRefresh = createBtn("Refresh", "#7f8c8d", btnSize);
        JButton btnExport = createBtn("Export", "#8e44ad", btnSize);
        toolBar.add(btnExport);

        btnExport.addActionListener(e -> onExport());

        toolBar.add(btnAdd); toolBar.addSeparator();
        toolBar.add(btnView); toolBar.addSeparator();
        toolBar.add(btnEdit); toolBar.addSeparator();
        toolBar.add(btnDelete); toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnRefresh);

        // --- TABLE ---
        table.setModel(tableModel);
        table.setRowHeight(40);
        CustomTableRenderer renderer = new CustomTableRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JPanel north = new JPanel(new GridLayout(2, 1, 0, 10));
        north.add(topBar); north.add(toolBar);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Listeners
        btnAdd.addActionListener(e -> onAdd());
        btnView.addActionListener(e -> onAction(true));
        btnEdit.addActionListener(e -> onAction(false));
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> reload());
        txtSearch.addActionListener(e -> reload());
        cboStatus.addActionListener(e -> reload());
    }

    private JButton createBtn(String text, String color, Dimension size) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(size);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.putClientProperty(FlatClientProperties.STYLE, "background:" + color + "; foreground:#fff");
        return btn;
    }

    private void onExport() {

        int option = JOptionPane.showOptionDialog(
                this,
                "Choose export format",
                "Export",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"PDF", "Excel", "CSV"},
                "PDF"
        );

        try {

            List<Student> students = studentService.getAll();

            JFileChooser chooser = new JFileChooser();

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

                String path = chooser.getSelectedFile().getAbsolutePath();

                ExportStrategy strategy = null;

                switch (option) {
                    case 0 -> {
                        path += ".pdf";
                        strategy = new PdfExportStrategy();
                    }
                    case 1 -> {
                        path += ".xlsx";
                        strategy = new ExcelExportStrategy();
                    }
                    case 2 -> {
                        path += ".csv";
                        strategy = new CsvExportStrategy();
                    }
                }

                if (strategy != null) {

                    ExportService service = new ExportService(strategy);
                    service.export(students, path);

                    JOptionPane.showMessageDialog(this, "Export success!");
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    public void reload() {
        try {
            List<Student> list = studentService.getAll();
            String key = txtSearch.getText().trim();
            if (!key.isEmpty()) list = StudentStreamQueries.searchByName(list, key);
            StudentStatus status = (StudentStatus) cboStatus.getSelectedItem();
            if (status != null) list = StudentStreamQueries.filterByStatus(list, status);
            tableModel.setData(list);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void onAction(boolean isView) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Student s = tableModel.getStudent(row);
        try {
            UserAccount acc = studentService.findAccountByStudentId(s.getId());
            StudentFormDialog dlg = new StudentFormDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    isView ? "View Details" : "Edit Student", s, acc != null ? acc.getUsername() : "", isView);
            dlg.setVisible(true);
            if (!isView && dlg.isSaved()) {
                studentService.update(dlg.getStudent(), dlg.getUsername(), dlg.getPassword());
                reload();
            }
        } catch (Exception ex) {reload(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        if (JOptionPane.showConfirmDialog(this, "Delete student?", "Confirm", 0) == 0) {
            try {
                studentService.delete(tableModel.getStudent(row).getId());
                reload();
            } catch (Exception ex) { reload(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }

    private void onAdd() {
        StudentFormDialog dlg = new StudentFormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New", null, null, false);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                studentService.create(dlg.getStudent(), dlg.getUsername(), dlg.getPassword());
                reload();
            } catch (Exception ex) { reload(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }
}