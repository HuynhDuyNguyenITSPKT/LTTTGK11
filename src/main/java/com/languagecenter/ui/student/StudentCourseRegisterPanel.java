package com.languagecenter.ui.student;

import com.languagecenter.model.Class;
import com.languagecenter.model.Course;
import com.languagecenter.model.Schedule;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentCourseRegisterPanel extends JPanel {

    private final Long studentId;

    private final CourseService courseService;
    private final ClassService classService;
    private final ScheduleService scheduleService;
    private final EnrollmentService enrollmentService;

    private JTable courseTable;
    private JTable classTable;
    private JTable scheduleTable;

    private List<Course> courseData;
    private List<Class> classData;

    public StudentCourseRegisterPanel(
            Long studentId,
            CourseService courseService,
            ClassService classService,
            ScheduleService scheduleService,
            EnrollmentService enrollmentService) {

        this.studentId = studentId;
        this.courseService = courseService;
        this.classService = classService;
        this.scheduleService = scheduleService;
        this.enrollmentService = enrollmentService;

        setLayout(new BorderLayout());
        setBackground(new Color(245,245,245));

        buildUI();

        loadCourses();
    }

    private void buildUI(){

        JLabel title = new JLabel("ĐĂNG KÝ KHÓA HỌC");
        title.setFont(new Font("Segoe UI",Font.BOLD,22));
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

        add(title,BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1,3,10,10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        courseTable = new JTable();
        classTable = new JTable();
        scheduleTable = new JTable();

        centerPanel.add(createPanel("Courses",courseTable));
        centerPanel.add(createPanel("Classes",classTable));
        centerPanel.add(createPanel("Schedule",scheduleTable));

        add(centerPanel,BorderLayout.CENTER);

        JButton btnRegister = new JButton("Đăng ký lớp");
        btnRegister.setPreferredSize(new Dimension(200,40));
        btnRegister.setBackground(new Color(76,175,80));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Segoe UI",Font.BOLD,14));

        JPanel bottom = new JPanel();
        bottom.add(btnRegister);

        add(bottom,BorderLayout.SOUTH);

        courseTable.getSelectionModel().addListSelectionListener(e -> loadClasses());
        classTable.getSelectionModel().addListSelectionListener(e -> loadSchedules());

        btnRegister.addActionListener(e -> register());
    }

    private JPanel createPanel(String title, JTable table){

        JPanel panel = new JPanel(new BorderLayout());

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI",Font.BOLD,14));
        lbl.setHorizontalAlignment(JLabel.CENTER);
        lbl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        table.setRowHeight(28);

        panel.add(lbl,BorderLayout.NORTH);
        panel.add(new JScrollPane(table),BorderLayout.CENTER);

        return panel;
    }

    private void loadCourses(){

        try{

            courseData = courseService.getAll();

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID","Course","Level","Fee"},0
            );

            for(Course c : courseData){

                model.addRow(new Object[]{
                        c.getId(),
                        c.getCourseName(),
                        c.getLevel(),
                        c.getFee()
                });
            }

            courseTable.setModel(model);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadClasses(){

        int row = courseTable.getSelectedRow();

        if(row < 0) return;

        Course course = courseData.get(row);

        try{

            classData = classService.getByCourse(course.getId());

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID","Class","Teacher","Start","End","Max"},0
            );

            for(Class c : classData){

                model.addRow(new Object[]{
                        c.getId(),
                        c.getClassName(),
                        c.getTeacher() != null ? c.getTeacher().getFullName() : "",
                        c.getStartDate(),
                        c.getEndDate(),
                        c.getMaxStudent()
                });
            }

            classTable.setModel(model);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadSchedules(){

        int row = classTable.getSelectedRow();

        if(row < 0) return;

        Class clazz = classData.get(row);

        try{

            List<Schedule> schedules =
                    scheduleService.getByClass(clazz.getId());

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Date","Start","End","Room"},0
            );

            for(Schedule s : schedules){

                model.addRow(new Object[]{
                        s.getStudyDate(),
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getRoom().getRoomName()
                });
            }

            scheduleTable.setModel(model);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void register(){

        int row = classTable.getSelectedRow();

        if(row < 0){

            JOptionPane.showMessageDialog(this,"Vui lòng chọn lớp!");
            return;
        }

        Class clazz = classData.get(row);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn muốn đăng ký lớp: "+clazz.getClassName()+" ?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        try{

            enrollmentService.register(studentId, clazz);

            JOptionPane.showMessageDialog(this,"Đăng ký thành công!");

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());
        }
    }
}