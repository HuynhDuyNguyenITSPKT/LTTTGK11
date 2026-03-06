package com.languagecenter.ui.student;

import com.formdev.flatlaf.FlatClientProperties;
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

    // Khôi phục ComboBox trạng thái sinh viên
    private final JComboBox<StudentStatus> cboStatus = new JComboBox<>(StudentStatus.values());

    private final JTable table = new JTable();
    private final StudentTableModel tableModel = new StudentTableModel();

    public StudentPanel(StudentService studentService) {
        this.studentService = studentService;
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        buildUI();
        reload(); // Tải dữ liệu ban đầu
    }

    private void buildUI() {
        // --- TOP: TITLE & FILTER (COMBO + SEARCH) ---
        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblTitle = new JLabel("Student Directory");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        topBar.add(lblTitle, gbc);

        // Cụm tìm kiếm và lọc trạng thái bên phải
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search name or email...");
        txtSearch.setPreferredSize(new Dimension(250, 38));

        cboStatus.setPreferredSize(new Dimension(150, 38));

        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cboStatus);
        filterPanel.add(txtSearch);

        gbc.gridx = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        topBar.add(filterPanel, gbc);

        // --- MIDDLE: ACTION BUTTONS (ĐỒNG NHẤT KÍCH THƯỚC) ---
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);

        Dimension btnSize = new Dimension(120, 40);
        JButton btnAdd = createBtn("Add New", "#27ae60", btnSize);
        JButton btnEdit = createBtn("Edit", "#2980b9", btnSize);
        JButton btnDelete = createBtn("Delete", "#e74c3c", btnSize);
        JButton btnRefresh = createBtn("Refresh", "#7f8c8d", btnSize);

        toolBar.add(btnAdd);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(btnEdit);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(btnDelete);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnRefresh);

        // --- TABLE: CĂN GIỮA & ĐỔI MÀU TRẠNG THÁI ---
        table.setModel(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        // Sử dụng Renderer chung để căn giữa và đổi màu status
        CustomTableRenderer renderer = new CustomTableRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JPanel northPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        northPanel.setOpaque(false);
        northPanel.add(topBar);
        northPanel.add(toolBar);

        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- EVENT LISTENERS ---
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> reload());

        // Tự động reload khi nhấn Enter hoặc thay đổi combo
        txtSearch.addActionListener(e -> reload());
        cboStatus.addActionListener(e -> reload());
    }

    private JButton createBtn(String text, String color, Dimension size) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(size);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "background:" + color + "; foreground:#fff");
        return btn;
    }

    private void reload() {
        try {
            String keyword = txtSearch.getText().trim();
            StudentStatus status = (StudentStatus) cboStatus.getSelectedItem();

            List<Student> list = studentService.getAll();

            // Lọc theo tên/keyword
            if (!keyword.isEmpty()) {
                list = StudentStreamQueries.searchByName(list, keyword);
            }

            // Lọc theo trạng thái sinh viên
            if (status != null) {
                list = StudentStreamQueries.filterByStatus(list, status);
            }

            tableModel.setData(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Reload Error: " + ex.getMessage());
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student to edit!");
            return;
        }
        Student s = tableModel.getStudent(row);
        try {
            UserAccount acc = studentService.findAccountByStudentId(s.getId());
            StudentFormDialog dlg = new StudentFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Edit Student", s, acc != null ? acc.getUsername() : "");
            dlg.setVisible(true);

            if (dlg.isSaved()) {
                studentService.update(dlg.getStudent(), dlg.getUsername(), dlg.getPassword());
                reload();
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student to delete!");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete this student?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
            try {
                studentService.delete(tableModel.getStudent(row).getId());
                reload();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }

    private void onAdd() {
        StudentFormDialog dlg = new StudentFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), "Add Student", null, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                studentService.create(dlg.getStudent(), dlg.getUsername(), dlg.getPassword());
                reload();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }
}