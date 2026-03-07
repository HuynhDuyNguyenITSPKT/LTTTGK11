package com.languagecenter;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.init.AppInitializer;
import com.languagecenter.repo.*;
import com.languagecenter.repo.jpa.*;
import com.languagecenter.service.*;
import com.languagecenter.ui.LoginFrame;
import com.languagecenter.ui.UI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        UI.initLookAndFeel();

        TransactionManager tx = new TransactionManager();

        StudentRepository studentRepo = new JpaStudentRepository();

        TeacherRepository teacherRepo = new JpaTeacherRepository();

        CourseRepository courseRepo = new JpaCourseRepository();

        RoomRepository roomRepo = new JpaRoomRepository();

        ClassRepository classRepo = new JpaClassRepository();

        UserAccountRepository userRepo = new JpaUserAccountRepository();

        ScheduleRepository scheduleRepo = new JpaScheduleRepository();

        EnrollmentRepository enrollmentRepo = new JpaEnrollmentRepository();

        StudentService studentService = new StudentService(studentRepo,userRepo,tx);

        TeacherService teacherService = new TeacherService(teacherRepo,userRepo,tx);

        CourseService courseService = new CourseService(courseRepo,tx);

        RoomService roomService = new RoomService(roomRepo,tx);

        ClassService classService = new ClassService(classRepo,tx);

        AuthService authService = new AuthService(userRepo,tx);

        EnrollmentService enrollmentService = new EnrollmentService(enrollmentRepo,tx);

        ScheduleService scheduleService = new ScheduleService(scheduleRepo,tx);

        try {
            // tạo admin nếu chưa có
            AppInitializer.initAdmin(tx,userRepo);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame login =
                    new LoginFrame(authService,studentService,teacherService,courseService,roomService,classService,scheduleService,enrollmentService);
            login.setVisible(true);
        });
    }
}