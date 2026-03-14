package com.languagecenter.ui.schedule;

import com.languagecenter.model.Schedule;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScheduleTableModel extends AbstractTableModel {

    private final String[] columns = {
            "STT","ID","Lớp","Phòng","Ngày học","Bắt đầu","Kết thúc"
    };

    private List<Schedule> data = new ArrayList<>();

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public void setData(List<Schedule> data){
        this.data = data;
        fireTableDataChanged();
    }

    public Schedule getScheduleAt(int row){
        return data.get(row);
    }

    @Override
    public int getRowCount() { return data.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int row, int col) {

        Schedule s = data.get(row);

        return switch(col){
            case 0 -> row + 1;
            case 1 -> s.getId();
            case 2 -> s.getClassEntity().getClassName();
            case 3 -> s.getRoom().getRoomName();
            case 4 -> s.getStudyDate().format(dateFormat);
            case 5 -> s.getStartTime().toString();
            case 6 -> s.getEndTime().toString();
            default -> "";
        };
    }
}