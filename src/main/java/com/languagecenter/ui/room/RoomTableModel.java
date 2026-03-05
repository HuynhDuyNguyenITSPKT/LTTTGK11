package com.languagecenter.ui.room;

import com.languagecenter.model.Room;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class RoomTableModel extends AbstractTableModel {

    private final String[] columns = {
            "ID","Room Name","Capacity","Location","Status"
    };

    private List<Room> data = new ArrayList<>();

    public void setData(List<Room> data){

        this.data = data;

        fireTableDataChanged();
    }

    public Room getRoom(int row){

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

        Room r = data.get(row);

        return switch(col){

            case 0 -> r.getId();
            case 1 -> r.getRoomName();
            case 2 -> r.getCapacity();
            case 3 -> r.getLocation();
            case 4 -> r.getStatus();
            default -> "";
        };
    }
}