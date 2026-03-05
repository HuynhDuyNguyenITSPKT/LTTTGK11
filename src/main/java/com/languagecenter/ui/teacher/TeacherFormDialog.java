package com.languagecenter.ui.teacher;

import com.languagecenter.model.Teacher;
import com.languagecenter.model.enums.TeacherStatus;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class TeacherFormDialog extends JDialog {

    private final JTextField txtName = new JTextField(25);
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtSpecialty = new JTextField(20);
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);

    private final JComboBox<TeacherStatus> cboStatus =
            new JComboBox<>(TeacherStatus.values());

    private boolean saved=false;

    private Teacher teacher;

    public TeacherFormDialog(Frame owner,
                             String title,
                             Teacher existing,
                             String username){

        super(owner,title,true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        buildUI();

        if(existing!=null){

            txtName.setText(existing.getFullName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            txtSpecialty.setText(existing.getSpecialty());

            cboStatus.setSelectedItem(existing.getStatus());

            txtUsername.setText(username);

            teacher = existing;

        }else{

            teacher = new Teacher();
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
        form.add(new JLabel("Specialty"),g);
        g.gridx=1;
        form.add(txtSpecialty,g);

        r++;
        g.gridx=0; g.gridy=r;
        form.add(new JLabel("Status"),g);
        g.gridx=1;
        form.add(cboStatus,g);

        r++;
        g.gridx=0; g.gridy=r;
        form.add(new JLabel("Username"),g);
        g.gridx=1;
        form.add(txtUsername,g);

        r++;
        g.gridx=0; g.gridy=r;
        form.add(new JLabel("Password"),g);
        g.gridx=1;
        form.add(txtPassword,g);

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
            String username = txtUsername.getText().trim();

            if(username.isEmpty())
                throw new IllegalArgumentException("Username required");

            String password = new String(txtPassword.getPassword());

            if(teacher.getId() == null && password.isBlank())
                throw new IllegalArgumentException("Password required");

            teacher.setFullName(name);
            teacher.setPhone(txtPhone.getText());
            teacher.setEmail(txtEmail.getText());
            teacher.setSpecialty(txtSpecialty.getText());
            teacher.setStatus((TeacherStatus)cboStatus.getSelectedItem());
            teacher.setHireDate(LocalDate.now());

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

    public Teacher getTeacher(){
        return teacher;
    }

    public String getUsername(){
        return txtUsername.getText();
    }

    public String getPassword(){
        return new String(txtPassword.getPassword());
    }
}