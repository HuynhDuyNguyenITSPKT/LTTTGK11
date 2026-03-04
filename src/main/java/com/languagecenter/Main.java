package com.languagecenter;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.repo.StudentRepository;
import com.languagecenter.repo.jpa.JpaStudentRepository;
import com.languagecenter.service.StudentService;
import com.languagecenter.ui.UI;
import com.languagecenter.ui.student.StudentFrame;

import javax.swing.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        UI.initLookAndFeel();

        TransactionManager tx = new TransactionManager();

        StudentRepository studentRepo =
                new JpaStudentRepository();

        StudentService studentService =
                new StudentService(studentRepo, tx);

        SwingUtilities.invokeLater(() -> {

            StudentFrame frame =
                    new StudentFrame(studentService);

            frame.setVisible(true);
        });

    }
}