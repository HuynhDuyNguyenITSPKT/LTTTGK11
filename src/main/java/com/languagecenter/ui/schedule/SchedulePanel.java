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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulePanel extends JPanel {

    private final ScheduleService service;
    private final ClassService classService;
    private final RoomService roomService;

    private final ScheduleTableModel tableModel = new ScheduleTableModel();
    private final JTable table = new JTable(tableModel);
    private ScheduleFormDialog formDialog;

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

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));

        JButton btnAdd = createButton("Add", new Color(34, 197, 94));
        JButton btnEdit = createButton("Edit", new Color(245, 158, 11));
        JButton btnView = createButton("View", new Color(14,165,233));
        JButton btnExport = createButton("Export PDF", new Color(99,102,241));
        JButton btnDelete = createButton("Delete", new Color(239, 68, 68));
        JButton btnRefresh = createButton("Refresh", new Color(100, 116, 139));

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnView);
        toolbar.add(btnExport);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        toolbar.add(new JLabel("   Search:"));
        toolbar.add(txtSearch);

        toolbar.add(new JLabel("Date:"));
        toolbar.add(txtDate);

        JButton btnFilter = createButton("Filter", new Color(79, 70, 229));
        toolbar.add(btnFilter);

        add(toolbar,BorderLayout.NORTH);

        btnRefresh.addActionListener(e -> reload());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnView.addActionListener(e -> onView());
        btnExport.addActionListener(e -> onExportPdf());
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
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(6,14,6,14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

            if(formDialog==null){
                formDialog = new ScheduleFormDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        "Schedule",
                        null,
                        classService.getAll(),
                        roomService.getAll()
                );
            }

            formDialog.prepare(null, "Add Schedule", false);
            formDialog.setVisible(true);

            if(formDialog.isSaved()){
                service.create(formDialog.getSchedule());
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

            if(formDialog==null){
                formDialog = new ScheduleFormDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        "Schedule",
                        null,
                        classService.getAll(),
                        roomService.getAll()
                );
            }

            formDialog.prepare(s, "Edit Schedule", false);
            formDialog.setVisible(true);

            if(formDialog.isSaved()){
                service.update(formDialog.getSchedule());
                reload();
            }

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());

        }

    }

    private void onView(){

        int row = table.getSelectedRow();
        if(row<0) return;

        Schedule s = tableModel.getScheduleAt(row);

        try{
            if(formDialog==null){
                formDialog = new ScheduleFormDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        "Schedule",
                        null,
                        classService.getAll(),
                        roomService.getAll()
                );
            }

            formDialog.prepare(s, "View Schedule", true);
            formDialog.setVisible(true);

        }catch(Exception ex){
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }

    }

    private void onExportPdf(){

        try{
            // choose class
            java.util.List<com.languagecenter.model.Class> classes = classService.getAll();
            JComboBox<com.languagecenter.model.Class> cbo = new JComboBox<>();
            classes.forEach(cbo::addItem);

            int res = JOptionPane.showConfirmDialog(this, cbo, "Select class to export", JOptionPane.OK_CANCEL_OPTION);
            if(res != JOptionPane.OK_OPTION) return;

            com.languagecenter.model.Class selected = (com.languagecenter.model.Class) cbo.getSelectedItem();
            if(selected==null) return;

            // filter schedules
            java.util.List<Schedule> all = service.getAll();
            java.util.List<Map<String,Object>> rows = new java.util.ArrayList<>();
            for(Schedule s : all){
                if(s.getClassEntity()!=null && s.getClassEntity().getId().equals(selected.getId())){
                    Map<String,Object> m = new HashMap<>();
                    m.put("date", s.getStudyDate()!=null? s.getStudyDate().toString(): "");
                    m.put("start", s.getStartTime()!=null? s.getStartTime().toString(): "");
                    m.put("end", s.getEndTime()!=null? s.getEndTime().toString(): "");
                    m.put("room", s.getRoom()!=null? s.getRoom().getRoomName(): "");
                    rows.add(m);
                }
            }

            if(rows.isEmpty()){
                JOptionPane.showMessageDialog(this, "Không có lịch cho lớp này.");
                return;
            }

            JFileChooser chooser = new JFileChooser();
            String base = selected.getClassName() != null ? selected.getClassName().replaceAll("\\s+","_") : "class";
            chooser.setSelectedFile(new java.io.File("schedule_" + base + ".pdf"));
            int fc = chooser.showSaveDialog(this);
            if(fc != JFileChooser.APPROVE_OPTION) return;

            java.io.File out = chooser.getSelectedFile();

            com.languagecenter.util.ReportUtil.exportScheduleByClass(rows, selected.getClassName(), out);

            JOptionPane.showMessageDialog(this, "Export PDF thành công: " + out.getAbsolutePath());

        }catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất PDF: " + ex.getMessage());
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