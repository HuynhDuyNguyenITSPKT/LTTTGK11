package com.languagecenter.ui.schedule;

import com.languagecenter.model.Class;
import com.languagecenter.model.Room;
import com.languagecenter.model.Schedule;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ScheduleFormDialog extends JDialog {

    private final JComboBox<Class> cboClass = new JComboBox<>();
    private final JComboBox<Room> cboRoom = new JComboBox<>();

    private final JTextField txtDate = new JTextField();
    private final JTextField txtStart = new JTextField();
    private final JTextField txtEnd = new JTextField();

    private boolean saved=false;
    private final Schedule schedule;

    public ScheduleFormDialog(Frame owner,
                              String title,
                              Schedule existing,
                              List<Class> classes,
                              List<Room> rooms){

        super(owner,title,true);

        this.schedule = existing!=null ? existing : new Schedule();

        classes.forEach(cboClass::addItem);
        rooms.forEach(cboRoom::addItem);

        buildUI();

        if(existing!=null){

            cboClass.setSelectedItem(existing.getClassEntity());
            cboRoom.setSelectedItem(existing.getRoom());

            txtDate.setText(existing.getStudyDate().toString());
            txtStart.setText(existing.getStartTime().toString());
            txtEnd.setText(existing.getEndTime().toString());

        }

        setSize(400,300);
        setLocationRelativeTo(owner);
    }

    private void buildUI(){

        setLayout(new GridLayout(6,2,10,10));

        ((JComponent)getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10,10,10,10)
        );

        add(new JLabel("Class"));
        add(cboClass);

        add(new JLabel("Room"));
        add(cboRoom);

        add(new JLabel("Date (YYYY-MM-DD)"));
        add(txtDate);

        add(new JLabel("Start (HH:MM)"));
        add(txtStart);

        add(new JLabel("End (HH:MM)"));
        add(txtEnd);

        JButton btnSave = new JButton("Save");

        btnSave.addActionListener(e -> {

            try{

                schedule.setClassEntity((Class) cboClass.getSelectedItem());
                schedule.setRoom((Room) cboRoom.getSelectedItem());

                schedule.setStudyDate(LocalDate.parse(txtDate.getText().trim()));
                schedule.setStartTime(LocalTime.parse(txtStart.getText().trim()));
                schedule.setEndTime(LocalTime.parse(txtEnd.getText().trim()));

                saved=true;
                dispose();

            }catch(Exception ex){

                JOptionPane.showMessageDialog(
                        this,
                        "Sai định dạng!\nDate: YYYY-MM-DD\nTime: HH:MM",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );

            }

        });

        add(new JLabel());
        add(btnSave);
    }

    public boolean isSaved(){
        return saved;
    }

    public Schedule getSchedule(){
        return schedule;
    }
}