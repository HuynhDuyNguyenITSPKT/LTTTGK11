package com.languagecenter.ui.component;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CustomHeader extends JPanel {

    public CustomHeader(String title, String userName, Color themeColor, ActionListener logoutAction) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(0, 65));
        // Tạo đường kẻ mảnh phía dưới
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        // --- Left Side: Title & Info ---
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 18));
        leftPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(themeColor); // Màu nhấn theo loại User

        JLabel lblSeparator = new JLabel("|");
        lblSeparator.setForeground(new Color(200, 200, 200));

        JLabel lblWelcome = new JLabel("Welcome, " + userName);
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWelcome.setForeground(new Color(100, 100, 100));

        leftPanel.add(lblTitle);
        leftPanel.add(lblSeparator);
        leftPanel.add(lblWelcome);

        // --- Right Side: Logout & Icons ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        rightPanel.setOpaque(false);

        JButton btnLogout = new JButton("Log Out");
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.putClientProperty(FlatClientProperties.STYLE, 
            "buttonType:borderless; " +
            "foreground:#e74c3c; " +
            "font:bold; " +
            "hoverBackground:#fdedec");
        
        btnLogout.addActionListener(logoutAction);
        
        rightPanel.add(btnLogout);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }
}