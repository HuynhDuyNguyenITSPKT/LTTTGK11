package com.languagecenter.ui.teacher;

import com.languagecenter.model.Teacher;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.TeacherStatus;
import com.languagecenter.service.TeacherService;
import com.languagecenter.stream.TeacherStreamQueries;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherPanel extends JPanel {

    private final TeacherService teacherService;

    private final JTextField txtSearch = new JTextField(15);

    private final JComboBox<TeacherStatus> cboStatus =
            new JComboBox<>(TeacherStatus.values());

    private final TeacherTableModel tableModel =
            new TeacherTableModel();

    private final JTable table =
            new JTable(tableModel);

    private List<Teacher> cachedTeachers = new ArrayList<>();

    public TeacherPanel(TeacherService teacherService){

        this.teacherService = teacherService;

        setLayout(new BorderLayout());

        buildUI();

        reloadAll();
    }

    private void buildUI(){

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        top.add(new JLabel("Search"));
        top.add(txtSearch);

        top.add(new JLabel("Status"));
        top.add(cboStatus);

        JButton btnSearch = new JButton("Search");

        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

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

            cachedTeachers =
                    teacherService.getAll();

            tableModel.setData(cachedTeachers);

        }catch(Exception ex){

            showError(ex);
        }
    }

    private void runFilter(){

        try{

            List<Teacher> result = cachedTeachers;

            String keyword = txtSearch.getText();

            if(keyword!=null && !keyword.isBlank()){

                result = TeacherStreamQueries
                        .searchByName(result,keyword);
            }

            TeacherStatus status =
                    (TeacherStatus)cboStatus.getSelectedItem();

            if(status!=null){

                result = TeacherStreamQueries
                        .filterByStatus(result,status);
            }

            tableModel.setData(result);

        }catch(Exception ex){

            showError(ex);
        }
    }

    private void onAdd(){

        TeacherFormDialog dlg =
                new TeacherFormDialog(
                        (Frame)SwingUtilities.getWindowAncestor(this),
                        "Add Teacher",
                        null,
                        null
                );

        dlg.setVisible(true);

        if(!dlg.isSaved()) return;

        try{

            teacherService.create(
                    dlg.getTeacher(),
                    dlg.getUsername(),
                    dlg.getPassword()
            );

            reloadAll();

        }catch(Exception ex){

            showError(ex);
        }
    }

    private void onEdit(){

        int row = table.getSelectedRow();

        if(row<0){
            JOptionPane.showMessageDialog(this,"Select teacher");
            return;
        }

        Teacher t = tableModel.getTeacher(row);

        try{

            UserAccount acc =
                    teacherService.findAccountByTeacherId(t.getId());

            String username =
                    acc!=null ? acc.getUsername() : "";

            TeacherFormDialog dlg =
                    new TeacherFormDialog(
                            (Frame)SwingUtilities.getWindowAncestor(this),
                            "Edit Teacher",
                            t,
                            username
                    );

            dlg.setVisible(true);

            if(!dlg.isSaved()) return;

            teacherService.update(
                    dlg.getTeacher(),
                    dlg.getUsername(),
                    dlg.getPassword()
            );

            reloadAll();

        }catch(Exception ex){

            showError(ex);
        }
    }

    private void onDelete(){

        int row = table.getSelectedRow();

        if(row<0){
            JOptionPane.showMessageDialog(this,"Select teacher");
            return;
        }

        Teacher t = tableModel.getTeacher(row);

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Delete teacher?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if(ok!=JOptionPane.YES_OPTION) return;

        try{

            teacherService.delete(t.getId());

            reloadAll();

        }catch(Exception ex){

            showError(ex);
        }
    }

    private void showError(Exception ex){

        ex.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}