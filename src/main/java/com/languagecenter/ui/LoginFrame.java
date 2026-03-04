package com.languagecenter.ui;

import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.UserRole;
import com.languagecenter.service.AuthService;
import com.languagecenter.service.StudentService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField txtUser = new JTextField(15);
    private final JPasswordField txtPass = new JPasswordField(15);

    private final AuthService authService;
    private final StudentService studentService;

    public LoginFrame(AuthService authService,
                      StudentService studentService){

        super("Login");

        this.authService = authService;
        this.studentService = studentService;

        buildUI();

        setSize(300,180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildUI(){

        JPanel form = new JPanel(new GridLayout(2,2));

        form.add(new JLabel("Username"));
        form.add(txtUser);

        form.add(new JLabel("Password"));
        form.add(txtPass);

        JButton btnLogin = new JButton("Login");

        btnLogin.addActionListener(e->login());

        setLayout(new BorderLayout());

        add(form,BorderLayout.CENTER);
        add(btnLogin,BorderLayout.SOUTH);
    }

    private void login(){

        try{

            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            UserAccount acc =
                    authService.login(user,pass);

            dispose();

            if(acc.getRole()== UserRole.Admin){

                new MainFrame(studentService)
                        .setVisible(true);

            }else{

                JOptionPane.showMessageDialog(
                        this,
                        "Only admin UI implemented"
                );
            }

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}