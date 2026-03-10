package com.languagecenter.ui.clas;

import com.languagecenter.model.*;
import com.languagecenter.model.Class;
import com.languagecenter.model.enums.ClassStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ClassFormDialog extends JDialog {

    private final JTextField txtName = new JTextField();

    private final JSpinner spnMaxStudent =
            new JSpinner(new SpinnerNumberModel(20,1,100,1));

    private final JComboBox<Course> cboCourse = new JComboBox<>();
    private final JComboBox<Teacher> cboTeacher = new JComboBox<>();
    private final JComboBox<Room> cboRoom = new JComboBox<>();

    private final JComboBox<ClassStatus> cboStatus =
            new JComboBox<>(ClassStatus.values());

    private final JSpinner spnStartDate =
            new JSpinner(new SpinnerDateModel());

    private final JSpinner spnEndDate =
            new JSpinner(new SpinnerDateModel());

    private boolean saved = false;
    private final Class clazz;

    public ClassFormDialog(Frame owner,
                           String title,
                           Class existing,
                           List<Course> courses,
                           List<Teacher> teachers,
                           List<Room> rooms) {

        super(owner,title,true);

        this.clazz = existing != null ? existing : new Class();

        setupComboBoxes(courses,teachers,rooms);

        buildUI();

        if(existing != null) fillData(existing);

        pack();
        setSize(500,600);
        setLocationRelativeTo(owner);
    }

    private void setupComboBoxes(List<Course> courses,
                                 List<Teacher> teachers,
                                 List<Room> rooms){

        courses.forEach(cboCourse::addItem);
        teachers.forEach(cboTeacher::addItem);
        rooms.forEach(cboRoom::addItem);

        ListCellRenderer<Object> renderer =
                new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,Object value,int index,
                    boolean isSelected,boolean cellHasFocus){

                if(value instanceof Course c)
                    value = c.getCourseName();

                else if(value instanceof Teacher t)
                    value = t.getFullName();

                else if(value instanceof Room r)
                    value = r.getRoomName();

                return super.getListCellRendererComponent(
                        list,value,index,isSelected,cellHasFocus);
            }
        };

        cboCourse.setRenderer(renderer);
        cboTeacher.setRenderer(renderer);
        cboRoom.setRenderer(renderer);
    }

    private void buildUI(){

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245,247,251));
        root.setBorder(new EmptyBorder(25,25,25,25));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addField(form,"Class Name",txtName,gbc,0);
        addField(form,"Course",cboCourse,gbc,1);
        addField(form,"Teacher",cboTeacher,gbc,2);
        addField(form,"Room",cboRoom,gbc,3);
        addField(form,"Start Date",spnStartDate,gbc,4);
        addField(form,"End Date",spnEndDate,gbc,5);
        addField(form,"Max Students",spnMaxStudent,gbc,6);
        addField(form,"Status",cboStatus,gbc,7);

        // format date spinner
        JSpinner.DateEditor startEditor =
                new JSpinner.DateEditor(spnStartDate,"yyyy-MM-dd");
        spnStartDate.setEditor(startEditor);

        JSpinner.DateEditor endEditor =
                new JSpinner.DateEditor(spnEndDate,"yyyy-MM-dd");
        spnEndDate.setEditor(endEditor);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT,15,0));
        actions.setOpaque(false);

        JButton btnCancel =
                createButton("Cancel",new Color(156,163,175));

        JButton btnSave =
                createButton("Save",new Color(37,99,235));

        btnCancel.addActionListener(e->dispose());
        btnSave.addActionListener(e->onSave());

        actions.add(btnCancel);
        actions.add(btnSave);

        root.add(form,BorderLayout.CENTER);
        root.add(actions,BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addField(JPanel panel,
                          String label,
                          JComponent field,
                          GridBagConstraints gbc,
                          int row){

        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;

        JLabel lb = new JLabel(label);
        lb.setFont(new Font("Segoe UI",Font.BOLD,12));

        panel.add(lb,gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(210,210,210)),
                BorderFactory.createEmptyBorder(6,8,6,8)));

        field.setFont(new Font("Segoe UI",Font.PLAIN,12));

        panel.add(field,gbc);
    }

    private JButton createButton(String text,Color color){

        JButton btn = new JButton(text);

        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI",Font.BOLD,12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100,35));

        return btn;
    }

    private void onSave(){

        if(txtName.getText().isBlank()){
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter class name!",
                    "Validation",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        clazz.setClassName(txtName.getText());
        clazz.setCourse((Course)cboCourse.getSelectedItem());
        clazz.setTeacher((Teacher)cboTeacher.getSelectedItem());
        clazz.setRoom((Room)cboRoom.getSelectedItem());

        java.util.Date start =
                (java.util.Date)spnStartDate.getValue();

        java.util.Date end =
                (java.util.Date)spnEndDate.getValue();

        clazz.setStartDate(
                start.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate());

        clazz.setEndDate(
                end.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate());

        clazz.setMaxStudent((Integer)spnMaxStudent.getValue());
        clazz.setStatus((ClassStatus)cboStatus.getSelectedItem());

        saved = true;
        dispose();
    }

    private void fillData(Class c){

    txtName.setText(c.getClassName());

    // Course
    for(int i = 0; i < cboCourse.getItemCount(); i++){
        Course course = cboCourse.getItemAt(i);
        if(course.getId().equals(c.getCourse().getId())){
            cboCourse.setSelectedIndex(i);
            break;
        }
    }

    // Teacher
    for(int i = 0; i < cboTeacher.getItemCount(); i++){
        Teacher teacher = cboTeacher.getItemAt(i);
        if(teacher.getId().equals(c.getTeacher().getId())){
            cboTeacher.setSelectedIndex(i);
            break;
        }
    }

    // Room
    for(int i = 0; i < cboRoom.getItemCount(); i++){
        Room room = cboRoom.getItemAt(i);
        if(room.getId().equals(c.getRoom().getId())){
            cboRoom.setSelectedIndex(i);
            break;
        }
    }

    if(c.getStartDate()!=null)
        spnStartDate.setValue(java.sql.Date.valueOf(c.getStartDate()));

    if(c.getEndDate()!=null)
        spnEndDate.setValue(java.sql.Date.valueOf(c.getEndDate()));

    spnMaxStudent.setValue(c.getMaxStudent());
    cboStatus.setSelectedItem(c.getStatus());
}
    public boolean isSaved(){
        return saved;
    }

    public Class getClazz(){
        return clazz;
    }
}