package com.languagecenter.ui.result;

import com.languagecenter.model.Result;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ResultTableModel extends AbstractTableModel {

    private final String[] columns = {
            "STT", "ID", "Student", "Class", "Score", "Grade", "Comment"
    };

    private List<Result> data = new ArrayList<>();

    public void setData(List<Result> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public Result getResultAt(int row) {
        return data.get(row);
    }

    @Override public int getRowCount()    { return data.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Result r = data.get(row);
        return switch (col) {
            case 0 -> row + 1;
            case 1 -> r.getId();
            case 2 -> r.getStudent() != null ? r.getStudent().getFullName() : "";
            case 3 -> r.getClassEntity() != null ? r.getClassEntity().getClassName() : "";
            case 4 -> r.getScore() != null ? r.getScore().toPlainString() : "";
            case 5 -> r.getGrade() != null ? r.getGrade() : "";
            case 6 -> r.getComment() != null ? r.getComment() : "";
            default -> "";
        };
    }
}
