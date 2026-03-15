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
    private final JTextField txtFrom = new JTextField();

    private List<Class> classes;
    private List<Room> rooms;
    private JButton btnSave;

    private boolean saved = false;
    private Schedule schedule;

    public ScheduleFormDialog(Frame owner,
                              String title,
                              Schedule existing,
                              List<Class> classes,
                              List<Room> rooms){

        super(owner,title,true);

        this.classes = classes;
        this.rooms = rooms;

        // populate combos
        classes.forEach(cboClass::addItem);
        rooms.forEach(cboRoom::addItem);

        setupRenderer();
        buildUI();
        setupFromField();

        if(existing!=null){
            prepare(existing, title, false);
        }else{
            prepare(null, title, false);
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

        addField(form,"From (search)",txtFrom,gbc,0);
        addField(form,"Class",cboClass,gbc,1);
        addField(form,"Room",cboRoom,gbc,2);
        addField(form,"Date (YYYY-MM-DD)",txtDate,gbc,3);
        addField(form,"Start Time (HH:MM)",txtStart,gbc,4);
        addField(form,"End Time (HH:MM)",txtEnd,gbc,5);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnCancel = new JButton("Cancel");
        btnSave = new JButton("Save");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        actions.add(btnCancel);
        actions.add(btnSave);

        root.add(form,BorderLayout.CENTER);
        root.add(actions,BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void setupFromField(){

        // When user presses Enter in From, search class names and select first match
        txtFrom.addActionListener(e -> searchAndSelectClass());

        // When user changes class selection, update the From text to match
        cboClass.addActionListener(e -> {
            Class selected = (Class) cboClass.getSelectedItem();
            if(selected!=null){
                txtFrom.setText(selected.getClassName());
            }
        });
    }

    private void searchAndSelectClass(){

        String q = txtFrom.getText().trim().toLowerCase();
        if(q.isBlank()) return;

        for(int i = 0; i < cboClass.getItemCount(); i++){
            Class c = cboClass.getItemAt(i);
            if(c.getClassName()!=null && c.getClassName().toLowerCase().contains(q)){
                cboClass.setSelectedIndex(i);
                return;
            }
        }

        // not found -> show small notice
        JOptionPane.showMessageDialog(this,
                "Không tìm thấy lớp phù hợp: " + txtFrom.getText(),
                "Tìm kiếm", JOptionPane.INFORMATION_MESSAGE);
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

        // Class
        for(int i = 0; i < cboClass.getItemCount(); i++){
            Class c = cboClass.getItemAt(i);
            if(c.getId().equals(existing.getClassEntity().getId())){
                cboClass.setSelectedIndex(i);
                txtFrom.setText(c.getClassName());
                break;
            }
        }

        // Room
        for(int i = 0; i < cboRoom.getItemCount(); i++){
            Room r = cboRoom.getItemAt(i);
            if(r.getId().equals(existing.getRoom().getId())){
                cboRoom.setSelectedIndex(i);
                break;
            }
        }

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

    public void prepare(Schedule existing, String title, boolean readOnly){

        setTitle(title);

        this.schedule = existing!=null ? existing : new Schedule();

        // repopulate combos to ensure current lists
        cboClass.removeAllItems();
        classes.forEach(cboClass::addItem);

        cboRoom.removeAllItems();
        rooms.forEach(cboRoom::addItem);

        if(existing!=null){
            fillData(existing);
        }else{
            txtFrom.setText("");
            txtDate.setText(LocalDate.now().toString());
            txtStart.setText("08:00");
            txtEnd.setText("10:00");
        }

        // read-only mode
        btnSave.setVisible(!readOnly);
        cboClass.setEnabled(!readOnly);
        cboRoom.setEnabled(!readOnly);
        txtDate.setEditable(!readOnly);
        txtStart.setEditable(!readOnly);
        txtEnd.setEditable(!readOnly);
        txtFrom.setEditable(!readOnly);

        this.saved = false;
    }

}
