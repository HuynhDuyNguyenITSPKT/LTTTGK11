package com.languagecenter.ui.teacher;

import com.languagecenter.model.Schedule;
import com.languagecenter.service.EnrollmentService;
import com.languagecenter.service.ScheduleService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

public class TeacherSchedulePanel extends JPanel {

    private final ScheduleService service;
    private final EnrollmentService enrollmentService;
    private final Long teacherId;

    private List<Schedule> schedules = new ArrayList<>();

    private JPanel calendarPanel;
    private JLabel weekLabel;

    private LocalDate currentWeekStart;

    // map tính tiết
    private Map<Long,Integer> lessonIndexMap = new HashMap<>();
    private Map<Long,Integer> totalLessonMap = new HashMap<>();

    public TeacherSchedulePanel(ScheduleService service, Long teacherId, EnrollmentService enrollmentService) {

        this.service = service;
        this.teacherId = teacherId;
        this.enrollmentService = enrollmentService;

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
        toolbar.setBackground(new Color(63,81,181));

        JButton prevBtn = new JButton("◀");
        JButton nextBtn = new JButton("▶");

        prevBtn.addActionListener(e -> changeWeek(-1));
        nextBtn.addActionListener(e -> changeWeek(1));

        JLabel title = new JLabel("Teacher Schedule");
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

            schedules = service.getScheduleByTeacher(teacherId);

            calculateLessonIndex();

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

        weekLabel.setText(currentWeekStart + " → " + endWeek);

        for(int i=0;i<7;i++){

            JPanel dayPanel = (JPanel) calendarPanel.getComponent(i);

            while(dayPanel.getComponentCount() > 2){
                dayPanel.remove(2);
            }
        }

        for(Schedule s : schedules){

            LocalDate date = s.getStudyDate();

            if(date.isBefore(currentWeekStart) || date.isAfter(endWeek))
                continue;

            int dayIndex = date.getDayOfWeek().getValue() - 1;

            JPanel dayPanel = (JPanel) calendarPanel.getComponent(dayIndex);

            dayPanel.add(createScheduleCard(s));
        }

        revalidate();
        repaint();
    }

    // tính tiết
    private void calculateLessonIndex(){

        Map<Long,List<Schedule>> classSchedules = new HashMap<>();

        for(Schedule s : schedules){

            Long classId = s.getClassEntity().getId();

            classSchedules
                    .computeIfAbsent(classId,k->new ArrayList<>())
                    .add(s);
        }

        for(Map.Entry<Long,List<Schedule>> entry : classSchedules.entrySet()){

            List<Schedule> list = entry.getValue();

            list.sort((a,b)->{

                int d = a.getStudyDate().compareTo(b.getStudyDate());

                if(d != 0) return d;

                return a.getStartTime().compareTo(b.getStartTime());
            });

            int total = list.size();

            for(int i=0;i<list.size();i++){

                Schedule s = list.get(i);

                lessonIndexMap.put(s.getId(), i+1);
                totalLessonMap.put(s.getId(), total);
            }
        }
    }

    private JPanel createScheduleCard(Schedule s){

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));

        card.setMaximumSize(new Dimension(200,90));
        card.setBackground(new Color(200,230,201));
        card.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

        String className = s.getClassEntity().getClassName();

        int lesson = lessonIndexMap.getOrDefault(s.getId(),1);
        int total = totalLessonMap.getOrDefault(s.getId(),1);

        long studentCount = 0;

        try{
            studentCount = enrollmentService.countStudentsByClass(
                    s.getClassEntity().getId()
            );
        }catch(Exception ex){
            ex.printStackTrace();
        }

        int maxStudent = s.getClassEntity().getMaxStudent();

        JLabel title = new JLabel(className);
        title.setFont(new Font("Arial",Font.BOLD,12));

        JLabel time = new JLabel(
                "⏰ "+s.getStartTime()+" - "+s.getEndTime()
        );

        JLabel lessonLbl = new JLabel(
                "📚 Tiết "+lesson+" / "+total
        );

        JLabel studentLbl = new JLabel(
                "👨‍🎓 "+studentCount+" / "+maxStudent
        );

        JLabel room = new JLabel(
                "🏫 "+(s.getRoom()!=null ? s.getRoom().getRoomName() : "")
        );

        card.add(title);
        card.add(time);
        card.add(lessonLbl);
        card.add(studentLbl);
        card.add(room);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new java.awt.event.MouseAdapter(){

            public void mouseClicked(java.awt.event.MouseEvent evt){

                JOptionPane.showMessageDialog(
                        TeacherSchedulePanel.this,
                        "Class: "+className+"\n"+
                                "Lesson: "+lesson+" / "+total+"\n"+
                                "Students: "+maxStudent+"\n"+
                                "Time: "+s.getStartTime()+" - "+s.getEndTime()+"\n"+
                                "Room: "+(s.getRoom()!=null?s.getRoom().getRoomName():"")
                );
            }
        });

        return card;
    }
}