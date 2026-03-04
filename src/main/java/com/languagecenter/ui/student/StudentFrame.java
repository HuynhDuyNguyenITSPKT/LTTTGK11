package com.languagecenter.ui.student;
import com.languagecenter.model.Student;
import com.languagecenter.service.StudentService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
public class StudentFrame extends JFrame {

    private final StudentService studentService;

    private final StudentTableModel tableModel = new StudentTableModel();
    private final JTable table = new JTable(tableModel);

    private final JLabel lblSelected =
            new JLabel("Selected: (none)");

    private List<Student> cachedStudents = new ArrayList<>();

    private Student selectedStudent=null;

    public StudentFrame(StudentService studentService){

        super("Student Management");

        this.studentService=studentService;

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        buildUI();
        reloadAll();

        setSize(900,500);
        setLocationRelativeTo(null);
    }

    private void buildUI(){

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAdd=new JButton("Add");
        JButton btnEdit=new JButton("Edit");
        JButton btnDelete=new JButton("Delete");
        JButton btnRefresh=new JButton("Refresh");
        JButton btnStream=new JButton("Stream Queries");

        btnAdd.addActionListener(e->onAdd());
        btnEdit.addActionListener(e->onEdit());
        btnDelete.addActionListener(e->onDelete());
        btnRefresh.addActionListener(e->reloadAll());
        btnStream.addActionListener(e->onStream());

        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);
        top.add(btnStream);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::onRowSelected);

        JScrollPane scroll=new JScrollPane(table);

        JPanel bottom=new JPanel(new BorderLayout());
        bottom.add(lblSelected,BorderLayout.WEST);

        JPanel root=new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        root.add(top,BorderLayout.NORTH);
        root.add(scroll,BorderLayout.CENTER);
        root.add(bottom,BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void reloadAll(){

        try{

            cachedStudents = studentService.getAll();

            tableModel.setData(cachedStudents);

            selectedStudent=null;
            lblSelected.setText("Selected: (none)");

        }catch(Exception ex){
            showError(ex);
        }
    }

    private void onRowSelected(ListSelectionEvent e){

        if(e.getValueIsAdjusting()) return;

        int row = table.getSelectedRow();

        if(row<0){

            selectedStudent=null;
            lblSelected.setText("Selected: (none)");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        selectedStudent = tableModel.getAt(modelRow);

        lblSelected.setText(
                "Selected: ID="+selectedStudent.getId()
                        +" | "+selectedStudent.getFullName()
        );
    }

    private void onAdd(){

        StudentFormDialog dlg =
                new StudentFormDialog(this,"Add Student",null);

        dlg.setVisible(true);

        if(!dlg.isSaved()) return;

        try{

            studentService.create(dlg.getStudent());

            reloadAll();

        }catch(Exception ex){
            showError(ex);
        }
    }

    private void onEdit(){

        if(selectedStudent==null){

            JOptionPane.showMessageDialog(this,"Select student first");
            return;
        }

        StudentFormDialog dlg =
                new StudentFormDialog(this,"Edit Student",selectedStudent);

        dlg.setVisible(true);

        if(!dlg.isSaved()) return;

        try{

            studentService.update(dlg.getStudent());

            reloadAll();

        }catch(Exception ex){
            showError(ex);
        }
    }

    private void onDelete(){

        if(selectedStudent==null){

            JOptionPane.showMessageDialog(this,"Select student first");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Delete student ID="+selectedStudent.getId()+"?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if(ok!=JOptionPane.YES_OPTION) return;

        try{

            studentService.delete(selectedStudent.getId());

            reloadAll();

        }catch(Exception ex){
            showError(ex);
        }
    }

    private void onStream(){

        StudentStreamDialog dlg =
                new StudentStreamDialog(this,cachedStudents);

        dlg.setVisible(true);
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