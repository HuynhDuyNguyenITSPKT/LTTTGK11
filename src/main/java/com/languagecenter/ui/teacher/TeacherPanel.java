package com.languagecenter.ui.teacher;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.Teacher;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.TeacherStatus;
import com.languagecenter.service.TeacherService;
import com.languagecenter.stream.TeacherStreamQueries;
import com.languagecenter.ui.CustomTableRenderer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TeacherPanel extends JPanel {
    private final TeacherService teacherService;
    private final JTextField txtSearch = new JTextField();

    // Khôi phục ComboBox trạng thái
    private final JComboBox<TeacherStatus> cboStatus = new JComboBox<>(TeacherStatus.values());

    private final TeacherTableModel tableModel = new TeacherTableModel();
    private final JTable table = new JTable(tableModel);

    public TeacherPanel(TeacherService teacherService) {
        this.teacherService = teacherService;
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // Thêm mục "All Status" vào combo nếu cần (tùy thuộc vào enum của bạn)
        // cboStatus.insertAt(null, 0);

        buildUI();
        reload();
    }

    private void buildUI() {
        // --- TOP: TITLE & SEARCH & STATUS ---
        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblTitle = new JLabel("Teacher Directory");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        topBar.add(lblTitle, gbc);

        // Group Search và Combo Trạng thái bên phải
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search name...");
        txtSearch.setPreferredSize(new Dimension(250, 38));

        cboStatus.setPreferredSize(new Dimension(150, 38));

        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cboStatus);
        filterPanel.add(txtSearch);

        gbc.gridx = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        topBar.add(filterPanel, gbc);

        // --- MIDDLE: ACTION BUTTONS ---
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

        // --- TABLE SETUP ---
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
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

        // Search khi nhấn Enter hoặc đổi Trạng thái
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

    private void reload() {
        try {
            String keyword = txtSearch.getText().trim();
            TeacherStatus status = (TeacherStatus) cboStatus.getSelectedItem();

            List<Teacher> list = teacherService.getAll();

            // Lọc theo tên
            if (!keyword.isEmpty()) {
                list = TeacherStreamQueries.searchByName(list, keyword);
            }

            // Lọc theo trạng thái
            if (status != null) {
                list = TeacherStreamQueries.filterByStatus(list, status);
            }

            tableModel.setData(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a teacher!");
            return;
        }
        Teacher t = tableModel.getTeacher(row);
        try {
            UserAccount acc = teacherService.findAccountByTeacherId(t.getId());
            TeacherFormDialog dlg = new TeacherFormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Teacher", t, acc != null ? acc.getUsername() : "");
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                teacherService.update(dlg.getTeacher(), dlg.getUsername(), dlg.getPassword());
                reload();
            }
        } catch (Exception ex) { 
            reload();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        if (JOptionPane.showConfirmDialog(this, "Delete teacher?", "Confirm", 0) == 0) {
            try {
                teacherService.delete(tableModel.getTeacher(row).getId());
                reload();
            } catch (Exception ex) { reload();JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }

    private void onAdd() {
        TeacherFormDialog dlg = new TeacherFormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Teacher", null, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                teacherService.create(dlg.getTeacher(), dlg.getUsername(), dlg.getPassword());
                reload();
            } catch (Exception ex) { reload();JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }
}