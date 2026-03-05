package com.languagecenter.ui.room;

import com.languagecenter.model.Room;
import com.languagecenter.model.enums.RoomStatus;

import javax.swing.*;
import java.awt.*;

public class RoomFormDialog extends JDialog {

    private final JTextField txtName = new JTextField(20);
    private final JTextField txtCapacity = new JTextField(10);
    private final JTextField txtLocation = new JTextField(20);

    private final JComboBox<RoomStatus> cboStatus =
            new JComboBox<>(RoomStatus.values());

    private boolean saved = false;

    private Room room;

    public RoomFormDialog(Frame owner,
                          String title,
                          Room existing){

        super(owner,title,true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        buildUI();

        if(existing!=null){

            txtName.setText(existing.getRoomName());
            txtCapacity.setText(String.valueOf(existing.getCapacity()));
            txtLocation.setText(existing.getLocation());
            cboStatus.setSelectedItem(existing.getStatus());

            room = existing;

        }else{

            room = new Room();
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI(){

        JPanel form = new JPanel(new GridLayout(4,2,6,6));

        form.add(new JLabel("Room Name"));
        form.add(txtName);

        form.add(new JLabel("Capacity"));
        form.add(txtCapacity);

        form.add(new JLabel("Location"));
        form.add(txtLocation);

        form.add(new JLabel("Status"));
        form.add(cboStatus);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel();
        actions.add(btnSave);
        actions.add(btnCancel);

        add(form,BorderLayout.CENTER);
        add(actions,BorderLayout.SOUTH);
    }

    private void onSave(){

        try{

            room.setRoomName(txtName.getText());
            room.setCapacity(Integer.parseInt(txtCapacity.getText()));
            room.setLocation(txtLocation.getText());
            room.setStatus((RoomStatus)cboStatus.getSelectedItem());

            saved = true;

            dispose();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Validation",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved(){
        return saved;
    }

    public Room getRoom(){
        return room;
    }
}