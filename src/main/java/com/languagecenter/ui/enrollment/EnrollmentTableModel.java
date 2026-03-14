package com.languagecenter.ui.enrollment;

import com.languagecenter.model.Enrollment;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentTableModel extends AbstractTableModel {

    private final String[] columns = {
            "STT","ID","Student","Class","Date","Status","Result"
    };

    private List<Enrollment> data = new ArrayList<>();

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public void setData(List<Enrollment> data){
        this.data = data;
        fireTableDataChanged();
    }

    public Enrollment getEnrollmentAt(int row){
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int row, int col) {

        Enrollment e = data.get(row);

        return switch(col){
            case 0 -> row + 1;
            case 1 -> e.getId();
            case 2 -> e.getStudent().getFullName();
            case 3 -> e.getClassEntity().getClassName();
            case 4 -> e.getEnrollmentDate().format(dateFormat);
            case 5 -> e.getStatus();
            case 6 -> e.getResult() == null ? "" : e.getResult();
            default -> "";
        };
    }
}