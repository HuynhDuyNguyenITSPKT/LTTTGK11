package com.languagecenter.ui.clas;

import com.languagecenter.model.*;
import com.languagecenter.model.Class;
import com.languagecenter.model.enums.ClassStatus;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ClassFormDialog extends JDialog {
    private final JTextField txtName = new JTextField();
    private final JSpinner spnMaxStudent = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
    private final JComboBox<Course> cboCourse = new JComboBox<>();
    private final JComboBox<Teacher> cboTeacher = new JComboBox<>();
    private final JComboBox<Room> cboRoom = new JComboBox<>();
    private final JComboBox<ClassStatus> cboStatus = new JComboBox<>(ClassStatus.values());
    private final JSpinner spnStartDate = new JSpinner(new javax.swing.SpinnerDateModel());
    private final JSpinner spnEndDate = new JSpinner(new javax.swing.SpinnerDateModel());

    private boolean saved = false;
    private final Class clazz;

    public ClassFormDialog(Frame owner, String title, Class existing,
                           List<Course> courses, List<Teacher> teachers, List<Room> rooms) {
        super(owner, title, true);
        this.clazz = (existing != null) ? existing : new Class();

        setupComboBoxes(courses, teachers, rooms);
        buildUI();

        if (existing != null) fillData(existing);

        pack();
        setSize(450, 450);
        setLocationRelativeTo(owner);
    }

    private void setupComboBoxes(List<Course> courses, List<Teacher> teachers, List<Room> rooms) {
        courses.forEach(cboCourse::addItem);
        teachers.forEach(cboTeacher::addItem);
        rooms.forEach(cboRoom::addItem);

        // Renderer để hiển thị tên thay vì Object ID
        ListCellRenderer<Object> renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Course c) value = c.getCourseName();
                else if (value instanceof Teacher t) value = t.getFullName();
                else if (value instanceof Room r) value = r.getRoomName();
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        };
        cboCourse.setRenderer(renderer);
        cboTeacher.setRenderer(renderer);
        cboRoom.setRenderer(renderer);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 245, 250));
        root.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(245, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 8, 12, 8);
        gbc.weightx = 1.0;

        addLabelAndField(form, "Tên lớp học", txtName, gbc, 0);
        addLabelAndField(form, "Khóa học", cboCourse, gbc, 1);
        addLabelAndField(form, "Giáo viên", cboTeacher, gbc, 2);
        addLabelAndField(form, "Phòng học", cboRoom, gbc, 3);
        addLabelAndField(form, "Ngày bắt đầu", spnStartDate, gbc, 4);
        addLabelAndField(form, "Ngày kết thúc", spnEndDate, gbc, 5);
        addLabelAndField(form, "Sĩ số tối đa", spnMaxStudent, gbc, 6);
        addLabelAndField(form, "Trạng thái", cboStatus, gbc, 7);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setBackground(new Color(245, 245, 250));

        JButton btnCancel = createModernButton("Hủy", new Color(200, 200, 200));
        JButton btnSave = createModernButton("Lưu", new Color(37, 99, 235));

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        add(root);
    }

    private void addLabelAndField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(40, 40, 40));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        field.setBorder(createModernBorder());
        if (field instanceof JTextField) field.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(field, gbc);
    }

    private javax.swing.border.Border createModernBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 220), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(100, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void onSave() {
        if(txtName.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên lớp học!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        clazz.setClassName(txtName.getText());
        clazz.setCourse((Course) cboCourse.getSelectedItem());
        clazz.setTeacher((Teacher) cboTeacher.getSelectedItem());
        clazz.setRoom((Room) cboRoom.getSelectedItem());
        clazz.setStartDate(java.time.Instant.ofEpochMilli(((java.util.Date) spnStartDate.getValue()).getTime())
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        clazz.setEndDate(java.time.Instant.ofEpochMilli(((java.util.Date) spnEndDate.getValue()).getTime())
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        clazz.setMaxStudent((Integer) spnMaxStudent.getValue());
        clazz.setStatus((ClassStatus) cboStatus.getSelectedItem());
        saved = true;
        dispose();
    }

    private void fillData(Class c) {
        txtName.setText(c.getClassName());
        cboCourse.setSelectedItem(c.getCourse());
        cboTeacher.setSelectedItem(c.getTeacher());
        cboRoom.setSelectedItem(c.getRoom());
        if (c.getStartDate() != null) {
            spnStartDate.setValue(java.sql.Date.valueOf(c.getStartDate()));
        }
        if (c.getEndDate() != null) {
            spnEndDate.setValue(java.sql.Date.valueOf(c.getEndDate()));
        }
        spnMaxStudent.setValue(c.getMaxStudent());
        cboStatus.setSelectedItem(c.getStatus());
    }

    public boolean isSaved() { return saved; }
    public Class getClazz() { return clazz; }
}