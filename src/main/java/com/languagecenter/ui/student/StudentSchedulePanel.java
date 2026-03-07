package com.languagecenter.ui.student;

import com.languagecenter.model.Schedule;
import com.languagecenter.service.ScheduleService;
import com.languagecenter.stream.StudentStreamQueries;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

public class StudentSchedulePanel extends JPanel {

    private final ScheduleService service;
    private final Long studentId;

    private final StudentScheduleTableModel tableModel =
            new StudentScheduleTableModel();

    private final JTable table = new JTable(tableModel);

    private List<Schedule> allData;

    private final JTextField txtDate = new JTextField(10);

    public StudentSchedulePanel(ScheduleService service, Long studentId){

        this.service = service;
        this.studentId = studentId;

        setLayout(new BorderLayout());
        setBackground(new Color(245,245,245));

        buildToolbar();
        buildTable();

        loadData();
    }

    private void buildToolbar(){

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(30,136,229));
        toolbar.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));

        JLabel title = new JLabel("My Schedule");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial",Font.BOLD,16));

        toolbar.add(title);

        toolbar.add(new JLabel("   Date:"));
        toolbar.add(txtDate);

        JButton btnFilter = createButton("Filter", new Color(33,150,243));
        JButton btnToday = createButton("Today", new Color(76,175,80));
        JButton btnMap = createButton("Room Map", new Color(255,152,0));

        toolbar.add(btnFilter);
        toolbar.add(btnToday);
        toolbar.add(btnMap);

        add(toolbar,BorderLayout.NORTH);

        btnFilter.addActionListener(e -> applyFilter());

        btnToday.addActionListener(e -> {

            List<Schedule> result =
                    StudentStreamQueries.filterToday(allData);

            tableModel.setData(result);
        });

        btnMap.addActionListener(e -> openRoomMap());
    }

    private void buildTable(){

        table.setRowHeight(32);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(new Color(220,220,220));

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer(){

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,int column){

                        Component c = super.getTableCellRendererComponent(
                                table,value,isSelected,hasFocus,row,column);

                        LocalDate date = tableModel.getDateAt(row);

                        if(date.equals(LocalDate.now()))
                            c.setBackground(new Color(255,249,196));
                        else
                            c.setBackground(Color.WHITE);

                        if(isSelected)
                            c.setBackground(new Color(200,230,255));

                        setHorizontalAlignment(JLabel.CENTER);

                        return c;
                    }
                };

        for(int i=0;i<table.getColumnCount();i++)
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);

        add(new JScrollPane(table),BorderLayout.CENTER);
    }

    private JButton createButton(String text, Color color){

        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6,14,6,14));

        return btn;
    }

    private void loadData(){

        try{

            allData = service.getScheduleByStudent(studentId);
            tableModel.setData(allData);

        }catch(Exception e){

            JOptionPane.showMessageDialog(this,"Cannot load schedule");
        }
    }

    private void applyFilter(){

        if(txtDate.getText().isBlank()){
            tableModel.setData(allData);
            return;
        }

        try{

            LocalDate date = LocalDate.parse(txtDate.getText());

            List<Schedule> result =
                    StudentStreamQueries.filterByDate(allData,date);

            tableModel.setData(result);

        }catch(Exception e){

            JOptionPane.showMessageDialog(this,"Date format: YYYY-MM-DD");
        }
    }

    /**
     * mở google map phòng học
     */
    private void openRoomMap(){

        int row = table.getSelectedRow();

        if(row < 0){
            JOptionPane.showMessageDialog(this,"Select a schedule first");
            return;
        }

        Schedule s = tableModel.getScheduleAt(row);

        String roomName = s.getRoom().getRoomName();

        try{

            Desktop.getDesktop().browse(
                    new URI("https://www.google.com/maps/search/" + roomName)
            );

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}