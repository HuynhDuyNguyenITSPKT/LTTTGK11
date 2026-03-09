package com.languagecenter.ui.student;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.Student;
import com.languagecenter.service.StudentService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StudentProfilePage extends JPanel {
    private final Student student;
    private final StudentService studentService;
    private final String username;

    private JTextField txtPhone, txtEmail, txtAddress;

    public StudentProfilePage(Student student, String username, StudentService ss) {
        this.student = student;
        this.studentService = ss;
        this.username = username;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 50, 30, 50));

        buildUI();
    }

    private void buildUI() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("STUDENT PROFILE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g.gridwidth = 2; card.add(title, g);

        g.gridwidth = 1;
        addLabelAndField(card, g, "Full Name:", new JLabel(student.getFullName()), 1);
        addLabelAndField(card, g, "Phone:", txtPhone = new JTextField(student.getPhone()), 2);
        addLabelAndField(card, g, "Email:", txtEmail = new JTextField(student.getEmail()), 3);
        addLabelAndField(card, g, "Address:", txtAddress = new JTextField(student.getAddress()), 4);

        JButton btnUpdate = new JButton("Save Changes");
        btnUpdate.putClientProperty(FlatClientProperties.STYLE, "background:#673ab7; foreground:#ffffff; font:bold");
        btnUpdate.addActionListener(e -> {
            try {
                student.setPhone(txtPhone.getText().trim());
                student.setEmail(txtEmail.getText().trim());
                student.setAddress(txtAddress.getText().trim());

                studentService.update(student, username, null);
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        g.gridy = 5; g.gridx = 1; card.add(btnUpdate, g);
        add(card, BorderLayout.NORTH);
    }

    private void addLabelAndField(JPanel p, GridBagConstraints g, String label, JComponent comp, int y) {
        g.gridy = y; g.gridx = 0; p.add(new JLabel(label), g);
        g.gridx = 1;
        comp.setPreferredSize(new Dimension(300, 35));
        p.add(comp, g);
    }
}