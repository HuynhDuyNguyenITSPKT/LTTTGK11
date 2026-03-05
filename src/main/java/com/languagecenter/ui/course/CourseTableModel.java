package com.languagecenter.ui.course;

import com.languagecenter.model.Course;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class CourseTableModel extends AbstractTableModel {

    private final String[] columns={
            "ID","Course Name","Level","Duration","Fee","Status","Description"
    };

    private List<Course> data = new ArrayList<>();

    public void setData(List<Course> data){

        this.data=data;

        fireTableDataChanged();
    }

    public Course getCourse(int row){

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
    public String getColumnName(int col){
        return columns[col];
    }

    @Override
    public Object getValueAt(int row,int col){

        Course c = data.get(row);

        return switch(col){

            case 0 -> c.getId();
            case 1 -> c.getCourseName();
            case 2 -> c.getLevel();
            case 3 -> c.getDuration()+" "+c.getDurationUnit();
            case 4 -> c.getFee();
            case 5 -> c.getStatus();
            case 6 -> c.getDescription();
            default -> "";
        };
    }
}