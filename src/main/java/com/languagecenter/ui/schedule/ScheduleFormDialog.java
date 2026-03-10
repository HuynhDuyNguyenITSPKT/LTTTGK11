package com.languagecenter.ui.schedule;

import com.languagecenter.model.Class;
import com.languagecenter.model.Room;
import com.languagecenter.model.Schedule;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

        setupRenderer();
        buildUI();

        if(existing!=null){
            fillData(existing);
        }else{
            txtDate.setText(LocalDate.now().toString());
            txtStart.setText("08:00");
            txtEnd.setText("10:00");
        }

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void setupRenderer(){

        ListCellRenderer<Object> renderer = new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,Object value,int index,
                    boolean isSelected,boolean cellHasFocus){

                if(value instanceof Class c){
                    value = c.getClassName();
                }

                if(value instanceof Room r){
                    value = r.getRoomName();
                }

                return super.getListCellRendererComponent(
                        list,value,index,isSelected,cellHasFocus);
            }
        };

        cboClass.setRenderer(renderer);
        cboRoom.setRenderer(renderer);
    }

    private void buildUI(){

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(20,20,20,20));

        JPanel form = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(form,"Class",cboClass,gbc,0);
        addField(form,"Room",cboRoom,gbc,1);
        addField(form,"Date (YYYY-MM-DD)",txtDate,gbc,2);
        addField(form,"Start Time (HH:MM)",txtStart,gbc,3);
        addField(form,"End Time (HH:MM)",txtEnd,gbc,4);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnCancel = new JButton("Cancel");
        JButton btnSave = new JButton("Save");

        btnCancel.addActionListener(e -> dispose());

        btnSave.addActionListener(e -> onSave());

        actions.add(btnCancel);
        actions.add(btnSave);

        root.add(form,BorderLayout.CENTER);
        root.add(actions,BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addField(JPanel panel,
                          String label,
                          JComponent field,
                          GridBagConstraints gbc,
                          int row){

        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(label),gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field,gbc);
    }

    private void onSave(){

        try{

            schedule.setClassEntity((Class) cboClass.getSelectedItem());
            schedule.setRoom((Room) cboRoom.getSelectedItem());

            schedule.setStudyDate(
                    LocalDate.parse(txtDate.getText().trim())
            );

            schedule.setStartTime(
                    LocalTime.parse(txtStart.getText().trim())
            );

            schedule.setEndTime(
                    LocalTime.parse(txtEnd.getText().trim())
            );

            saved=true;
            dispose();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    "Sai định dạng!\nDate: YYYY-MM-DD\nTime: HH:MM",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

        }
    }

    private void fillData(Schedule existing){

        cboClass.setSelectedItem(existing.getClassEntity());
        cboRoom.setSelectedItem(existing.getRoom());

        if(existing.getStudyDate()!=null)
            txtDate.setText(existing.getStudyDate().toString());

        if(existing.getStartTime()!=null)
            txtStart.setText(existing.getStartTime().toString());

        if(existing.getEndTime()!=null)
            txtEnd.setText(existing.getEndTime().toString());
    }

    public boolean isSaved(){
        return saved;
    }

    public Schedule getSchedule(){
        return schedule;
    }
}