package com.languagecenter.ui.room;

import com.languagecenter.model.Room;
import com.languagecenter.model.enums.RoomStatus;
import com.languagecenter.service.RoomService;
import com.languagecenter.stream.RoomStreamQueries;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RoomPanel extends JPanel {

    private final RoomService roomService;
    private final JTextField txtSearch = new JTextField(15);
    private final JComboBox<RoomStatus> cboStatus = new JComboBox<>();
    private final RoomTableModel tableModel = new RoomTableModel();
    private final JTable table = new JTable(tableModel);
    private List<Room> cachedRooms = new ArrayList<>();

    // Palette màu sắc hiện đại
    private final Color COLOR_PRIMARY = new Color(59, 130, 246);  // Blue
    private final Color COLOR_SUCCESS = new Color(34, 197, 94);  // Green
    private final Color COLOR_DANGER = new Color(239, 68, 68);   // Red
    private final Color COLOR_BG = new Color(245, 247, 251);     // Gray nhạt

    public RoomPanel(RoomService roomService) {
        this.roomService = roomService;

        setLayout(new BorderLayout(0, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        buildUI();
        styleTable();
        reloadAll();
    }

    private void buildUI() {
        // --- Toolbar Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Khu vực bên trái: Tìm kiếm & Lọc
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        cboStatus.addItem(null); // Option "Tất cả"
        for (RoomStatus s : RoomStatus.values()) cboStatus.addItem(s);

        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(txtSearch);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cboStatus);

        // Khu vực bên phải: Thao tác dữ liệu
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd = createStyledButton("Add Room", COLOR_SUCCESS);
        JButton btnEdit = createStyledButton("Edit", COLOR_PRIMARY);
        JButton btnDelete = createStyledButton("Delete", COLOR_DANGER);
        JButton btnRefresh = createStyledButton("Refresh", Color.DARK_GRAY);

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        actionPanel.add(btnRefresh);

        headerPanel.add(filterPanel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- Bảng dữ liệu ---
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        add(scrollPane, BorderLayout.CENTER);

        // --- Xử lý sự kiện ---
        cboStatus.addActionListener(e -> runFilter());

        // Tự động lọc khi gõ phím
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
        table.setRowHeight(35);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(235, 235, 235));

        // căn giữa text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Renderer cho cột trạng thái (Cột số 5)
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);

                if (value instanceof RoomStatus status) {
                    l.setOpaque(true);
                    if (!isSelected) {
                        switch (status) {
                            case Active -> { l.setBackground(new Color(220, 252, 231)); l.setForeground(new Color(22, 101, 52)); }
                            case Inactive -> { l.setBackground(new Color(254, 243, 199)); l.setForeground(new Color(146, 64, 14)); }
                        }
                    }
                }
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
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(110, 32));
        return btn;
    }

    public void reload() {
        reloadAll();
    }

    private void reloadAll() {
        try {
            cachedRooms = roomService.getAll();
            tableModel.setData(cachedRooms);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void runFilter() {
        List<Room> result = cachedRooms;
        String keyword = txtSearch.getText();
        if (keyword != null && !keyword.isBlank()) {
            result = RoomStreamQueries.searchByName(result, keyword);
        }
        RoomStatus status = (RoomStatus) cboStatus.getSelectedItem();
        if (status != null) {
            result = RoomStreamQueries.filterByStatus(result, status);
        }
        tableModel.setData(result);
    }

    private void onAdd() {
        RoomFormDialog dlg = new RoomFormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Room", null);
        dlg.setVisible(true);
        if (!dlg.isSaved()) return;
        try {
            roomService.create(dlg.getRoom());
            reloadAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a room to edit");
            return;
        }
        Room r = tableModel.getRoom(row);
        RoomFormDialog dlg = new RoomFormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Room", r);
        dlg.setVisible(true);
        if (!dlg.isSaved()) return;
        try {
            roomService.update(dlg.getRoom());
            reloadAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Room r = tableModel.getRoom(row);
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận xóa phòng " + r.getRoomName() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                roomService.delete(r.getId());
                reloadAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        }
    }
}