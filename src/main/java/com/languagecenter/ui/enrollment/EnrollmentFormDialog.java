package com.languagecenter.ui.enrollment;

import com.languagecenter.model.Class;
import com.languagecenter.model.Enrollment;
import com.languagecenter.model.Student;
import com.languagecenter.model.enums.EnrollmentStatus;
import com.languagecenter.model.enums.ResultStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class EnrollmentFormDialog extends JDialog {

    private final JComboBox<Student> cboStudent = new JComboBox<>();
    private final JComboBox<Class> cboClass = new JComboBox<>();

    private final JComboBox<EnrollmentStatus> cboStatus =
            new JComboBox<>(EnrollmentStatus.values());

    private final JComboBox<ResultStatus> cboResult =
            new JComboBox<>(ResultStatus.values());

    private final JTextField txtDate = new JTextField();

    private boolean saved = false;
    private final Enrollment enrollment;

    public EnrollmentFormDialog(Frame owner,
                                String title,
                                Enrollment existing,
                                List<Student> students,
                                List<Class> classes){

        super(owner,title,true);

        this.enrollment = existing != null ? existing : new Enrollment();

        students.forEach(cboStudent::addItem);
        classes.forEach(cboClass::addItem);

        setupRenderers();
        buildUI();

        if(existing != null){
            fillData(existing);
        } else {
            txtDate.setText(LocalDate.now().toString());
        }

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void setupRenderers(){

        ListCellRenderer<Object> renderer =
                new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,Object value,int index,
                    boolean isSelected,boolean cellHasFocus){

                if(value instanceof Student s)
                    value = s.getFullName();

                else if(value instanceof Class c)
                    value = c.getClassName();

                return super.getListCellRendererComponent(
                        list,value,index,isSelected,cellHasFocus);
            }
        };

        cboStudent.setRenderer(renderer);
        cboClass.setRenderer(renderer);
    }

    private void buildUI(){

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(20,20,20,20));
        root.setBackground(new Color(245,247,251));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addField(form,"Student",cboStudent,gbc,0);
        addField(form,"Class",cboClass,gbc,1);
        addField(form,"Enrollment Date",txtDate,gbc,2);
        addField(form,"Status",cboStatus,gbc,3);
        addField(form,"Result",cboResult,gbc,4);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        actions.setOpaque(false);

        JButton btnCancel = createButton("Cancel",new Color(156,163,175));
        JButton btnSave = createButton("Save",new Color(34,197,94));

        btnCancel.addActionListener(e->dispose());

        btnSave.addActionListener(e->onSave());

        actions.add(btnCancel);
        actions.add(btnSave);

        root.add(form,BorderLayout.CENTER);
        root.add(actions,BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addField(JPanel panel,
                          String label,
                          JComponent field,
                          GridBagConstraints gbc,
                          int row){

        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;

        JLabel lb = new JLabel(label);
        lb.setFont(new Font("Segoe UI",Font.BOLD,12));

        panel.add(lb,gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,210,210)),
                BorderFactory.createEmptyBorder(6,8,6,8)
        ));

        field.setFont(new Font("Segoe UI",Font.PLAIN,12));

        panel.add(field,gbc);
    }

    private JButton createButton(String text, Color color){

        JButton btn = new JButton(text);

        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI",Font.BOLD,12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90,34));

        return btn;
    }

    private void onSave(){

        try{

            enrollment.setStudent((Student)cboStudent.getSelectedItem());
            enrollment.setClassEntity((Class)cboClass.getSelectedItem());

            enrollment.setEnrollmentDate(
                    LocalDate.parse(txtDate.getText().trim())
            );

            enrollment.setStatus(
                    (EnrollmentStatus)cboStatus.getSelectedItem()
            );

            enrollment.setResult(
                    (ResultStatus)cboResult.getSelectedItem()
            );

            saved = true;
            dispose();

        }catch(Exception ex){

            JOptionPane.showMessageDialog(
                    this,
                    "Date format must be YYYY-MM-DD",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void fillData(Enrollment e){

    // Student
    for(int i = 0; i < cboStudent.getItemCount(); i++){
        Student s = cboStudent.getItemAt(i);
        if(s.getId().equals(e.getStudent().getId())){
            cboStudent.setSelectedIndex(i);
            break;
        }
    }

    // Class
    for(int i = 0; i < cboClass.getItemCount(); i++){
        Class c = cboClass.getItemAt(i);
        if(c.getId().equals(e.getClassEntity().getId())){
            cboClass.setSelectedIndex(i);
            break;
        }
    }

    if(e.getEnrollmentDate()!=null)
        txtDate.setText(e.getEnrollmentDate().toString());

    cboStatus.setSelectedItem(e.getStatus());
    cboResult.setSelectedItem(e.getResult());
}

    public boolean isSaved(){
        return saved;
    }

    public Enrollment getEnrollment(){
        return enrollment;
    }
}