package com.languagecenter.ui.student;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.Student;
import com.languagecenter.model.enums.Gender;
import com.languagecenter.model.enums.StudentStatus;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StudentFormDialog extends JDialog {
    private final JTextField txtName = new JTextField(20);
    private final JTextField txtPhone = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPass = new JPasswordField(20);
    private final JComboBox<Gender> cboGender = new JComboBox<>(Gender.values());
    private final JComboBox<StudentStatus> cboStatus = new JComboBox<>(StudentStatus.values());

    private boolean saved = false;
    private Student student;

    public StudentFormDialog(Frame owner, String title, Student existing, String username) {
        super(owner, title, true);
        this.student = (existing != null) ? existing : new Student();

        buildUI();

        if (existing != null) {
            txtName.setText(existing.getFullName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            cboGender.setSelectedItem(existing.getGender());
            cboStatus.setSelectedItem(existing.getStatus());
            txtUsername.setText(username);
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Form fields setup
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 5, 8, 5);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        String[] labels = {"Full Name:", "Phone Number:", "Email Address:", "Gender:", "Status:", "Username:", "Password:"};
        JComponent[] fields = {txtName, txtPhone, txtEmail, cboGender, cboStatus, txtUsername, txtPass};

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(labelFont);
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            form.add(lbl, g);

            fields[i].setFont(inputFont);
            fields[i].setPreferredSize(new Dimension(250, 35));
            g.gridx = 1; g.weightx = 1.0;
            form.add(fields[i], g);
        }

        // Actions
        JButton btnSave = new JButton("Save Data");
        btnSave.putClientProperty(FlatClientProperties.STYLE, "background:#27ae60; foreground:#fff; font:bold");
        btnSave.setPreferredSize(new Dimension(120, 40));
        btnSave.addActionListener(e -> onSave());

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(btnSave);
        actions.add(btnCancel);

        container.add(form, BorderLayout.CENTER);
        container.add(actions, BorderLayout.SOUTH);
        setContentPane(container);
    }

    private void onSave() {
        try {
            if (txtName.getText().isBlank()) throw new IllegalArgumentException("Name is required");
            if (txtUsername.getText().isBlank()) throw new IllegalArgumentException("Username is required");

            student.setFullName(txtName.getText().trim());
            student.setPhone(txtPhone.getText().trim());
            student.setEmail(txtEmail.getText().trim());
            student.setGender((Gender) cboGender.getSelectedItem());
            student.setStatus((StudentStatus) cboStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public Student getStudent() { return student; }
    public String getUsername() { return txtUsername.getText(); }
    public String getPassword() { return new String(txtPass.getPassword()); }
}