package com.languagecenter.ui.student;

import com.languagecenter.model.Class;
import com.languagecenter.model.Course;
import com.languagecenter.model.Schedule;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class StudentCourseRegisterPanel extends JPanel {

    private final Long studentId;

    private final CourseService courseService;
    private final ClassService classService;
    private final ScheduleService scheduleService;
    private final EnrollmentService enrollmentService;

    private JPanel courseContainer;
    private JTextField txtSearch;

    private List<Course> courseData;

    private final Color PRIMARY = new Color(79,70,229);
    private final Color BACKGROUND = new Color(245,247,251);
    private final Color BORDER = new Color(229,231,235);
    private final Color CARD_BG = new Color(220,252,231);

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
        setBackground(BACKGROUND);

        buildUI();
        loadCourses();
    }

    public void reload() {
        loadCourses();
    }

    private void buildUI(){

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND);
        header.setBorder(new EmptyBorder(20,20,10,20));

        JLabel title = new JLabel("COURSE REGISTRATION");
        title.setFont(new Font("Segoe UI",Font.BOLD,26));

        JPanel right = new JPanel();
        right.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200,32));
        txtSearch.addActionListener(e -> filterCourses());

        JButton btnSearch = createPrimaryButton("Search");
        btnSearch.addActionListener(e -> filterCourses());

        right.add(txtSearch);
        right.add(btnSearch);

        header.add(title,BorderLayout.WEST);
        header.add(right,BorderLayout.EAST);

        add(header,BorderLayout.NORTH);

        // container hiển thị course
        courseContainer = new JPanel(new FlowLayout(FlowLayout.LEFT,20,20));
        courseContainer.setBackground(BACKGROUND);
        courseContainer.setBorder(new EmptyBorder(20,20,20,20));

        courseContainer.setPreferredSize(new Dimension(920, 1000));

        JScrollPane scroll = new JScrollPane(courseContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll,BorderLayout.CENTER);
    }

    private JButton createPrimaryButton(String text){

        JButton btn = new JButton(text);

        btn.setFocusPainted(false);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI",Font.BOLD,13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));

        btn.addMouseListener(new MouseAdapter(){

            public void mouseEntered(MouseEvent e){
                btn.setBackground(PRIMARY.darker());
            }

            public void mouseExited(MouseEvent e){
                btn.setBackground(PRIMARY);
            }

        });

        return btn;
    }

    private void loadCourses(){

        try{

            courseData = courseService.getAll();
            renderCourses(courseData);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void renderCourses(List<Course> list){

        courseContainer.removeAll();

        list.forEach(c ->
                courseContainer.add(createCourseCard(c))
        );

        courseContainer.revalidate();
        courseContainer.repaint();
    }

    private JPanel createCourseCard(Course c){

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15,15,15,15)
        ));

        // kích thước cố định
        card.setPreferredSize(new Dimension(200,160));
        card.setMinimumSize(new Dimension(200,160));
        card.setMaximumSize(new Dimension(200,160));

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel name = new JLabel(c.getCourseName());
        name.setFont(new Font("Segoe UI",Font.BOLD,16));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel level = new JLabel("Level: " + c.getLevel());
        level.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel fee = new JLabel("Fee: " + c.getFee());
        fee.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea desc = new JTextArea(c.getDescription());
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setFont(new Font("Segoe UI",Font.PLAIN,12));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnView = createPrimaryButton("View Classes");
        btnView.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnView.addActionListener(e -> showClasses(c));

        card.add(name);
        card.add(Box.createVerticalStrut(5));
        card.add(level);
        card.add(fee);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);
        card.add(Box.createVerticalGlue());
        card.add(btnView);

        card.addMouseListener(new MouseAdapter(){

            public void mouseEntered(MouseEvent e){
                card.setBorder(BorderFactory.createLineBorder(PRIMARY,2));
            }

            public void mouseExited(MouseEvent e){
                card.setBorder(BorderFactory.createLineBorder(BORDER));
            }

        });

        return card;
    }

    private void filterCourses(){

        String keyword = txtSearch.getText().toLowerCase();

        List<Course> filtered =
                courseData.stream()
                        .filter(c ->
                                c.getCourseName()
                                        .toLowerCase()
                                        .contains(keyword))
                        .collect(Collectors.toList());

        renderCourses(filtered);
    }

    private void showClasses(Course course){

        try{

            List<Class> classes =
                    classService.getByCourse(course.getId());

            JDialog dialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Class List",true);

            dialog.setSize(450,420);
            dialog.setLocationRelativeTo(this);

            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
            container.setBackground(BACKGROUND);
            container.setBorder(new EmptyBorder(15,15,15,15));

            classes.forEach(c ->
                    container.add(createClassCard(c))
            );

            dialog.add(new JScrollPane(container));
            dialog.setVisible(true);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private JPanel createClassCard(Class c){

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));

        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15,15,15,15)
        ));

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,150));

        JLabel name = new JLabel(c.getClassName());
        name.setFont(new Font("Segoe UI",Font.BOLD,16));

        JLabel teacher = new JLabel(
                "Teacher: " +
                        (c.getTeacher()!=null ?
                                c.getTeacher().getFullName() : "")
        );

        JLabel date = new JLabel(
                c.getStartDate()+" - "+c.getEndDate()
        );

        JLabel max = new JLabel("Max: "+c.getMaxStudent());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton btnSchedule = createPrimaryButton("View Schedule");
        JButton btnRegister = createPrimaryButton("Register");

        btnSchedule.addActionListener(e -> showSchedules(c));
        btnRegister.addActionListener(e -> register(c));

        btnPanel.add(btnSchedule);
        btnPanel.add(btnRegister);

        card.add(name);
        card.add(Box.createVerticalStrut(5));
        card.add(teacher);
        card.add(date);
        card.add(max);
        card.add(Box.createVerticalStrut(10));
        card.add(btnPanel);

        return card;
    }

    private void showSchedules(Class clazz){

        try{

            List<Schedule> schedules =
                    scheduleService.getByClass(clazz.getId());

            JDialog dialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Schedule",true);

            dialog.setSize(380,350);
            dialog.setLocationRelativeTo(this);

            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
            container.setBackground(BACKGROUND);
            container.setBorder(new EmptyBorder(15,15,15,15));

            schedules.forEach(s ->
                    container.add(createScheduleCard(s))
            );

            dialog.add(new JScrollPane(container));
            dialog.setVisible(true);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private JPanel createScheduleCard(Schedule s){

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));

        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12,12,12,12)
        ));

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,80));

        JLabel date = new JLabel("Date: "+s.getStudyDate());
        JLabel time = new JLabel(s.getStartTime()+" - "+s.getEndTime());
        JLabel room = new JLabel("Room: "+s.getRoom().getRoomName());

        card.add(date);
        card.add(time);
        card.add(room);

        return card;
    }

    private void register(Class clazz){

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Register for class: "+clazz.getClassName()+" ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        try{

            enrollmentService.register(studentId, clazz);
            JOptionPane.showMessageDialog(this,"Registration successful!");

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());
        }
    }
}