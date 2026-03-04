package com.languagecenter.ui.student;

import com.languagecenter.model.Student;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StudentTableModel extends AbstractTableModel {

    private final String[] columns = {
            "ID","Name","Gender","Phone","Email","Status"
    };

    private List<Student> data = new ArrayList<>();

    public void setData(List<Student> data){
        this.data = data;
        fireTableDataChanged();
    }

    public Student getAt(int row){
        if(row < 0 || row >= data.size()) return null;
        return data.get(row);
    }

    @Override
    public int getRowCount(){
        return data.size();
    }

    @Override
    public int getColumnCount(){
        return columns.length;
    }

    @Override
    public String getColumnName(int column){
        return columns[column];
    }

    @Override
    public Object getValueAt(int row,int col){

        Student s = data.get(row);

        switch(col){
            case 0: return s.getId();
            case 1: return s.getFullName();
            case 2: return s.getGender();
            case 3: return s.getPhone();
            case 4: return s.getEmail();
            case 5: return s.getStatus();
        }

        return "";
    }
}