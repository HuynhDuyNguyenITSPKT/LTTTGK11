package com.languagecenter.ui.student;

import com.languagecenter.model.Student;
import com.languagecenter.model.enums.StudentStatus;
import com.languagecenter.stream.StudentStreamQueries;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudentStreamDialog extends JDialog {

    private List<Student> students;

    private final JTextArea txtResult = new JTextArea();

    public StudentStreamDialog(Frame owner, List<Student> students){

        super(owner,"Student Stream Queries",true);

        this.students = students;

        buildUI();

        setSize(600,400);
        setLocationRelativeTo(owner);
    }

    private void buildUI(){

        JButton btnSearch = new JButton("Search by name");
        JButton btnStatus = new JButton("Filter by status");

        btnSearch.addActionListener(e->runSearch());
        btnStatus.addActionListener(e->runFilter());

        JPanel top = new JPanel();

        top.add(btnSearch);
        top.add(btnStatus);

        txtResult.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));

        add(top,BorderLayout.NORTH);
        add(new JScrollPane(txtResult),BorderLayout.CENTER);
    }

    private void runSearch(){

        String keyword = JOptionPane.showInputDialog("Keyword");

        if(keyword==null) return;

        List<Student> result =
                StudentStreamQueries.searchByName(students,keyword);

        show(result);
    }

    private void runFilter(){

        StudentStatus status = (StudentStatus)
                JOptionPane.showInputDialog(
                        this,
                        "Status",
                        "Filter",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        StudentStatus.values(),
                        StudentStatus.Active
                );

        if(status==null) return;

        List<Student> result =
                StudentStreamQueries.filterByStatus(students,status);

        show(result);
    }

    private void show(List<Student> list){

        StringBuilder sb = new StringBuilder();

        for(Student s:list){

            sb.append(s.getId())
                    .append(" | ")
                    .append(s.getFullName())
                    .append(" | ")
                    .append(s.getStatus())
                    .append("\n");
        }

        txtResult.setText(sb.toString());
    }
}
