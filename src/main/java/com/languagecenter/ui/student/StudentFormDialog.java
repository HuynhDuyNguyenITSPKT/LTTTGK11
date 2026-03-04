package com.languagecenter.ui.student;

import com.languagecenter.model.Student;
import com.languagecenter.model.enums.Gender;
import com.languagecenter.model.enums.StudentStatus;

import javax.swing.*;
import java.awt.*;

public class StudentFormDialog extends JDialog {

    private final JTextField txtName = new JTextField(25);
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtEmail = new JTextField(20);

    private final JComboBox<Gender> cboGender =
            new JComboBox<>(Gender.values());

    private final JComboBox<StudentStatus> cboStatus =
            new JComboBox<>(StudentStatus.values());

    private boolean saved=false;

    private Student student;

    public StudentFormDialog(Frame owner,String title,Student existing){

        super(owner,title,true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        buildUI();

        if(existing!=null){

            txtName.setText(existing.getFullName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());

            cboGender.setSelectedItem(existing.getGender());
            cboStatus.setSelectedItem(existing.getStatus());

            student = existing;

        }else{

            student = new Student();
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI(){

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();

        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;

        int r=0;

        g.gridx=0; g.gridy=r;
        form.add(new JLabel("Name"),g);

        g.gridx=1;
        form.add(txtName,g);

        r++;

        g.gridx=0; g.gridy=r;
        form.add(new JLabel("Phone"),g);

        g.gridx=1;
        form.add(txtPhone,g);

        r++;

        g.gridx=0; g.gridy=r;
        form.add(new JLabel("Email"),g);

        g.gridx=1;
        form.add(txtEmail,g);

        r++;

        g.gridx=0; g.gridy=r;
        form.add(new JLabel("Gender"),g);

        g.gridx=1;
        form.add(cboGender,g);

        r++;

        g.gridx=0; g.gridy=r;
        form.add(new JLabel("Status"),g);

        g.gridx=1;
        form.add(cboStatus,g);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e->onSave());
        btnCancel.addActionListener(e->dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnSave);
        actions.add(btnCancel);

        getContentPane().setLayout(new BorderLayout(10,10));
        getContentPane().add(form,BorderLayout.CENTER);
        getContentPane().add(actions,BorderLayout.SOUTH);
    }

    private void onSave(){

        try{

            String name = txtName.getText().trim();

            if(name.isEmpty())
                throw new IllegalArgumentException("Name required");

            student.setFullName(name);
            student.setPhone(txtPhone.getText());
            student.setEmail(txtEmail.getText());
            student.setGender((Gender)cboGender.getSelectedItem());
            student.setStatus((StudentStatus)cboStatus.getSelectedItem());

            saved=true;
            dispose();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Validation",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public boolean isSaved(){
        return saved;
    }

    public Student getStudent(){
        return student;
    }
}