package com.languagecenter.ui.teacher;

import com.languagecenter.model.Schedule;
import com.languagecenter.model.enums.ClassStatus;
import com.languagecenter.util.ReportUtil;
import com.languagecenter.service.EnrollmentService;
import com.languagecenter.service.ScheduleService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
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

        JButton export = new JButton("Export");

        export.addActionListener(e -> onExportTeacherPdf());

        prev.addActionListener(e -> changeWeek(-1));
        next.addActionListener(e -> changeWeek(1));

        weekLabel = new JLabel();
        weekLabel.setForeground(Color.WHITE);
        weekLabel.setFont(new Font("Segoe UI",Font.BOLD,14));

        nav.add(prev);
        nav.add(next);
        nav.add(Box.createHorizontalStrut(10));
        nav.add(weekLabel);
        nav.add(Box.createHorizontalStrut(8));
        nav.add(export);

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

            List<Schedule> loaded = service.getScheduleByTeacher(teacherId);
            schedules = (loaded != null) ? loaded : new ArrayList<>();

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

            if (s == null) continue;

            if(s.getClassEntity().getStatus() != ClassStatus.Ongoing)
                continue;

            LocalDate date = s.getStudyDate();
            if (date == null) continue;

            if(date.isBefore(currentWeekStart) || date.isAfter(endWeek))
                continue;

            int dayIndex = date.getDayOfWeek().getValue() - 1;
            if (dayIndex < 0 || dayIndex > 6) continue;

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

        lessonIndexMap.clear();
        totalLessonMap.clear();

        for(Schedule s : schedules){

            if (s == null || s.getId() == null) continue;
            if (s.getClassEntity() == null || s.getClassEntity().getId() == null) continue;

            Long classId = s.getClassEntity().getId();

            classSchedules
                    .computeIfAbsent(classId,k->new ArrayList<>())
                    .add(s);
        }

        for(Map.Entry<Long,List<Schedule>> entry : classSchedules.entrySet()){

            List<Schedule> list = entry.getValue();

            list.sort((a,b)->{

                LocalDate dateA = a.getStudyDate();
                LocalDate dateB = b.getStudyDate();
                if (dateA == null && dateB == null) return 0;
                if (dateA == null) return 1;
                if (dateB == null) return -1;

                int d = dateA.compareTo(dateB);

                if(d != 0) return d;

                if (a.getStartTime() == null && b.getStartTime() == null) return 0;
                if (a.getStartTime() == null) return 1;
                if (b.getStartTime() == null) return -1;
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

        String className = (s.getClassEntity() != null && s.getClassEntity().getClassName() != null)
                ? s.getClassEntity().getClassName()
                : "(No class)";

        Long classId = (s.getClassEntity() != null) ? s.getClassEntity().getId() : null;

        int lesson = lessonIndexMap.getOrDefault(s.getId(),1);
        int total = totalLessonMap.getOrDefault(s.getId(),1);

        long tempStudentCount = 0;

        if (classId != null) {
            try{
                tempStudentCount = enrollmentService.countStudentsByClass(classId);
            }
            catch(Exception ignored){
            }
        }

        final long studentCount = tempStudentCount;
        int maxStudent = (s.getClassEntity() != null) ? s.getClassEntity().getMaxStudent() : 0;

        boolean ongoing = s.getClassEntity()!=null && s.getClassEntity().getStatus() == ClassStatus.Ongoing;

        JLabel title = new JLabel(className);
        title.setFont(new Font("Segoe UI",Font.BOLD,13));

        String startText = (s.getStartTime() != null) ? s.getStartTime().toString() : "";
        String endText = (s.getEndTime() != null) ? s.getEndTime().toString() : "";
        String timeText = (ongoing ? "⏰ " : "") + startText + " - " + endText;
        JLabel time = new JLabel(timeText);

        String lessonText = (ongoing ? "📚 " : "") + "Lesson "+lesson+" / "+total;
        JLabel lessonLbl = new JLabel(lessonText);

        String studentText = (ongoing ? "👨‍🎓 " : "") + studentCount+" / "+maxStudent;
        JLabel studentLbl = new JLabel(studentText);

        String roomText = (ongoing ? "🏫 " : "") + (s.getRoom()!=null ? s.getRoom().getRoomName() : "");
        JLabel room = new JLabel(roomText);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        info.setOpaque(false);

        info.add(title);
        info.add(time);
        info.add(lessonLbl);
        info.add(studentLbl);
        info.add(room);

        card.add(info,BorderLayout.CENTER);

        // If class is full, add a View button on the right so teacher can inspect full-class details
        if(maxStudent > 0 && studentCount >= maxStudent){
            JButton viewBtn = new JButton("View");
            viewBtn.addActionListener(ev -> {
                String details = "Class: "+className+"\n"
                        +"Lesson: "+lesson+" / "+total+"\n"
                        +"Students: "+studentCount+" / "+maxStudent+"\n"
                        +"Time: "+startText+" - "+endText+"\n"
                        +"Room: "+(s.getRoom()!=null?s.getRoom().getRoomName():"")+"\n"
                        +"Status: "+(s.getClassEntity()!=null? s.getClassEntity().getStatus() : "");

                JOptionPane.showMessageDialog(TeacherSchedulePanel.this, details, "Class Details", JOptionPane.INFORMATION_MESSAGE);
            });

            JPanel right = new JPanel(new BorderLayout());
            right.setOpaque(false);
            right.add(viewBtn, BorderLayout.NORTH);
            card.add(right, BorderLayout.EAST);
        }

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
                                "Time: "+startText+" - "+endText+"\n"+
                                "Room: "+(s.getRoom()!=null?s.getRoom().getRoomName():"")
                );
            }
        });

        return card;
    }

    private void onExportTeacherPdf(){

        try{
            List<Schedule> teacherSchedules = service.getScheduleByTeacher(teacherId);
            if(teacherSchedules==null || teacherSchedules.isEmpty()){
                JOptionPane.showMessageDialog(this, "No schedules to export.");
                return;
            }

            List<Map<String,Object>> rows = new ArrayList<>();

            for(Schedule s : teacherSchedules){
                Map<String,Object> m = new HashMap<>();
                m.put("className", s.getClassEntity()!=null? s.getClassEntity().getClassName() : "");
                m.put("date", s.getStudyDate()!=null? s.getStudyDate().toString() : "");
                m.put("start", s.getStartTime()!=null? s.getStartTime().toString() : "");
                m.put("end", s.getEndTime()!=null? s.getEndTime().toString() : "");
                m.put("room", s.getRoom()!=null? s.getRoom().getRoomName() : "");
                long studentCount = 0;
                try{
                    Long exportClassId = (s.getClassEntity() != null) ? s.getClassEntity().getId() : null;
                    if (exportClassId != null) studentCount = enrollmentService.countStudentsByClass(exportClassId);
                }catch(Exception ignored){
                }
                m.put("studentCount", String.valueOf(studentCount));
                m.put("maxStudent", String.valueOf(s.getClassEntity()!=null? s.getClassEntity().getMaxStudent():0));
                m.put("status", s.getClassEntity()!=null? s.getClassEntity().getStatus().toString():"");
                rows.add(m);
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("teacher_schedule_"+teacherId+".pdf"));
            if(chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File out = chooser.getSelectedFile();

            ReportUtil.exportScheduleForTeacher(rows, "Teacher " + teacherId, out);

            JOptionPane.showMessageDialog(this, "Exported to: " + out.getAbsolutePath());

        }catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage());
        }
    }
}
