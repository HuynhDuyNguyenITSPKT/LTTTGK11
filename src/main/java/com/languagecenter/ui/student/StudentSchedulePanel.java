package com.languagecenter.ui.student;

import com.languagecenter.model.Schedule;
import com.languagecenter.service.ScheduleService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StudentSchedulePanel extends JPanel {

    private final ScheduleService service;
    private final Long studentId;

    private List<Schedule> schedules = new ArrayList<>();

    private JPanel calendarPanel;
    private JLabel weekLabel;

    private LocalDate currentWeekStart;

    private final DateTimeFormatter dayFormatter =
            DateTimeFormatter.ofPattern("dd/MM");

    public StudentSchedulePanel(ScheduleService service, Long studentId) {

        this.service = service;
        this.studentId = studentId;

        setLayout(new BorderLayout());
        setBackground(new Color(245,247,250));

        LocalDate today = LocalDate.now();
        currentWeekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        buildToolbar();
        buildCalendar();

        loadData();
    }

    public void reload() {
        loadData();
    }

    private void buildToolbar(){

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        toolbar.setBackground(new Color(33,150,243));

        JLabel title = new JLabel("Weekly Schedule");
        title.setFont(new Font("Segoe UI",Font.BOLD,18));
        title.setForeground(Color.WHITE);

        JPanel nav = new JPanel();
        nav.setOpaque(false);

        JButton prev = new JButton("◀");
        JButton next = new JButton("▶");

        prev.addActionListener(e -> changeWeek(-1));
        next.addActionListener(e -> changeWeek(1));

        weekLabel = new JLabel();
        weekLabel.setForeground(Color.WHITE);
        weekLabel.setFont(new Font("Segoe UI",Font.BOLD,14));

        nav.add(prev);
        nav.add(next);
        nav.add(Box.createHorizontalStrut(10));
        nav.add(weekLabel);

        toolbar.add(title,BorderLayout.WEST);
        toolbar.add(nav,BorderLayout.EAST);

        add(toolbar,BorderLayout.NORTH);
    }

    private void buildCalendar(){

        calendarPanel = new JPanel(new GridLayout(1,7,10,10));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        calendarPanel.setBackground(new Color(245,247,250));

        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};

        for(int i=0;i<7;i++){

            JPanel dayPanel = new JPanel(new BorderLayout());
            dayPanel.setBackground(Color.WHITE);
            dayPanel.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));

            JLabel header = new JLabel("",SwingConstants.CENTER);
            header.setFont(new Font("Segoe UI",Font.BOLD,14));
            header.setBorder(BorderFactory.createEmptyBorder(8,0,8,0));

            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
            list.setBackground(Color.WHITE);

            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(10);

            dayPanel.add(header,BorderLayout.NORTH);
            dayPanel.add(scroll,BorderLayout.CENTER);

            calendarPanel.add(dayPanel);
        }

        add(calendarPanel,BorderLayout.CENTER);
    }

    private void loadData(){

        try{

            schedules = service.getScheduleByStudent(studentId);

            schedules.sort(Comparator.comparing(Schedule::getStartTime));

            renderSchedules();

        }catch(Exception e){

            JOptionPane.showMessageDialog(this,"Cannot load schedule");

        }
    }

    private void changeWeek(int step){

        currentWeekStart = currentWeekStart.plusWeeks(step);
        renderSchedules();
    }

    private void renderSchedules(){

        LocalDate endWeek = currentWeekStart.plusDays(6);

        weekLabel.setText(
                currentWeekStart.format(dayFormatter)
                        +" → "+
                        endWeek.format(dayFormatter)
        );

        for(int i=0;i<7;i++){

            LocalDate date = currentWeekStart.plusDays(i);

            JPanel dayPanel = (JPanel) calendarPanel.getComponent(i);

            JLabel header = (JLabel) dayPanel.getComponent(0);
            JScrollPane scroll = (JScrollPane) dayPanel.getComponent(1);

            JPanel list = (JPanel) scroll.getViewport().getView();
            list.removeAll();

            String dayName = date.getDayOfWeek().toString().substring(0,3);

            header.setText(dayName+" "+date.format(dayFormatter));

            if(date.equals(LocalDate.now())){
                header.setForeground(new Color(33,150,243));
            }else{
                header.setForeground(Color.BLACK);
            }
        }

        for(Schedule s : schedules){

            LocalDate date = s.getStudyDate();

            if(date.isBefore(currentWeekStart) || date.isAfter(endWeek)){
                continue;
            }

            int dayIndex = date.getDayOfWeek().getValue() - 1;

            JPanel dayPanel = (JPanel) calendarPanel.getComponent(dayIndex);
            JScrollPane scroll = (JScrollPane) dayPanel.getComponent(1);

            JPanel list = (JPanel) scroll.getViewport().getView();

            JPanel card = createScheduleCard(s);

            list.add(card);
            list.add(Box.createVerticalStrut(8));
        }

        revalidate();
        repaint();
    }

    private JPanel createScheduleCard(Schedule s){

    JPanel card = new JPanel(new BorderLayout());
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE,65));

    LocalDate today = LocalDate.now();
    LocalDate studyDate = s.getStudyDate();

    Color color;

    if(studyDate.isEqual(today)){
        color = new Color(244,67,54); // đỏ - hôm nay
    }
    else if(studyDate.isBefore(today)){
        color = new Color(255,193,7); // vàng - đã học
    }
    else{
        color = new Color(0,0,0); // đen - chưa học
    }

    card.setBackground(Color.WHITE);

    card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,5,0,0,color),
            BorderFactory.createEmptyBorder(8,10,8,10)
    ));

    JLabel title = new JLabel(s.getClassEntity().getClassName());
    title.setFont(new Font("Segoe UI",Font.BOLD,13));

    JLabel time = new JLabel(s.getStartTime()+" - "+s.getEndTime());

    JLabel room = new JLabel(
            s.getRoom()!=null ? "Room: "+s.getRoom().getRoomName() : ""
    );

    JPanel info = new JPanel();
    info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
    info.setOpaque(false);

    info.add(title);
    info.add(time);
    info.add(room);

    card.add(info,BorderLayout.CENTER);

    card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    card.addMouseListener(new java.awt.event.MouseAdapter(){

        public void mouseEntered(java.awt.event.MouseEvent e){
            card.setBackground(new Color(245,245,245));
        }

        public void mouseExited(java.awt.event.MouseEvent e){
            card.setBackground(Color.WHITE);
        }

        public void mouseClicked(java.awt.event.MouseEvent evt){

            JOptionPane.showMessageDialog(
                    StudentSchedulePanel.this,
                    "Class: "+s.getClassEntity().getClassName()+"\n"+
                    "Teacher: "+s.getClassEntity().getTeacher().getFullName()+"\n"+
                    "Time: "+s.getStartTime()+" - "+s.getEndTime()+"\n"+
                    "Room: "+(s.getRoom()!=null?s.getRoom().getRoomName():"")
            );

        }

    });

    return card;
}
}