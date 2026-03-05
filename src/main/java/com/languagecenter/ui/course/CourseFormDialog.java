package com.languagecenter.ui.course;

import com.languagecenter.model.Course;
import com.languagecenter.model.enums.*;

import javax.swing.*;
import java.awt.*;

public class CourseFormDialog extends JDialog {

    private final JTextField txtName = new JTextField(25);
    private final JTextArea txtDescription = new JTextArea(3,25);
    private final JTextField txtDuration = new JTextField(10);
    private final JTextField txtFee = new JTextField(10);

    private final JComboBox<CourseLevel> cboLevel =
            new JComboBox<>(CourseLevel.values());

    private final JComboBox<DurationUnit> cboDurationUnit =
            new JComboBox<>(DurationUnit.values());

    private final JComboBox<CourseStatus> cboStatus =
            new JComboBox<>(CourseStatus.values());

    private boolean saved=false;

    private Course course;

    public CourseFormDialog(Frame owner,String title,Course existing){

        super(owner,title,true);

        buildUI();

        if(existing!=null){

            course = existing;

            txtName.setText(existing.getCourseName());
            txtDescription.setText(existing.getDescription());
            txtDuration.setText(String.valueOf(existing.getDuration()));
            txtFee.setText(String.valueOf(existing.getFee()));

            cboLevel.setSelectedItem(existing.getLevel());
            cboDurationUnit.setSelectedItem(existing.getDurationUnit());
            cboStatus.setSelectedItem(existing.getStatus());

        }else{

            course = new Course();
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI(){

        JPanel form = new JPanel(new GridLayout(0,2,10,10));

        form.add(new JLabel("Course Name"));
        form.add(txtName);

        form.add(new JLabel("Description"));
        form.add(new JScrollPane(txtDescription));

        form.add(new JLabel("Level"));
        form.add(cboLevel);

        form.add(new JLabel("Duration"));
        form.add(txtDuration);

        form.add(new JLabel("Duration Unit"));
        form.add(cboDurationUnit);

        form.add(new JLabel("Fee"));
        form.add(txtFee);

        form.add(new JLabel("Status"));
        form.add(cboStatus);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e->onSave());
        btnCancel.addActionListener(e->dispose());

        JPanel actions = new JPanel();
        actions.add(btnSave);
        actions.add(btnCancel);

        setLayout(new BorderLayout());

        add(form,BorderLayout.CENTER);
        add(actions,BorderLayout.SOUTH);
    }

    private void onSave(){

        try{

            course.setCourseName(txtName.getText());
            course.setDescription(txtDescription.getText());
            course.setLevel((CourseLevel)cboLevel.getSelectedItem());
            course.setDuration(Integer.parseInt(txtDuration.getText()));
            course.setDurationUnit((DurationUnit)cboDurationUnit.getSelectedItem());
            course.setFee(Double.parseDouble(txtFee.getText()));
            course.setStatus((CourseStatus)cboStatus.getSelectedItem());

            saved=true;

            dispose();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public boolean isSaved(){
        return saved;
    }

    public Course getCourse(){
        return course;
    }
}