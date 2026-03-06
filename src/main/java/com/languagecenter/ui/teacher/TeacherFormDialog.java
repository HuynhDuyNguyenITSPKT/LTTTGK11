package com.languagecenter.ui.teacher;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.Teacher;
import com.languagecenter.model.enums.TeacherStatus;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

public class TeacherFormDialog extends JDialog {
    private final JTextField txtName = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtSpecialty = new JTextField();
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private final JComboBox<TeacherStatus> cboStatus = new JComboBox<>(TeacherStatus.values());

    private boolean saved = false;
    private Teacher teacher;

    public TeacherFormDialog(Frame owner, String title, Teacher existing, String username) {
        super(owner, title, true);
        this.teacher = (existing != null) ? existing : new Teacher();
        buildUI();

        if (existing != null) {
            txtName.setText(existing.getFullName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            txtSpecialty.setText(existing.getSpecialty());
            cboStatus.setSelectedItem(existing.getStatus());
            txtUsername.setText(username);
        }
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 5, 8, 5);

        String[] labels = {"Name:", "Phone:", "Email:", "Specialty:", "Status:", "Username:", "Password:"};
        JComponent[] fields = {txtName, txtPhone, txtEmail, txtSpecialty, cboStatus, txtUsername, txtPass};

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.gridx = 0; g.gridy = i;
            form.add(lbl, g);

            fields[i].setPreferredSize(new Dimension(250, 35));
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g.gridx = 1;
            form.add(fields[i], g);
        }

        JButton btnSave = new JButton("Save Teacher");
        btnSave.putClientProperty(FlatClientProperties.STYLE, "background:#2980b9; foreground:#fff; font:bold");
        btnSave.setPreferredSize(new Dimension(130, 40));
        btnSave.addActionListener(e -> onSave());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnSave);

        container.add(form, BorderLayout.CENTER);
        container.add(actions, BorderLayout.SOUTH);
        setContentPane(container);
    }

    private void onSave() {
        try {
            if(txtName.getText().isBlank()) throw new IllegalArgumentException("Name required");
            teacher.setFullName(txtName.getText().trim());
            teacher.setPhone(txtPhone.getText());
            teacher.setEmail(txtEmail.getText());
            teacher.setSpecialty(txtSpecialty.getText());
            teacher.setStatus((TeacherStatus) cboStatus.getSelectedItem());
            teacher.setHireDate(LocalDate.now());
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public Teacher getTeacher() { return teacher; }
    public String getUsername() { return txtUsername.getText(); }
    public String getPassword() { return new String(txtPass.getPassword()); }
}