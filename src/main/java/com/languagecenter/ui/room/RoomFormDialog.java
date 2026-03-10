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

        JPanel container = new JPanel(new BorderLayout(10,10));
        container.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        container.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new GridLayout(4,1,12,12));
        form.setBackground(Color.WHITE);

        form.add(createField("Room Name", txtName));
        form.add(createField("Capacity", txtCapacity));
        form.add(createField("Location", txtLocation));
        form.add(createField("Status", cboStatus));

        JButton btnSave = createButton("Save", new Color(34,197,94));
        JButton btnCancel = createButton("Cancel", new Color(239,68,68));

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,5));
        actions.setBackground(Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnSave);

        container.add(form,BorderLayout.CENTER);
        container.add(actions,BorderLayout.SOUTH);

        setContentPane(container);
    }

    private JPanel createField(String label, JComponent field){

        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.setBackground(Color.WHITE);

        JLabel lb = new JLabel(label);
        lb.setFont(new Font("SansSerif",Font.BOLD,13));

        field.setFont(new Font("SansSerif",Font.PLAIN,13));
        field.setPreferredSize(new Dimension(200,30));

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200)),
                BorderFactory.createEmptyBorder(5,8,5,8)
        ));

        panel.add(lb,BorderLayout.NORTH);
        panel.add(field,BorderLayout.CENTER);

        return panel;
    }

    private JButton createButton(String text, Color color){

        JButton btn = new JButton(text);

        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif",Font.BOLD,12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90,32));

        return btn;
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