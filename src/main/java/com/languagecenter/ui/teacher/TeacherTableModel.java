package com.languagecenter.ui.teacher;

import com.languagecenter.model.Teacher;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TeacherTableModel extends AbstractTableModel {
    private final String[] columns = {"ID", "Name", "Phone", "Email", "Specialty", "Status"};
    private List<Teacher> data = new ArrayList<>();

    public void setData(List<Teacher> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public Teacher getTeacher(int row) { return data.get(row); }
    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int row, int col) {
        Teacher t = data.get(row);
        return switch (col) {
            case 0 -> t.getId();
            case 1 -> t.getFullName();
            case 2 -> t.getPhone();
            case 3 -> t.getEmail();
            case 4 -> t.getSpecialty();
            case 5 -> t.getStatus();
            default -> "";
        };
    }
}