package com.languagecenter.ui.course;

import com.languagecenter.model.Course;
import com.languagecenter.model.enums.CourseStatus;
import com.languagecenter.service.CourseService;
import com.languagecenter.stream.CourseStreamQueries;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CoursePanel extends JPanel {

    private final CourseService service;

    private final JTextField txtSearch = new JTextField(15);

    private final JComboBox<CourseStatus> cboStatus =
            new JComboBox<>(CourseStatus.values());

    private final CourseTableModel tableModel =
            new CourseTableModel();

    private final JTable table =
            new JTable(tableModel);

    private List<Course> cachedCourses = new ArrayList<>();

    public CoursePanel(CourseService service){

        this.service = service;

        setLayout(new BorderLayout());

        buildUI();

        reloadAll();
    }

    private void buildUI(){

        JPanel top = new JPanel();

        JButton btnSearch = new JButton("Search");
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

        top.add(new JLabel("Search"));
        top.add(txtSearch);

        top.add(new JLabel("Status"));
        top.add(cboStatus);

        top.add(btnSearch);
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);

        add(top,BorderLayout.NORTH);

        add(new JScrollPane(table),BorderLayout.CENTER);

        btnSearch.addActionListener(e->runFilter());
        cboStatus.addActionListener(e->runFilter());
        btnRefresh.addActionListener(e->reloadAll());

        btnAdd.addActionListener(e->onAdd());
        btnEdit.addActionListener(e->onEdit());
        btnDelete.addActionListener(e->onDelete());
    }

    private void reloadAll(){

        try{

            cachedCourses = service.getAll();

            tableModel.setData(cachedCourses);

        }catch(Exception ex){

            ex.printStackTrace();
        }
    }

    private void runFilter(){

        List<Course> result = cachedCourses;

        String keyword = txtSearch.getText();

        if(keyword!=null && !keyword.isBlank()){

            result = CourseStreamQueries.searchByName(result,keyword);
        }

        CourseStatus status = (CourseStatus)cboStatus.getSelectedItem();

        if(status!=null){

            result = CourseStreamQueries.filterByStatus(result,status);
        }

        tableModel.setData(result);
    }

    private void onAdd(){

        CourseFormDialog dlg =
                new CourseFormDialog(
                        (Frame)SwingUtilities.getWindowAncestor(this),
                        "Add Course",
                        null
                );

        dlg.setVisible(true);

        if(!dlg.isSaved()) return;

        try{

            service.create(dlg.getCourse());

            reloadAll();

        }catch(Exception ex){

            ex.printStackTrace();
        }
    }

    private void onEdit(){

        int row = table.getSelectedRow();

        if(row<0){
            JOptionPane.showMessageDialog(this,"Select course");
            return;
        }

        Course c = tableModel.getCourse(row);

        CourseFormDialog dlg =
                new CourseFormDialog(
                        (Frame)SwingUtilities.getWindowAncestor(this),
                        "Edit Course",
                        c
                );

        dlg.setVisible(true);

        if(!dlg.isSaved()) return;

        try{

            service.update(dlg.getCourse());

            reloadAll();

        }catch(Exception ex){

            ex.printStackTrace();
        }
    }

    private void onDelete(){

        int row = table.getSelectedRow();

        if(row<0){
            JOptionPane.showMessageDialog(this,"Select course");
            return;
        }

        Course c = tableModel.getCourse(row);

        try{

            service.delete(c.getId());

            reloadAll();

        }catch(Exception ex){

            ex.printStackTrace();
        }
    }
}