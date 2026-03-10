package com.languagecenter.ui.teacher;

import com.languagecenter.model.Schedule;
import com.languagecenter.service.EnrollmentService;
import com.languagecenter.service.ScheduleService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class TeacherSchedulePanel extends JPanel {

    private final ScheduleService service;
    private final EnrollmentService enrollmentService;
    private final Long teacherId;

    private List<Schedule> schedules = new ArrayList<>();

    private JPanel calendarPanel;
    private JLabel weekLabel;

    private LocalDate currentWeekStart;

    private Map<Long,Integer> lessonIndexMap = new HashMap<>();
    private Map<Long,Integer> totalLessonMap = new HashMap<>();

    private final DateTimeFormatter dayFormatter =
            DateTimeFormatter.ofPattern("dd/MM");

    public TeacherSchedulePanel(ScheduleService service, Long teacherId, EnrollmentService enrollmentService) {

        this.service = service;
        this.teacherId = teacherId;
        this.enrollmentService = enrollmentService;

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

        JLabel title = new JLabel("Teacher Schedule");
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

            if(date.equals(LocalDate.now()))
                header.setForeground(new Color(33,150,243));
            else
                header.setForeground(Color.BLACK);
        }

        for(Schedule s : schedules){

            LocalDate date = s.getStudyDate();

            if(date.isBefore(currentWeekStart) || date.isAfter(endWeek))
                continue;

            int dayIndex = date.getDayOfWeek().getValue() - 1;

            JPanel dayPanel = (JPanel) calendarPanel.getComponent(dayIndex);
            JScrollPane scroll = (JScrollPane) dayPanel.getComponent(1);

            JPanel list = (JPanel) scroll.getViewport().getView();

            list.add(createScheduleCard(s));
            list.add(Box.createVerticalStrut(8));
        }

        revalidate();
        repaint();
    }

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

        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,80));

        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,5,0,0,new Color(33,150,243)),
                BorderFactory.createEmptyBorder(8,10,8,10)
        ));

        String className = s.getClassEntity().getClassName();

        int lesson = lessonIndexMap.getOrDefault(s.getId(),1);
        int total = totalLessonMap.getOrDefault(s.getId(),1);

        long tempStudentCount = 0;

        try{
            tempStudentCount = enrollmentService
                    .countStudentsByClass(s.getClassEntity().getId());
        }
        catch(Exception ex){
            ex.printStackTrace();
        }

        final long studentCount = tempStudentCount;
        int maxStudent = s.getClassEntity().getMaxStudent();

        JLabel title = new JLabel(className);
        title.setFont(new Font("Segoe UI",Font.BOLD,13));

        JLabel time = new JLabel("⏰ "+s.getStartTime()+" - "+s.getEndTime());

        JLabel lessonLbl = new JLabel("📚 Lesson "+lesson+" / "+total);

        JLabel studentLbl = new JLabel("👨‍🎓 "+studentCount+" / "+maxStudent);

        JLabel room = new JLabel(
                "🏫 "+(s.getRoom()!=null ? s.getRoom().getRoomName() : "")
        );

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        info.setOpaque(false);

        info.add(title);
        info.add(time);
        info.add(lessonLbl);
        info.add(studentLbl);
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
                        TeacherSchedulePanel.this,
                        "Class: "+className+"\n"+
                                "Lesson: "+lesson+" / "+total+"\n"+
                                "Students: "+studentCount+" / "+maxStudent+"\n"+
                                "Time: "+s.getStartTime()+" - "+s.getEndTime()+"\n"+
                                "Room: "+(s.getRoom()!=null?s.getRoom().getRoomName():"")
                );
            }
        });

        return card;
    }
}