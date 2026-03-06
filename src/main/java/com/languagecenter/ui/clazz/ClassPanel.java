package com.languagecenter.ui.clazz;

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
        toolbar.add(new JLabel("Tên lớp:"));
        toolbar.add(txtSearch);

        // 2. Nhóm Trạng thái
        toolbar.add(new JLabel("Trạng thái:"));
        cboStatusFilter.addItem(null); // Để mặc định là "Tất cả"
        for (ClassStatus s : ClassStatus.values()) cboStatusFilter.addItem(s);
        toolbar.add(cboStatusFilter);

        // 3. Nhóm Học phí (Lấy từ Course)
        toolbar.add(new JLabel("Học phí từ:"));
        toolbar.add(txtMinFee);
        toolbar.add(new JLabel("đến:"));
        toolbar.add(txtMaxFee);

        // 4. Các nút chức năng
        JButton btnSearch = createStyledButton("Lọc", new Color(79, 70, 229)); // Màu tím hiện đại
        JButton btnAdd = createStyledButton("Thêm Lớp", new Color(34, 197, 94));
        JButton btnEdit = createStyledButton("Sửa", new Color(59, 130, 246));
        JButton btnDelete = createStyledButton("Xóa", new Color(239, 68, 68));
        JButton btnRefresh = createStyledButton("Làm mới", Color.DARK_GRAY);

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
            JOptionPane.showMessageDialog(this, "Vui lòng nhập học phí là số hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }

        tableModel.setData(result);
    }

    private void reload() {
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

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
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
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lớp học để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Class selectedClass = tableModel.getClassAt(selectedRow);
        try {
            ClassFormDialog dlg = new ClassFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), "Chỉnh Sửa Lớp Học", selectedClass,
                    courseService.getAll(), teacherService.getAll(), roomService.getAll()
            );
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                try {
                    classService.update(dlg.getClazz());
                    reload();
                    JOptionPane.showMessageDialog(this, "Cập nhật lớp học thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi cập nhật", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void onDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp học cần xóa!");
            return;
        }
        Class selectedClass = tableModel.getClassAt(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa lớp " + selectedClass.getClassName() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                classService.delete(selectedClass.getId());
                reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa: " + ex.getMessage());
            }
        }
    }

    private void onAdd() {
        try {
            var courses = courseService.getAll();
            var teachers = teacherService.getAll();
            var rooms = roomService.getAll();
            ClassFormDialog dlg = new ClassFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), "Thêm Lớp Mới", null,
                    courses, teachers, rooms
            );
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                try {
                    classService.create(dlg.getClazz());
                    reload();
                    JOptionPane.showMessageDialog(this, "Thêm lớp học thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage());
        }
    }
}