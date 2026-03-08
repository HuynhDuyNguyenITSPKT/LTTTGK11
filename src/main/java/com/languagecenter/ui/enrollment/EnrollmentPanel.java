package com.languagecenter.ui.enrollment;

import com.languagecenter.model.Enrollment;
import com.languagecenter.service.ClassService;
import com.languagecenter.service.EnrollmentService;
import com.languagecenter.service.StudentService;
import com.languagecenter.stream.EnrollmentStreamQueries;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class EnrollmentPanel extends JPanel {

    private final EnrollmentService service;
    private final StudentService studentService;
    private final ClassService classService;

    private final EnrollmentTableModel tableModel = new EnrollmentTableModel();
    private final JTable table = new JTable(tableModel);

    private List<Enrollment> allData;

    private final JTextField txtStudent = new JTextField(12);
    private final JTextField txtClass = new JTextField(12);

    public EnrollmentPanel(EnrollmentService service,
                           StudentService studentService,
                           ClassService classService){

        this.service = service;
        this.studentService = studentService;
        this.classService = classService;

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

        JButton btnAdd = createButton("Thêm", new Color(76,175,80));
        JButton btnEdit = createButton("Sửa", new Color(255,167,38));
        JButton btnDelete = createButton("Xóa", new Color(244,67,54));
        JButton btnRefresh = createButton("Refresh", new Color(120,144,156));

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        toolbar.add(new JLabel(" Student:"));
        toolbar.add(txtStudent);

        toolbar.add(new JLabel(" Class:"));
        toolbar.add(txtClass);

        JButton btnFilter = createButton("Filter", new Color(33,150,243));
        toolbar.add(btnFilter);

        add(toolbar,BorderLayout.NORTH);

        btnRefresh.addActionListener(e -> reload());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnFilter.addActionListener(e -> applyFilter());
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

    private void reload(){

        try{

            allData = service.getAll();
            tableModel.setData(allData);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void applyFilter(){

        List<Enrollment> result = allData;

        if(!txtStudent.getText().isBlank()){
            result = EnrollmentStreamQueries.filterByStudent(result, txtStudent.getText());
        }

        if(!txtClass.getText().isBlank()){
            result = EnrollmentStreamQueries.filterByClass(result, txtClass.getText());
        }

        tableModel.setData(result);
    }

    private void onAdd(){

        try{

            EnrollmentFormDialog dlg =
                    new EnrollmentFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this),
                            "Thêm Enrollment",
                            null,
                            studentService.getAll(),
                            classService.getAll()
                    );

            dlg.setVisible(true);

            if(dlg.isSaved()){

                service.create(dlg.getEnrollment());
                reload();

                // Thông báo Invoice đã được tạo tự động
                JOptionPane.showMessageDialog(
                        this,
                        "Enrollment đã được tạo thành công!\n" +
                        "Hóa đơn (Invoice) đã được tự động tạo cho enrollment này.\n" +
                        "Vui lòng vào mục 'Invoices' để xem chi tiết.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());
        }
    }

    private void onEdit(){

        int row = table.getSelectedRow();

        if(row < 0) return;

        Enrollment e = tableModel.getEnrollmentAt(row);

        try{

            EnrollmentFormDialog dlg =
                    new EnrollmentFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this),
                            "Sửa Enrollment",
                            e,
                            studentService.getAll(),
                            classService.getAll()
                    );

            dlg.setVisible(true);

            if(dlg.isSaved()){

                service.update(dlg.getEnrollment());
                reload();
            }

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());
        }
    }

    private void onDelete(){

        int row = table.getSelectedRow();

        if(row < 0) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xóa?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION)
            return;

        Enrollment e = tableModel.getEnrollmentAt(row);

        try{

            service.delete(e.getId());
            reload();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());
        }
    }
}