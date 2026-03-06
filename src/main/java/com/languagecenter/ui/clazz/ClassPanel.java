package com.languagecenter.ui.clazz;

import com.languagecenter.model.Class;
import com.languagecenter.model.enums.ClassStatus;
import com.languagecenter.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ClassPanel extends JPanel {
    private final ClassService classService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    private final RoomService roomService;

    private final ClassTableModel tableModel = new ClassTableModel();
    private final JTable table = new JTable(tableModel);

    public ClassPanel(ClassService cs, CourseService cos, TeacherService ts, RoomService rs) {
        this.classService = cs;
        this.courseService = cos;
        this.teacherService = ts;
        this.roomService = rs;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 251));

        buildUI();
        styleTable();
        reload();
    }

    private void styleTable() {
        table.setRowHeight(35);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setSelectionBackground(new Color(232, 242, 254));
        // Chỉ cho phép chọn 1 dòng tại một thời điểm
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

    private void buildUI() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolbar.setOpaque(false);

        JButton btnAdd = createStyledButton("Thêm Lớp", new Color(34, 197, 94));
        JButton btnEdit = createStyledButton("Sửa", new Color(59, 130, 246));
        JButton btnDelete = createStyledButton("Xóa", new Color(239, 68, 68));
        JButton btnRefresh = createStyledButton("Làm mới", Color.DARK_GRAY);

        toolbar.add(btnAdd);
        toolbar.add(btnEdit); // Nút sửa đã được thêm
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        // Đăng ký sự kiện
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit()); // Gán sự kiện sửa
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> reload());

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
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
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Chỉnh Sửa Lớp Học",
                    selectedClass,
                    courseService.getAll(),
                    teacherService.getAll(),
                    roomService.getAll()
            );

            dlg.setVisible(true);

            if (dlg.isSaved()) {
                try {
                    classService.update(dlg.getClazz());
                    reload();
                    JOptionPane.showMessageDialog(this, "Cập nhật lớp học thành công!");
                } catch (Exception ex) {
                    // Bắt lỗi từ Service (validateCapacity)
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

    private void reload() {
        try { tableModel.setData(classService.getAll()); } catch (Exception e) { e.printStackTrace(); }
    }

    private void onAdd() {
        try {
            // Lấy dữ liệu cho ComboBox
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
                    // ĐÂY LÀ NƠI GỌI SERVICE: Nếu vi phạm sức chứa, Service sẽ ném Exception
                    classService.create(dlg.getClazz());

                    reload();
                    JOptionPane.showMessageDialog(this, "Thêm lớp học thành công!");
                } catch (Exception ex) {
                    // HIỂN THỊ LỖI: JOptionPane sẽ hiện thông báo "Số lượng học viên tối đa..."
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);


                }
            }
        } catch (Exception ex) {
            // Lỗi hệ thống (mất mạng, lỗi DB khi load danh sách...)
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage());
        }
    }
}