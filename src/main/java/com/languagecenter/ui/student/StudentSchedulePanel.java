package com.languagecenter.ui.student;

import com.languagecenter.model.Schedule;
import com.languagecenter.service.ScheduleService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentSchedulePanel extends JPanel {

    private final ScheduleService service;
    private final Long studentId;

    private List<Schedule> schedules = new ArrayList<>();

    private JPanel calendarPanel;

    private JLabel weekLabel;

    // tuần đang xem
    private LocalDate currentWeekStart;

    public StudentSchedulePanel(ScheduleService service, Long studentId) {

        this.service = service;
        this.studentId = studentId;

        setLayout(new BorderLayout());
        setBackground(new Color(245,245,245));

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

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(30,136,229));

        JButton prevBtn = new JButton("◀");
        JButton nextBtn = new JButton("▶");

        prevBtn.addActionListener(e -> changeWeek(-1));
        nextBtn.addActionListener(e -> changeWeek(1));

        JLabel title = new JLabel("Weekly Schedule");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial",Font.BOLD,18));

        weekLabel = new JLabel();
        weekLabel.setForeground(Color.WHITE);

        toolbar.add(title);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(prevBtn);
        toolbar.add(nextBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(weekLabel);

        add(toolbar,BorderLayout.NORTH);
    }

    private void buildCalendar(){

        calendarPanel = new JPanel(new GridLayout(1,7,5,5));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        String[] days = {
                "Mon","Tue","Wed","Thu","Fri","Sat","Sun"
        };

        for(int i=0;i<7;i++){

            JPanel dayPanel = new JPanel();
            dayPanel.setLayout(new BoxLayout(dayPanel,BoxLayout.Y_AXIS));
            dayPanel.setBackground(Color.WHITE);
            dayPanel.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));

            JLabel lbl = new JLabel(days[i],SwingConstants.CENTER);
            lbl.setFont(new Font("Arial",Font.BOLD,14));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            dayPanel.add(lbl);
            dayPanel.add(Box.createVerticalStrut(10));

            calendarPanel.add(dayPanel);
        }

        add(calendarPanel,BorderLayout.CENTER);
    }

    private void loadData(){

        try{

            schedules = service.getScheduleByStudent(studentId);

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

        // update label tuần
        LocalDate endWeek = currentWeekStart.plusDays(6);
        weekLabel.setText(currentWeekStart + "  →  " + endWeek);

        // clear cũ
        for(int i=0;i<7;i++){

            JPanel dayPanel = (JPanel) calendarPanel.getComponent(i);

            while(dayPanel.getComponentCount() > 2){
                dayPanel.remove(2);
            }
        }

        for(Schedule s : schedules){

            LocalDate date = s.getStudyDate();

            if(date.isBefore(currentWeekStart) || date.isAfter(endWeek)){
                continue;
            }

            int dayIndex = date.getDayOfWeek().getValue() - 1;

            JPanel dayPanel = (JPanel) calendarPanel.getComponent(dayIndex);

            JPanel card = createScheduleCard(s);

            dayPanel.add(card);
        }

        revalidate();
        repaint();
    }

    private JPanel createScheduleCard(Schedule s){

        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(200,60));
        card.setBackground(new Color(187,222,251));
        card.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JLabel title = new JLabel(s.getClassEntity().getClassName());
        title.setFont(new Font("Arial",Font.BOLD,12));

        JLabel time = new JLabel(
                s.getStartTime()+" - "+s.getEndTime()
        );

        JLabel room = new JLabel(
                s.getRoom()!=null ? s.getRoom().getRoomName() : ""
        );

        card.add(title,BorderLayout.NORTH);
        card.add(time,BorderLayout.CENTER);
        card.add(room,BorderLayout.SOUTH);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new java.awt.event.MouseAdapter(){

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