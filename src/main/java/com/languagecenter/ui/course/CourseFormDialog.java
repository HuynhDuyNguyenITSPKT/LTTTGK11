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

        setMinimumSize(new Dimension(420,420));
        setLocationRelativeTo(owner);

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
    }

    private void buildUI(){

        Font font = new Font("Segoe UI",Font.PLAIN,14);

        txtName.setFont(font);
        txtDescription.setFont(font);
        txtDuration.setFont(font);
        txtFee.setFont(font);

        cboLevel.setFont(font);
        cboDurationUnit.setFont(font);
        cboStatus.setFont(font);

        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row=0;

        addField(form,gbc,row++,"Course Name",txtName);
        addField(form,gbc,row++,"Description",new JScrollPane(txtDescription));
        addField(form,gbc,row++,"Level",cboLevel);
        addField(form,gbc,row++,"Duration",txtDuration);
        addField(form,gbc,row++,"Duration Unit",cboDurationUnit);
        addField(form,gbc,row++,"Fee",txtFee);
        addField(form,gbc,row++,"Status",cboStatus);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        btnSave.setFocusPainted(false);
        btnCancel.setFocusPainted(false);

        btnSave.setBackground(new Color(52,152,219));
        btnSave.setForeground(Color.WHITE);

        btnSave.setPreferredSize(new Dimension(110,35));
        btnCancel.setPreferredSize(new Dimension(110,35));

        btnSave.addActionListener(e->onSave());
        btnCancel.addActionListener(e->dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBorder(BorderFactory.createEmptyBorder(10,10,15,15));

        actions.add(btnSave);
        actions.add(btnCancel);

        setLayout(new BorderLayout());

        add(form,BorderLayout.CENTER);
        add(actions,BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp){

        gbc.gridx=0;
        gbc.gridy=row;
        gbc.weightx=0;

        JLabel lbl=new JLabel(label);
        lbl.setFont(new Font("Segoe UI",Font.BOLD,14));

        panel.add(lbl,gbc);

        gbc.gridx=1;
        gbc.weightx=1;

        panel.add(comp,gbc);
    }

    private void onSave(){

        try{

            if(txtName.getText().trim().isEmpty()){
                throw new RuntimeException("Course name is required");
            }

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