package com.languagecenter.ui.clas;

import com.languagecenter.model.Class;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ClassTableModel extends AbstractTableModel {
    private final String[] columns = {"ID", "Tên lớp", "Khóa học", "Giáo viên", "Phòng", "Sĩ số", "Trạng thái"};
    private List<Class> data = new ArrayList<>();

    public void setData(List<Class> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public Class getClassAt(int row) {
        return data.get(row);
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Class c = data.get(row);
        return switch(col) {
            case 0 -> c.getId();
            case 1 -> c.getClassName();
            case 2 -> c.getCourse() != null ? c.getCourse().getCourseName() : "N/A";
            case 3 -> c.getTeacher() != null ? c.getTeacher().getFullName() : "Chưa phân công";
            case 4 -> c.getRoom() != null ? c.getRoom().getRoomName() : "N/A";
            case 5 -> c.getMaxStudent();
            case 6 -> c.getStatus();
            default -> "";
        };
    }
}