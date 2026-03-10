package com.languagecenter.ui.student;

import com.languagecenter.model.Class;
import com.languagecenter.model.Course;
import com.languagecenter.model.Schedule;
import com.languagecenter.model.enums.ClassStatus;
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
    private final Color BACKGROUND = new Color(248,250,252);
    private final Color BORDER = new Color(226,232,240);
    private final Color CARD_BG = new Color(255,255,255);
    private final Color CARD_HOVER = new Color(241,245,249);
    private final Color TEXT_PRIMARY = new Color(15,23,42);
    private final Color TEXT_SECONDARY = new Color(100,116,139);

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
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,BORDER),
                new EmptyBorder(25,30,25,30)
        ));

        JLabel title = new JLabel("COURSE REGISTRATION");
        title.setFont(new Font("Segoe UI",Font.BOLD,28));
        title.setForeground(TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        right.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(250,38));
        txtSearch.setFont(new Font("Segoe UI",Font.PLAIN,14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER,1,true),
                new EmptyBorder(5,12,5,12)
        ));
        txtSearch.addActionListener(e -> filterCourses());

        JButton btnSearch = createPrimaryButton("Search");
        btnSearch.setPreferredSize(new Dimension(100,38));
        btnSearch.addActionListener(e -> filterCourses());

        right.add(txtSearch);
        right.add(btnSearch);

        header.add(title,BorderLayout.WEST);
        header.add(right,BorderLayout.EAST);

        add(header,BorderLayout.NORTH);

        // container hiển thị course
        courseContainer = new JPanel(new FlowLayout(FlowLayout.LEFT,25,25));
        courseContainer.setBackground(BACKGROUND);
        courseContainer.setBorder(new EmptyBorder(30,30,30,30));

        courseContainer.setPreferredSize(new Dimension(920, 1000));

        JScrollPane scroll = new JScrollPane(courseContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(BACKGROUND);

        add(scroll,BorderLayout.CENTER);
    }

    private JButton createPrimaryButton(String text){

        JButton btn = new JButton(text);

        btn.setFocusPainted(false);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI",Font.BOLD,13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY,0,true),
                new EmptyBorder(10,20,10,20)
        ));

        btn.addMouseListener(new MouseAdapter(){

            public void mouseEntered(MouseEvent e){
                btn.setBackground(new Color(67,56,202));
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
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER,1,true),
                        BorderFactory.createEmptyBorder(2,2,4,2)
                ),
                new EmptyBorder(20,18,20,18)
        ));

        // kích thước cố định
        card.setPreferredSize(new Dimension(260,200));
        card.setMinimumSize(new Dimension(260,200));
        card.setMaximumSize(new Dimension(260,200));

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel name = new JLabel("<html><center>" + c.getCourseName() + "</center></html>");
        name.setFont(new Font("Segoe UI",Font.BOLD,17));
        name.setForeground(TEXT_PRIMARY);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,15,5));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel level = new JLabel("Level: " + c.getLevel());
        level.setFont(new Font("Segoe UI",Font.PLAIN,13));
        level.setForeground(TEXT_SECONDARY);

        JLabel fee = new JLabel(String.format("%,d VNĐ", c.getFee().longValue()));
        fee.setFont(new Font("Segoe UI",Font.BOLD,13));
        fee.setForeground(new Color(34,197,94));

        infoPanel.add(level);
        infoPanel.add(new JLabel("|"));
        infoPanel.add(fee);

        JTextArea desc = new JTextArea(c.getDescription());
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setFont(new Font("Segoe UI",Font.PLAIN,12));
        desc.setForeground(TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnView = createPrimaryButton("View Classes");
        btnView.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnView.addActionListener(e -> showClasses(c));

        card.add(name);
        card.add(Box.createVerticalStrut(10));
        card.add(infoPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(desc);
        card.add(Box.createVerticalGlue());
        card.add(btnView);

        card.addMouseListener(new MouseAdapter(){

            public void mouseEntered(MouseEvent e){
                card.setBackground(CARD_HOVER);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(PRIMARY,2,true),
                                BorderFactory.createEmptyBorder(2,2,4,2)
                        ),
                        new EmptyBorder(20,18,20,18)
                ));
            }

            public void mouseExited(MouseEvent e){
                card.setBackground(CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER,1,true),
                                BorderFactory.createEmptyBorder(2,2,4,2)
                        ),
                        new EmptyBorder(20,18,20,18)
                ));
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
                    course.getCourseName() + " - Available Classes",true);

            dialog.setSize(550,500);
            dialog.setLocationRelativeTo(this);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(BACKGROUND);

            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
            container.setBackground(BACKGROUND);
            container.setBorder(new EmptyBorder(20,20,20,20));

            if(classes.isEmpty()){
                JLabel emptyLabel = new JLabel("No classes available");
                emptyLabel.setFont(new Font("Segoe UI",Font.PLAIN,14));
                emptyLabel.setForeground(TEXT_SECONDARY);
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.add(Box.createVerticalStrut(50));
                container.add(emptyLabel);
            } else {
                classes.forEach(c -> {
                    container.add(createClassCard(c));
                    container.add(Box.createVerticalStrut(15));
                });
            }

            JScrollPane scroll = new JScrollPane(container);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.getViewport().setBackground(BACKGROUND);

            mainPanel.add(scroll,BorderLayout.CENTER);
            dialog.add(mainPanel);
            dialog.setVisible(true);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private JPanel createClassCard(Class c){

        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(15,0));

        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER,1,true),
                new EmptyBorder(18,20,18,20)
        ));

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,160));

        // Left panel - Class info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel,BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        headerPanel.setOpaque(false);

        JLabel name = new JLabel(c.getClassName());
        name.setFont(new Font("Segoe UI",Font.BOLD,17));
        name.setForeground(TEXT_PRIMARY);

        JLabel statusBadge = createStatusBadge(c.getStatus());

        headerPanel.add(name);
        headerPanel.add(statusBadge);

        JLabel teacher = new JLabel(
                "Teacher: " +
                        (c.getTeacher()!=null ?
                                c.getTeacher().getFullName() : "TBA")
        );
        teacher.setFont(new Font("Segoe UI",Font.PLAIN,13));
        teacher.setForeground(TEXT_SECONDARY);

        JLabel date = new JLabel(
                c.getStartDate()+" - "+c.getEndDate()
        );
        date.setFont(new Font("Segoe UI",Font.PLAIN,13));
        date.setForeground(TEXT_SECONDARY);

        JLabel max = new JLabel("Max Students: "+c.getMaxStudent());
        max.setFont(new Font("Segoe UI",Font.PLAIN,13));
        max.setForeground(TEXT_SECONDARY);

        leftPanel.add(headerPanel);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(teacher);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(date);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(max);

        // Right panel - Buttons
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel,BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);

        JButton btnSchedule = createPrimaryButton("View Schedule");
        btnSchedule.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSchedule.addActionListener(e -> showSchedules(c));

        JButton btnRegister = createSecondaryButton("Register");
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> register(c));

        btnPanel.add(Box.createVerticalGlue());
        btnPanel.add(btnSchedule);
        btnPanel.add(Box.createVerticalStrut(10));
        btnPanel.add(btnRegister);
        btnPanel.add(Box.createVerticalGlue());

        card.add(leftPanel,BorderLayout.CENTER);
        card.add(btnPanel,BorderLayout.EAST);

        return card;
    }

    private JButton createSecondaryButton(String text){
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(34,197,94));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI",Font.BOLD,13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(34,197,94),0,true),
                new EmptyBorder(10,20,10,20)
        ));

        btn.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){
                btn.setBackground(new Color(22,163,74));
            }
            public void mouseExited(MouseEvent e){
                btn.setBackground(new Color(34,197,94));
            }
        });

        return btn;
    }

    private JLabel createStatusBadge(ClassStatus status){
        String statusText = status != null ? status.toString() : "Unknown";
        String icon = getStatusIcon(status);
        JLabel badge = new JLabel(icon + " " + statusText);
        badge.setFont(new Font("Segoe UI",Font.BOLD,11));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(getStatusColor(status));
        badge.setBorder(new EmptyBorder(4,10,4,10));
        return badge;
    }

    private String getStatusIcon(ClassStatus status){
        if(status == null) return "[?]";
        switch (status){
            case Planned: return "[P]";
            case Open: return "[O]";
            case Ongoing: return "[~]";
            case Completed: return "[*]";
            case Cancelled: return "[X]";
        }
        return "[?]";
    }

    private void showSchedules(Class clazz){

        try{

            List<Schedule> schedules =
                    scheduleService.getByClass(clazz.getId());

            JDialog dialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    clazz.getClassName() + " - Class Schedule",true);

            dialog.setSize(450,400);
            dialog.setLocationRelativeTo(this);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(BACKGROUND);

            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
            container.setBackground(BACKGROUND);
            container.setBorder(new EmptyBorder(20,20,20,20));

            if(schedules.isEmpty()){
                JLabel emptyLabel = new JLabel("No schedule available");
                emptyLabel.setFont(new Font("Segoe UI",Font.PLAIN,14));
                emptyLabel.setForeground(TEXT_SECONDARY);
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.add(Box.createVerticalStrut(50));
                container.add(emptyLabel);
            } else {
                schedules.forEach(s -> {
                    container.add(createScheduleCard(s));
                    container.add(Box.createVerticalStrut(12));
                });
            }

            JScrollPane scroll = new JScrollPane(container);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.getViewport().setBackground(BACKGROUND);

            mainPanel.add(scroll,BorderLayout.CENTER);
            dialog.add(mainPanel);
            dialog.setVisible(true);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private JPanel createScheduleCard(Schedule s){

        JPanel card = new JPanel(new GridLayout(3,1,5,5));

        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER,1,true),
                new EmptyBorder(15,18,15,18)
        ));

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,95));

        JLabel date = new JLabel("Date: " + s.getStudyDate());
        date.setFont(new Font("Segoe UI",Font.BOLD,14));
        date.setForeground(TEXT_PRIMARY);

        JLabel time = new JLabel("Time: " + s.getStartTime()+" - "+s.getEndTime());
        time.setFont(new Font("Segoe UI",Font.PLAIN,13));
        time.setForeground(TEXT_SECONDARY);

        JLabel room = new JLabel("Room: " + s.getRoom().getRoomName());
        room.setFont(new Font("Segoe UI",Font.PLAIN,13));
        room.setForeground(TEXT_SECONDARY);

        card.add(date);
        card.add(time);
        card.add(room);

        return card;
    }

    private Color getStatusColor(ClassStatus status){

        if(status == null) return Color.GRAY;

        switch (status){

            case Planned:
                return new Color(59,130,246); // blue

            case Open:
                return new Color(34,197,94); // green

            case Ongoing:
                return new Color(168,85,247); // purple

            case Completed:
                return new Color(107,114,128); // gray

            case Cancelled:
                return new Color(239,68,68); // red
        }

        return Color.GRAY;
    }

    private void register(Class clazz){

        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 12));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "<html><div style='width:280px;padding:10px;'>" +
                        "<b style='font-size:14px;'>Confirm Registration</b><br/><br/>" +
                        "Are you sure you want to register for:<br/>" +
                        "<span style='color:#4f46e5;'><b>" + clazz.getClassName() + "</b></span>" +
                        "</div></html>",
                "Course Registration",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        try{

            enrollmentService.register(studentId, clazz);
            JOptionPane.showMessageDialog(
                    this,
                    "<html><div style='width:250px;padding:10px;text-align:center;'>" +
                            "<b>Registration Successful!</b><br/><br/>" +
                            "You have been enrolled in:<br/>" +
                            "<span style='color:#4f46e5;'>" + clazz.getClassName() + "</span>" +
                            "</div></html>",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    "<html><div style='width:280px;padding:10px;'>" +
                            "<b style='color:#ef4444;'>Registration Failed</b><br/><br/>" +
                            ex.getMessage() +
                            "</div></html>",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}