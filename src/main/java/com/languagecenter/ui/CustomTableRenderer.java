package com.languagecenter.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CustomTableRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Căn giữa tất cả các chữ trong bảng
        setHorizontalAlignment(JLabel.CENTER);

        // Xử lý màu sắc cho cột Status (giả định là cột cuối cùng)
        if (column == table.getColumnCount() - 1 && value != null) {
            String status = value.toString().toUpperCase();
            if (status.contains("ACTIVE") || status.contains("ENABLE") || status.contains("ONLINE")) {
                c.setForeground(new Color(46, 204, 113)); // Màu xanh lá hiện đại
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                c.setForeground(new Color(231, 76, 60)); // Màu đỏ hiện đại
                setFont(getFont().deriveFont(Font.BOLD));
            }
        } else {
            if (!isSelected) c.setForeground(Color.BLACK);
        }

        return c;
    }
}