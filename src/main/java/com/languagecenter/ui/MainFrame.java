package com.languagecenter.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.service.*;
import com.languagecenter.ui.clazz.ClassPanel;
import com.languagecenter.ui.student.StudentPanel;
import com.languagecenter.ui.teacher.TeacherPanel;
import com.languagecenter.ui.course.CoursePanel;
import com.languagecenter.ui.room.RoomPanel; // Giả sử bạn đã có RoomPanel

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public MainFrame(StudentService ss, TeacherService ts, CourseService cs, RoomService rs,ClassService classService) {
        super("Language Center Management - Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // 1. Sidebar Panel (Menu dọc)
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5));

        // Logo Sidebar
        JLabel lblLogo = new JLabel("ENGLISH CENTER");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setBorder(new EmptyBorder(30, 0, 40, 0));
        sidebar.add(lblLogo);

        // Các nút menu - Sửa lại phần bị thiếu
        sidebar.add(createMenuButton("Dashboard", "DASH"));
        sidebar.add(createMenuButton("Students", "STUDENT"));
        sidebar.add(createMenuButton("Teachers", "TEACHER"));
        sidebar.add(createMenuButton("Courses", "COURSE"));
        sidebar.add(createMenuButton("Rooms", "ROOM")); // Đã thêm nút Room
        sidebar.add(createMenuButton("Payments", "PAY"));

        // 2. Content Panel (Sử dụng CardLayout)
        contentPanel.add(new JPanel(), "DASH");
        contentPanel.add(new StudentPanel(ss), "STUDENT");
        contentPanel.add(new TeacherPanel(ts), "TEACHER");
        contentPanel.add(new CoursePanel(cs), "COURSE");
        contentPanel.add(new RoomPanel(rs), "ROOM"); // Đã thêm RoomPanel
        contentPanel.add(new ClassPanel(classService,cs,ts,rs), "CLASS"); // Thêm ClassPanel nếu cần
        contentPanel.add(new JPanel(), "PAY");

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setSize(1250, 800);
        setLocationRelativeTo(null);
    }

    private JButton createMenuButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(210, 45));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Style nút theo phong cách Sidebar hiện đại
        btn.putClientProperty(FlatClientProperties.STYLE,
                "buttonType:borderless; " +
                        "foreground:#ecf0f1; " +
                        "focusedBackground:#34495e; " +
                        "hoverBackground:#34495e; " +
                        "arc:0");

        btn.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        return btn;
    }
}