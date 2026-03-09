package com.languagecenter.ui.teacher;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.Teacher;
import com.languagecenter.service.TeacherService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TeacherProfilePage extends JPanel {
    private final Teacher teacher;
    private final TeacherService teacherService;
    private final String username;

    private JTextField txtPhone, txtEmail, txtSpecialty;

    public TeacherProfilePage(Teacher teacher, String username, TeacherService ts) {
        this.teacher = teacher;
        this.teacherService = ts;
        this.username = username;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 50, 30, 50));
        buildUI();
    }

    private void buildUI() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:20; [light]background:shade(@background,3%)");

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(12, 10, 12, 10);

        // Tiêu đề trang
        JLabel lblTitle = new JLabel("TEACHER PROFILE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 130, 76));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        card.add(lblTitle, g);

        g.gridwidth = 1;
        addReadOnlyRow(card, g, "Teacher ID:", "TC-" + teacher.getId(), 1);
        addReadOnlyRow(card, g, "Full Name:", teacher.getFullName(), 2);

        addEditableRow(card, g, "Phone:", txtPhone = new JTextField(teacher.getPhone()), 3);
        addEditableRow(card, g, "Email:", txtEmail = new JTextField(teacher.getEmail()), 4);
        addEditableRow(card, g, "Specialty:", txtSpecialty = new JTextField(teacher.getSpecialty()), 5);

        JButton btnSave = new JButton("Save Profile");
        btnSave.putClientProperty(FlatClientProperties.STYLE, "background:#1e824c; foreground:#ffffff; font:bold; arc:10");
        btnSave.setPreferredSize(new Dimension(200, 45));
        btnSave.addActionListener(e -> saveProfile());

        g.gridx = 1; g.gridy = 6; g.insets = new Insets(20, 10, 10, 10);
        card.add(btnSave, g);

        add(card, BorderLayout.NORTH);
    }

    private void addReadOnlyRow(JPanel p, GridBagConstraints g, String label, String value, int y) {
        g.gridy = y; g.gridx = 0; p.add(new JLabel(label), g);
        g.gridx = 1;
        JLabel val = new JLabel(value);
        val.setFont(val.getFont().deriveFont(Font.ITALIC));
        p.add(val, g);
    }

    private void addEditableRow(JPanel p, GridBagConstraints g, String label, JTextField field, int y) {
        g.gridy = y; g.gridx = 0; p.add(new JLabel(label), g);
        g.gridx = 1;
        field.setPreferredSize(new Dimension(350, 35));
        p.add(field, g);
    }

    private void saveProfile() {
        try {
            teacher.setPhone(txtPhone.getText().trim());
            teacher.setEmail(txtEmail.getText().trim());
            teacher.setSpecialty(txtSpecialty.getText().trim());

            teacherService.update(teacher, username, null);
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}