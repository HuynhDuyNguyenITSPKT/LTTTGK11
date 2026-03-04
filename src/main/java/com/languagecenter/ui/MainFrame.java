package com.languagecenter.ui;

import com.languagecenter.service.StudentService;
import com.languagecenter.ui.student.StudentPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame(StudentService studentService) {

        super("Language Center Management");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        tabs.add("Dashboard", new JPanel());

        tabs.add("Students",
                new StudentPanel(studentService));

        tabs.add("Teachers", new JPanel());

        tabs.add("Courses", new JPanel());

        tabs.add("Payments", new JPanel());

        setLayout(new BorderLayout());

        add(tabs,BorderLayout.CENTER);

        setSize(1000,600);
        setLocationRelativeTo(null);
    }
}