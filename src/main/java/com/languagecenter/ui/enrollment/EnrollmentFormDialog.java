package com.languagecenter.ui.enrollment;

import com.languagecenter.model.Class;
import com.languagecenter.model.Enrollment;
import com.languagecenter.model.Student;
import com.languagecenter.model.enums.EnrollmentStatus;
import com.languagecenter.model.enums.ResultStatus;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class EnrollmentFormDialog extends JDialog {

    private final JComboBox<Student> cboStudent = new JComboBox<>();
    private final JComboBox<Class> cboClass = new JComboBox<>();

    private final JComboBox<EnrollmentStatus> cboStatus =
            new JComboBox<>(EnrollmentStatus.values());

    private final JComboBox<ResultStatus> cboResult =
            new JComboBox<>(ResultStatus.values());

    private final JTextField txtDate = new JTextField();

    private boolean saved = false;
    private final Enrollment enrollment;

    public EnrollmentFormDialog(Frame owner,
                                String title,
                                Enrollment existing,
                                List<Student> students,
                                List<Class> classes){

        super(owner,title,true);

        this.enrollment = existing != null ? existing : new Enrollment();

        students.forEach(cboStudent::addItem);
        classes.forEach(cboClass::addItem);

        buildUI();

        if(existing != null){

            cboStudent.setSelectedItem(existing.getStudent());
            cboClass.setSelectedItem(existing.getClassEntity());

            txtDate.setText(existing.getEnrollmentDate().toString());

            cboStatus.setSelectedItem(existing.getStatus());
            cboResult.setSelectedItem(existing.getResult());
        }

        setSize(400,320);
        setLocationRelativeTo(owner);
    }

    private void buildUI(){

        setLayout(new GridLayout(6,2,10,10));

        ((JComponent)getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10,10,10,10)
        );

        add(new JLabel("Student"));
        add(cboStudent);

        add(new JLabel("Class"));
        add(cboClass);

        add(new JLabel("Enrollment Date (YYYY-MM-DD)"));
        add(txtDate);

        add(new JLabel("Status"));
        add(cboStatus);

        add(new JLabel("Result"));
        add(cboResult);

        JButton btnSave = new JButton("Save");

        btnSave.addActionListener(e -> {

            try{

                enrollment.setStudent((Student) cboStudent.getSelectedItem());
                enrollment.setClassEntity((Class) cboClass.getSelectedItem());

                enrollment.setEnrollmentDate(
                        LocalDate.parse(txtDate.getText().trim())
                );

                enrollment.setStatus(
                        (EnrollmentStatus) cboStatus.getSelectedItem()
                );

                enrollment.setResult(
                        (ResultStatus) cboResult.getSelectedItem()
                );

                saved = true;
                dispose();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(
                        this,
                        "Sai định dạng ngày!\nYYYY-MM-DD",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        add(new JLabel());
        add(btnSave);
    }

    public boolean isSaved(){
        return saved;
    }

    public Enrollment getEnrollment(){
        return enrollment;
    }
}