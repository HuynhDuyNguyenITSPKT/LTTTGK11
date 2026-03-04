package com.languagecenter.ui.student;

import com.languagecenter.model.Student;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.StudentStatus;
import com.languagecenter.service.StudentService;
import com.languagecenter.stream.StudentStreamQueries;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StudentPanel extends JPanel {

    private final StudentService studentService;

    private final JTextField txtSearch = new JTextField(15);

    private final JComboBox<StudentStatus> cboStatus =
            new JComboBox<>(StudentStatus.values());

    private final StudentTableModel tableModel =
            new StudentTableModel();

    private final JTable table =
            new JTable(tableModel);

    private List<Student> cachedStudents = new ArrayList<>();

    public StudentPanel(StudentService studentService){

        this.studentService = studentService;

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

            cachedStudents =
                    studentService.getAll();

            tableModel.setData(cachedStudents);

        }catch(Exception ex){

            showError(ex);
        }
    }

    private void runFilter(){

        try{

            List<Student> result = cachedStudents;

            String keyword = txtSearch.getText();

            if(keyword!=null && !keyword.isBlank()){

                result =
                        StudentStreamQueries
                                .searchByName(result,keyword);
            }

            StudentStatus status =
                    (StudentStatus)cboStatus.getSelectedItem();

            if(status!=null){

                result =
                        StudentStreamQueries
                                .filterByStatus(result,status);
            }

            tableModel.setData(result);

        }catch(Exception ex){

            showError(ex);
        }
    }

    private void onAdd(){

        StudentFormDialog dlg =
                new StudentFormDialog(
                        (Frame)SwingUtilities.getWindowAncestor(this),
                        "Add Student",
                        null,
                                null
                );

        dlg.setVisible(true);

        if(!dlg.isSaved()) return;

        try{

            studentService.create(
                    dlg.getStudent(),
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
            JOptionPane.showMessageDialog(this,"Select student");
            return;
        }

        Student s = tableModel.getStudent(row);

        try{

            UserAccount acc =
                    studentService.findAccountByStudentId(s.getId());

            String username =
                    acc!=null ? acc.getUsername() : "";

            StudentFormDialog dlg =
                    new StudentFormDialog(
                            (Frame)SwingUtilities.getWindowAncestor(this),
                            "Edit Student",
                            s,
                            username
                    );

            dlg.setVisible(true);

            if(!dlg.isSaved()) return;

            studentService.update(
                    dlg.getStudent(),
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
            JOptionPane.showMessageDialog(this,"Select student");
            return;
        }

        Student s = tableModel.getStudent(row);

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Delete student?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if(ok!=JOptionPane.YES_OPTION) return;

        try{

            studentService.delete(s.getId());

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