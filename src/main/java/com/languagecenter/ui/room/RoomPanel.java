package com.languagecenter.ui.room;

import com.languagecenter.model.Room;
import com.languagecenter.model.enums.RoomStatus;
import com.languagecenter.service.RoomService;
import com.languagecenter.stream.RoomStreamQueries;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RoomPanel extends JPanel {

    private final RoomService roomService;

    private final JTextField txtSearch = new JTextField(15);

    private final JComboBox<RoomStatus> cboStatus =
            new JComboBox<>(RoomStatus.values());

    private final RoomTableModel tableModel =
            new RoomTableModel();

    private final JTable table =
            new JTable(tableModel);

    private List<Room> cachedRooms = new ArrayList<>();

    public RoomPanel(RoomService roomService){

        this.roomService = roomService;

        setLayout(new BorderLayout());

        buildUI();

        reloadAll();
    }

    private void buildUI(){

        JPanel top = new JPanel();

        JButton btnSearch = new JButton("Search");
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

        top.add(new JLabel("Search"));
        top.add(txtSearch);

        top.add(new JLabel("Status"));
        top.add(cboStatus);

        top.add(btnSearch);
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);

        add(top,BorderLayout.NORTH);

        add(new JScrollPane(table),BorderLayout.CENTER);

        btnSearch.addActionListener(e -> runFilter());
        cboStatus.addActionListener(e -> runFilter());

        btnRefresh.addActionListener(e -> reloadAll());

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
    }

    private void reloadAll(){

        try{

            cachedRooms = roomService.getAll();

            tableModel.setData(cachedRooms);

        }catch(Exception ex){

            ex.printStackTrace();
        }
    }

    private void runFilter(){

        List<Room> result = cachedRooms;

        String keyword = txtSearch.getText();

        if(keyword!=null && !keyword.isBlank()){

            result = RoomStreamQueries
                    .searchByName(result,keyword);
        }

        RoomStatus status =
                (RoomStatus)cboStatus.getSelectedItem();

        if(status!=null){

            result = RoomStreamQueries
                    .filterByStatus(result,status);
        }

        tableModel.setData(result);
    }

    private void onAdd(){

        RoomFormDialog dlg =
                new RoomFormDialog(
                        (Frame)SwingUtilities.getWindowAncestor(this),
                        "Add Room",
                        null
                );

        dlg.setVisible(true);

        if(!dlg.isSaved()) return;

        try{

            roomService.create(dlg.getRoom());

            reloadAll();

        }catch(Exception ex){

            ex.printStackTrace();
        }
    }

    private void onEdit(){

        int row = table.getSelectedRow();

        if(row<0) return;

        Room r = tableModel.getRoom(row);

        RoomFormDialog dlg =
                new RoomFormDialog(
                        (Frame)SwingUtilities.getWindowAncestor(this),
                        "Edit Room",
                        r
                );

        dlg.setVisible(true);

        if(!dlg.isSaved()) return;

        try{

            roomService.update(dlg.getRoom());

            reloadAll();

        }catch(Exception ex){

            ex.printStackTrace();
        }
    }

    private void onDelete(){

        int row = table.getSelectedRow();

        if(row<0) return;

        Room r = tableModel.getRoom(row);

        try{

            roomService.delete(r.getId());

            reloadAll();

        }catch(Exception ex){

            ex.printStackTrace();
        }
    }
}