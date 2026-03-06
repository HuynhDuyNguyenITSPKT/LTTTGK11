package com.languagecenter.ui;

import com.languagecenter.service.*;
import com.languagecenter.ui.clazz.ClassPanel;
import com.languagecenter.ui.course.CoursePanel;
import com.languagecenter.ui.room.RoomPanel;
import com.languagecenter.ui.student.StudentPanel;
import com.languagecenter.ui.teacher.TeacherPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame(StudentService studentService, TeacherService teacherService, CourseService courseService, RoomService roomService, ClassService classService) {

        super("Language Center Management");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        tabs.add("Dashboard", new JPanel());

        tabs.add("Students",
                new StudentPanel(studentService));

        tabs.add("Teachers",
                new TeacherPanel(teacherService));

        tabs.add("Courses", new CoursePanel(courseService));

        tabs.add("Rooms", new RoomPanel(roomService));

        tabs.add("Classes", new ClassPanel(classService,courseService,teacherService,roomService));

        tabs.add("Payments", new JPanel());

        setLayout(new BorderLayout());

        add(tabs,BorderLayout.CENTER);

        setSize(1000,600);
        setLocationRelativeTo(null);
    }
}