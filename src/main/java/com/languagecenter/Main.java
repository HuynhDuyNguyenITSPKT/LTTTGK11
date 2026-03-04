package com.languagecenter;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.init.AppInitializer;
import com.languagecenter.repo.StudentRepository;
import com.languagecenter.repo.UserAccountRepository;
import com.languagecenter.repo.jpa.JpaStudentRepository;
import com.languagecenter.repo.jpa.JpaUserAccountRepository;
import com.languagecenter.service.AuthService;
import com.languagecenter.service.StudentService;
import com.languagecenter.ui.LoginFrame;
import com.languagecenter.ui.UI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        UI.initLookAndFeel();

        TransactionManager tx = new TransactionManager();

        StudentRepository studentRepo =
                new JpaStudentRepository();

        UserAccountRepository userRepo =
                new JpaUserAccountRepository();

        StudentService studentService =
                new StudentService(studentRepo,userRepo,tx);

        AuthService authService =
                new AuthService(userRepo,tx);

        try {
            // tạo admin nếu chưa có
            AppInitializer.initAdmin(tx,userRepo);

        }catch(Exception ex){
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {

            LoginFrame login =
                    new LoginFrame(authService,studentService);

            login.setVisible(true);
        });
    }
}