package com.languagecenter.ui.clas;

import com.languagecenter.model.*;
import com.languagecenter.model.Class;
import com.languagecenter.model.enums.ClassStatus;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ClassFormDialog extends JDialog {
    private final JTextField txtName = new JTextField();
    private final JSpinner spnMaxStudent = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
    private final JComboBox<Course> cboCourse = new JComboBox<>();
    private final JComboBox<Teacher> cboTeacher = new JComboBox<>();
    private final JComboBox<Room> cboRoom = new JComboBox<>();
    private final JComboBox<ClassStatus> cboStatus = new JComboBox<>(ClassStatus.values());

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
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        addLabelAndField(form, "Tên lớp học:", txtName, gbc, 0);
        addLabelAndField(form, "Khóa học:", cboCourse, gbc, 1);
        addLabelAndField(form, "Giáo viên:", cboTeacher, gbc, 2);
        addLabelAndField(form, "Phòng học:", cboRoom, gbc, 3);
        addLabelAndField(form, "Sĩ số tối đa:", spnMaxStudent, gbc, 4);
        addLabelAndField(form, "Trạng thái:", cboStatus, gbc, 5);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(Color.WHITE);
        JButton btnSave = new JButton("Lưu lại");
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(100, 35));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> {
            if(txtName.getText().isBlank()) return;
            clazz.setClassName(txtName.getText());
            clazz.setCourse((Course) cboCourse.getSelectedItem());
            clazz.setTeacher((Teacher) cboTeacher.getSelectedItem());
            clazz.setRoom((Room) cboRoom.getSelectedItem());
            clazz.setMaxStudent((Integer) spnMaxStudent.getValue());
            clazz.setStatus((ClassStatus) cboStatus.getSelectedItem());
            saved = true;
            dispose();
        });

        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        add(root);
    }

    private void addLabelAndField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        field.setPreferredSize(new Dimension(200, 30));
        panel.add(field, gbc);
    }

    private void fillData(Class c) {
        txtName.setText(c.getClassName());
        cboCourse.setSelectedItem(c.getCourse());
        cboTeacher.setSelectedItem(c.getTeacher());
        cboRoom.setSelectedItem(c.getRoom());
        spnMaxStudent.setValue(c.getMaxStudent());
        cboStatus.setSelectedItem(c.getStatus());
    }

    public boolean isSaved() { return saved; }
    public Class getClazz() { return clazz; }
}