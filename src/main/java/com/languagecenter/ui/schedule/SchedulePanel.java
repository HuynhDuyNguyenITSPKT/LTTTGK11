package com.languagecenter.ui.schedule;

import com.languagecenter.model.Schedule;
import com.languagecenter.service.ClassService;
import com.languagecenter.service.RoomService;
import com.languagecenter.service.ScheduleService;
import com.languagecenter.stream.ScheduleStreamQueries;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class SchedulePanel extends JPanel {

    private final ScheduleService service;
    private final ClassService classService;
    private final RoomService roomService;

    private final ScheduleTableModel tableModel = new ScheduleTableModel();
    private final JTable table = new JTable(tableModel);

    private List<Schedule> allData;

    private final JTextField txtSearch = new JTextField(15);
    private final JTextField txtDate = new JTextField(10);

    public SchedulePanel(ScheduleService service,
                         ClassService classService,
                         RoomService roomService){

        this.service = service;
        this.classService = classService;
        this.roomService = roomService;

        setLayout(new BorderLayout());
        setBackground(new Color(245,245,245));

        buildToolbar();
        buildTable();

        reload();
    }

    private void buildToolbar(){

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(30,136,229));
        toolbar.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));

        JButton btnAdd = createButton("Add", new Color(76,175,80));
        JButton btnEdit = createButton("Edit", new Color(255,167,38));
        JButton btnDelete = createButton("Delete", new Color(244,67,54));
        JButton btnRefresh = createButton("Refresh", new Color(120,144,156));

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        toolbar.add(new JLabel("   Search:"));
        toolbar.add(txtSearch);

        toolbar.add(new JLabel("Date:"));
        toolbar.add(txtDate);

        JButton btnFilter = createButton("Filter", new Color(33,150,243));
        toolbar.add(btnFilter);

        add(toolbar,BorderLayout.NORTH);

        btnRefresh.addActionListener(e -> reload());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnFilter.addActionListener(e -> applyFilter());

        txtSearch.addActionListener(e -> applyFilter());
    }

    private void buildTable(){

        table.setRowHeight(32);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(new Color(220,220,220));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        for(int i=0;i<table.getColumnCount();i++){
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        table.setSelectionBackground(new Color(200,230,255));

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

    public void reload(){

        try{

            allData = service.getAll();
            tableModel.setData(allData);

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void applyFilter(){

        List<Schedule> result = allData;

        if(!txtSearch.getText().isBlank()){
            result = ScheduleStreamQueries.filterByClass(result, txtSearch.getText());
        }

        if(!txtDate.getText().isBlank()){
            try{
                LocalDate date = LocalDate.parse(txtDate.getText());
                result = ScheduleStreamQueries.filterByDate(result,date);
            }catch(Exception ignored){}
        }

        tableModel.setData(result);
    }

    private void onAdd(){

        try{

            ScheduleFormDialog dlg =
                    new ScheduleFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this),
                            "Add Schedule",
                            null,
                            classService.getAll(),
                            roomService.getAll()
                    );

            dlg.setVisible(true);

            if(dlg.isSaved()){

                service.create(dlg.getSchedule());
                reload();

            }

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());

        }

    }

    private void onEdit(){

        int row = table.getSelectedRow();

        if(row<0) return;

        Schedule s = tableModel.getScheduleAt(row);

        try{

            ScheduleFormDialog dlg =
                    new ScheduleFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this),
                            "Edit Schedule",
                            s,
                            classService.getAll(),
                            roomService.getAll()
                    );

            dlg.setVisible(true);

            if(dlg.isSaved()){

                service.update(dlg.getSchedule());
                reload();

            }

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());

        }

    }

    private void onDelete(){

        int row = table.getSelectedRow();

        if(row<0) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xóa lịch này?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION)
            return;

        Schedule s = tableModel.getScheduleAt(row);

        try{

            service.delete(s.getId());
            reload();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());

        }

    }
}