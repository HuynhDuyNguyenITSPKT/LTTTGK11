package com.languagecenter.ui.clas;

import com.languagecenter.model.Class;
import com.languagecenter.model.enums.ClassStatus;
import com.languagecenter.service.*;
import com.languagecenter.stream.ClassStreamQueries; // Import logic lọc

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClassPanel extends JPanel {
    private final ClassService classService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    private final RoomService roomService;

    // Thành phần UI phục vụ lọc
    private final JTextField txtSearch = new JTextField(12);
    private final JComboBox<ClassStatus> cboStatusFilter = new JComboBox<>();
    private final JTextField txtMinFee = new JTextField(6);
    private final JTextField txtMaxFee = new JTextField(6);

    private final ClassTableModel tableModel = new ClassTableModel();
    private final JTable table = new JTable(tableModel);

    // Biến lưu trữ dữ liệu gốc để lọc offline (Stream)
    private List<Class> cachedClasses = new ArrayList<>();

    public ClassPanel(ClassService cs, CourseService cos, TeacherService ts, RoomService rs) {
        this.classService = cs;
        this.courseService = cos;
        this.teacherService = ts;
        this.roomService = rs;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 251));

        buildUI();
        styleTable();
        reload(); // Tải dữ liệu lần đầu
    }

    private void buildUI() {
        // --- Toolbar phía trên ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setOpaque(false);

        // 1. Nhóm Tìm kiếm
        toolbar.add(new JLabel("Class Name:"));
        toolbar.add(txtSearch);

        // 2. Status group
        toolbar.add(new JLabel("Status:"));
        cboStatusFilter.addItem(null);
        for (ClassStatus s : ClassStatus.values()) cboStatusFilter.addItem(s);
        toolbar.add(cboStatusFilter);

        // 3. Fee from Course
        toolbar.add(new JLabel("Fee from:"));
        toolbar.add(txtMinFee);
        toolbar.add(new JLabel("to:"));
        toolbar.add(txtMaxFee);

        // 4. Function buttons
        JButton btnSearch = createStyledButton("Filter", new Color(79, 70, 229));
        JButton btnAdd = createStyledButton("Add Class", new Color(34, 197, 94));
        JButton btnEdit = createStyledButton("Edit", new Color(59, 130, 246));
        JButton btnDelete = createStyledButton("Delete", new Color(239, 68, 68));
        JButton btnRefresh = createStyledButton("Refresh", Color.DARK_GRAY);

        toolbar.add(btnSearch);
        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        // --- Gán sự kiện ---
        btnSearch.addActionListener(e -> runFilter());
        btnRefresh.addActionListener(e -> reload());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /**
     * Thực hiện logic lọc sử dụng Stream API
     */
    private void runFilter() {
        List<Class> result = new ArrayList<>(cachedClasses);

        // 1. Lọc theo tên lớp
        String keyword = txtSearch.getText();
        if (keyword != null && !keyword.isBlank()) {
            result = ClassStreamQueries.searchByName(result, keyword);
        }

        // 2. Lọc theo trạng thái
        ClassStatus status = (ClassStatus) cboStatusFilter.getSelectedItem();
        if (status != null) {
            result = ClassStreamQueries.filterByStatus(result, status);
        }

        // 3. Lọc theo học phí (Fee của Course)
        try {
            Double min = txtMinFee.getText().isBlank() ? null : Double.valueOf(txtMinFee.getText());
            Double max = txtMaxFee.getText().isBlank() ? null : Double.valueOf(txtMaxFee.getText());
            result = ClassStreamQueries.filterByCourseFee(result, min, max);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric fee!", "Input Error", JOptionPane.ERROR_MESSAGE);
        }

        tableModel.setData(result);
    }
    
    public void reload() {
        try {
            // Tải lại danh sách gốc từ Database
            cachedClasses = classService.getAll();
            // Cập nhật lên Table và xóa trắng bộ lọc UI nếu cần
            tableModel.setData(cachedClasses);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ... (Các hàm styleTable, onAdd, onEdit, onDelete giữ nguyên từ code của bạn)

    private void styleTable() {
        table.setRowHeight(35);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // căn giữa text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof ClassStatus s) {
                    l.setOpaque(true);
                    switch (s) {
                        case Open -> { l.setBackground(new Color(220, 252, 231)); l.setForeground(new Color(22, 101, 52)); }
                        case Ongoing -> { l.setBackground(new Color(219, 234, 254)); l.setForeground(new Color(30, 64, 175)); }
                        case Cancelled -> { l.setBackground(new Color(254, 226, 226)); l.setForeground(new Color(153, 27, 27)); }
                        default -> { l.setBackground(Color.WHITE); l.setForeground(Color.BLACK); }
                    }
                }
                if(isSelected) l.setBackground(table.getSelectionBackground());
                return l;
            }
        });
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void onEdit() {

    int viewRow = table.getSelectedRow();

    if (viewRow < 0) {
        JOptionPane.showMessageDialog(this,
                "Please select a class to edit!",
                "Notice",
                JOptionPane.WARNING_MESSAGE);
        return;
    }

    // convert view index -> model index
    int modelRow = table.convertRowIndexToModel(viewRow);

    Class selectedClass = tableModel.getClassAt(modelRow);

    try {

        ClassFormDialog dlg = new ClassFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Class",
                selectedClass,
                courseService.getAll(),
                teacherService.getAll(),
                roomService.getAll()
        );

        dlg.setVisible(true);

        if (dlg.isSaved()) {

            classService.update(dlg.getClazz());

            reload();

            JOptionPane.showMessageDialog(this,
                    "Class updated successfully!");
        }

    } catch (Exception ex) {

        ex.printStackTrace();

        JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage());
    }
}

    private void onDelete() {

    int viewRow = table.getSelectedRow();

    if (viewRow < 0) {
        JOptionPane.showMessageDialog(this,
                "Please select a class to delete!");
        return;
    }

    // convert view index -> model index
    int modelRow = table.convertRowIndexToModel(viewRow);

    Class selectedClass = tableModel.getClassAt(modelRow);

    int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete class "
                    + selectedClass.getClassName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {

        try {

            classService.delete(selectedClass.getId());

            reload();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this,
                    "Cannot delete: " + ex.getMessage());
        }
    }
}

    private void onAdd() {
        try {
            var courses = courseService.getAll();
            var teachers = teacherService.getAll();
            var rooms = roomService.getAll();
            ClassFormDialog dlg = new ClassFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), "Add New Class", null,
                    courses, teachers, rooms
            );
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                try {
                    classService.create(dlg.getClazz());
                    reload();
                    JOptionPane.showMessageDialog(this, "Class added successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Business Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "System error: " + ex.getMessage());
        }
    }
}