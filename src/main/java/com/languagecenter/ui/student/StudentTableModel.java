package com.languagecenter.ui.student;

import com.languagecenter.model.Student;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StudentTableModel extends AbstractTableModel {
    private final String[] columns = {"ID", "Full Name", "Gender", "Phone", "Email", "Address", "Status"};
    private List<Student> data = new ArrayList<>();

    public void setData(List<Student> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public Student getStudent(int row) { return data.get(row); }
    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int row, int col) {
        Student s = data.get(row);
        return switch (col) {
            case 0 -> s.getId();
            case 1 -> s.getFullName();
            case 2 -> s.getGender();
            case 3 -> s.getPhone();
            case 4 -> s.getEmail();
            case 5 -> s.getAddress();
            case 6 -> s.getStatus();
            default -> "";
        };
    }
}