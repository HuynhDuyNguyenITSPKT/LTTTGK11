package com.languagecenter.ui.course;

import com.languagecenter.model.Course;
import com.languagecenter.model.enums.CourseStatus;
import com.languagecenter.service.CourseService;
import com.languagecenter.stream.CourseStreamQueries;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CoursePanel extends JPanel {

    private final CourseService service;
    private final JTextField txtSearch = new JTextField(15);
    private final JComboBox<CourseStatus> cboStatus = new JComboBox<>();
    private final CourseTableModel tableModel = new CourseTableModel();
    private final JTable table = new JTable(tableModel);

    private List<Course> cachedCourses = new ArrayList<>();

    // Định nghĩa bảng màu Modern
    private final Color COLOR_PRIMARY = new Color(59, 130, 246);   // Blue
    private final Color COLOR_SUCCESS = new Color(34, 197, 94);   // Green
    private final Color COLOR_DANGER = new Color(239, 68, 68);    // Red
    private final Color COLOR_BG = new Color(243, 244, 246);      // Light Gray background

    public CoursePanel(CourseService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15)); // Tạo khoảng cách lề

        buildUI();
        styleTable();
        reloadAll();
    }

    private void buildUI() {
        // --- Toolbar phía trên ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Panel chứa bộ lọc (Trái)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        txtSearch.setPreferredSize(new Dimension(200, 30));
        cboStatus.addItem(null); // Tất cả
        for(CourseStatus s : CourseStatus.values()) cboStatus.addItem(s);

        filterPanel.add(new JLabel("Tìm kiếm:"));
        filterPanel.add(txtSearch);
        filterPanel.add(new JLabel("Trạng thái:"));
        filterPanel.add(cboStatus);

        // Panel chứa các nút bấm (Phải)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd = createStyledButton("Thêm mới", COLOR_SUCCESS);
        JButton btnEdit = createStyledButton("Chỉnh sửa", COLOR_PRIMARY);
        JButton btnDelete = createStyledButton("Xóa", COLOR_DANGER);
        JButton btnRefresh = createStyledButton("Làm mới", Color.DARK_GRAY);

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        actionPanel.add(btnRefresh);

        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- Bảng hiển thị ở giữa ---
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
        add(scrollPane, BorderLayout.CENTER);

        // --- Sự kiện ---
        // Lọc ngay khi chọn ComboBox
        cboStatus.addActionListener(e -> runFilter());

        // Lọc ngay khi đang gõ (Real-time search)
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { runFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { runFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { runFilter(); }
        });

        btnRefresh.addActionListener(e -> reloadAll());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
    }

    private void styleTable() {
        table.setRowHeight(35); // Dòng cao hơn cho thoáng
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.setGridColor(new Color(229, 231, 235));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false); // Chỉ hiện dòng kẻ ngang cho hiện đại

        // căn giữa text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Renderer cho cột trạng thái (Cột số 4 giả định)
        // Lưu ý: index cột tùy thuộc vào TableModel của bạn
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(noFocusBorder);
                if (column == 5 && value instanceof CourseStatus status) {
                    setHorizontalAlignment(CENTER);
                    setOpaque(true);
                    if (!isSelected) {
                        switch (status) {
                            case Active -> { setBackground(new Color(220, 252, 231)); setForeground(new Color(22, 101, 52)); }
                            case Inactive -> { setBackground(new Color(254, 226, 226)); setForeground(new Color(153, 27, 27)); }
                        }
                    }
                } else {
                    if (!isSelected) setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                    setHorizontalAlignment(LEFT);
                }
                return c;
            }
        });
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(100, 32));
        return btn;
    }

    // --- Các hàm Logic giữ nguyên logic cũ nhưng bắt lỗi tốt hơn ---

    private void reloadAll() {
        try {
            cachedCourses = service.getAll();
            tableModel.setData(cachedCourses);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void runFilter() {
        List<Course> result = cachedCourses;
        String keyword = txtSearch.getText();
        if (keyword != null && !keyword.isBlank()) {
            result = CourseStreamQueries.searchByName(result, keyword);
        }
        CourseStatus status = (CourseStatus) cboStatus.getSelectedItem();
        if (status != null) {
            result = CourseStreamQueries.filterByStatus(result, status);
        }
        tableModel.setData(result);
    }

    private void onAdd() {
        CourseFormDialog dlg = new CourseFormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm khóa học mới", null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                service.create(dlg.getCourse());
                reloadAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khóa học!");
            return;
        }
        Course c = tableModel.getCourse(row);
        CourseFormDialog dlg = new CourseFormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chỉnh sửa khóa học", c);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                service.update(dlg.getCourse());
                reloadAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khóa học cần xóa!");
            return;
        }
        Course c = tableModel.getCourse(row);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khóa học này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.delete(c.getId());
                reloadAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        }
    }
}