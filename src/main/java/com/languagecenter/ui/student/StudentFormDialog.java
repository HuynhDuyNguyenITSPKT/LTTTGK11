package com.languagecenter.ui.student;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.languagecenter.model.Student;
import com.languagecenter.model.enums.Gender;
import com.languagecenter.model.enums.StudentStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

public class StudentFormDialog extends JDialog {
    private final JTextField txtName = new JTextField(20);
    
    // THAY THẾ: Sử dụng DatePicker thay vì JTextField
    private final DatePicker datePickerDOB; 
    
    private final JTextField txtPhone = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtAddress = new JTextField(20);
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPass = new JPasswordField(20);
    private final JComboBox<Gender> cboGender = new JComboBox<>(Gender.values());
    private final JComboBox<StudentStatus> cboStatus = new JComboBox<>(StudentStatus.values());

    private boolean saved = false;
    private final Student student;
    private final boolean readOnly;

    public StudentFormDialog(Frame owner, String title, Student existing, String username, boolean readOnly) {
        super(owner, title, true);
        this.student = (existing != null) ? existing : new Student();
        this.readOnly = readOnly;

        // Khởi tạo DatePicker với cấu hình tiếng Việt hoặc giao diện tùy chỉnh
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
        dateSettings.setAllowEmptyDates(false);
        datePickerDOB = new DatePicker(dateSettings);

        buildUI();

        if (existing != null) {
            txtName.setText(existing.getFullName());
            // Đổ dữ liệu vào DatePicker
            if (existing.getDateOfBirth() != null) {
                datePickerDOB.setDate(existing.getDateOfBirth());
            }
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            txtAddress.setText(existing.getAddress());
            cboGender.setSelectedItem(existing.getGender());
            cboStatus.setSelectedItem(existing.getStatus());
            txtUsername.setText(username);

            if (readOnly) {
                disableFields();
            }
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void disableFields() {
        txtName.setEditable(false);
        datePickerDOB.setEnabled(false); // Khóa DatePicker
        txtPhone.setEditable(false);
        txtEmail.setEditable(false);
        txtAddress.setEditable(false);
        txtUsername.setEditable(false);
        txtPass.setEnabled(false);
        cboGender.setEnabled(false);
        cboStatus.setEnabled(false);
    }

    private void buildUI() {
        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 5, 8, 5);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Nhãn và các Component tương ứng
        String[] labels = {"Full Name:", "Date of Birth:", "Phone:", "Email:", "Address:", "Gender:", "Status:", "Username:", "Password:"};
        JComponent[] fields = {txtName, datePickerDOB, txtPhone, txtEmail, txtAddress, cboGender, cboStatus, txtUsername, txtPass};

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(labelFont);
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            form.add(lbl, g);

            fields[i].setFont(inputFont);
            fields[i].setPreferredSize(new Dimension(300, 35));
            
            // Tùy chỉnh riêng cho DatePicker để nó trông giống các TextField khác
            if (fields[i] instanceof DatePicker) {
                datePickerDOB.getComponentDateTextField().putClientProperty(FlatClientProperties.STYLE, "arc:5");
                datePickerDOB.getComponentToggleCalendarButton().setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            g.gridx = 1; g.weightx = 1.0;
            form.add(fields[i], g);
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        if (!readOnly) {
            JButton btnSave = new JButton("Save Data");
            btnSave.putClientProperty(FlatClientProperties.STYLE, "background:#27ae60; foreground:#fff; font:bold");
            btnSave.setPreferredSize(new Dimension(120, 40));
            btnSave.addActionListener(e -> onSave());
            actions.add(btnSave);
        }

        JButton btnClose = new JButton(readOnly ? "Close" : "Cancel");
        btnClose.setPreferredSize(new Dimension(100, 40));
        btnClose.addActionListener(e -> dispose());
        actions.add(btnClose);

        container.add(new JScrollPane(form), BorderLayout.CENTER);
        container.add(actions, BorderLayout.SOUTH);
        setContentPane(container);
    }

    private void onSave() {
        try {
            if (txtName.getText().isBlank()) throw new IllegalArgumentException("Name is required");
            
            student.setFullName(txtName.getText().trim());
            // Lấy ngày trực tiếp từ DatePicker (trả về LocalDate)
            student.setDateOfBirth(datePickerDOB.getDate());
            
            student.setPhone(txtPhone.getText().trim());
            student.setEmail(txtEmail.getText().trim());
            student.setAddress(txtAddress.getText().trim());
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