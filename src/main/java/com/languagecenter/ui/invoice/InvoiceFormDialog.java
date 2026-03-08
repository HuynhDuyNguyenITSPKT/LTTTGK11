package com.languagecenter.ui.invoice;

import com.languagecenter.model.Invoice;
import com.languagecenter.model.Student;
import com.languagecenter.model.enums.InvoiceStatus;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class InvoiceFormDialog extends JDialog {

    private final JComboBox<Student> cboStudent = new JComboBox<>();
    private final JTextField txtTotalAmount = new JTextField();
    private final JTextField txtIssueDate = new JTextField();
    private final JComboBox<InvoiceStatus> cboStatus =
            new JComboBox<>(InvoiceStatus.values());
    private final JTextArea txtNote = new JTextArea(3, 20);

    private boolean saved = false;
    private final Invoice invoice;

    public InvoiceFormDialog(Frame owner,
                            String title,
                            Invoice existing,
                            List<Student> students) {

        super(owner, title, true);

        this.invoice = existing != null ? existing : new Invoice();

        students.forEach(cboStudent::addItem);

        buildUI();

        if (existing != null) {
            cboStudent.setSelectedItem(existing.getStudent());
            txtTotalAmount.setText(existing.getTotalAmount().toString());
            txtIssueDate.setText(existing.getIssueDate().toString());
            cboStatus.setSelectedItem(existing.getStatus());
            txtNote.setText(existing.getNote() != null ? existing.getNote() : "");

            // Disable student khi update vì invoice đã gắn với student cố định
            cboStudent.setEnabled(false);
        }

        setSize(450, 350);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        ((JComponent) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        int row = 0;

        // Student
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel("Student:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(cboStudent, gbc);

        row++;

        // Total Amount
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel("Total Amount:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(txtTotalAmount, gbc);

        row++;

        // Issue Date
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel("Issue Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(txtIssueDate, gbc);

        row++;

        // Status
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel("Status:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(cboStatus, gbc);

        row++;

        // Note
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Note:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        JScrollPane scrollPane = new JScrollPane(txtNote);
        add(scrollPane, gbc);

        row++;

        // Button Save
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton btnSave = new JButton("Save");
        btnSave.setPreferredSize(new Dimension(100, 30));

        btnSave.addActionListener(e -> {
            try {
                invoice.setStudent((Student) cboStudent.getSelectedItem());
                invoice.setTotalAmount(Double.parseDouble(txtTotalAmount.getText().trim()));
                invoice.setIssueDate(LocalDate.parse(txtIssueDate.getText().trim()));
                invoice.setStatus((InvoiceStatus) cboStatus.getSelectedItem());
                invoice.setNote(txtNote.getText().trim());

                saved = true;
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid input!\nDate format: YYYY-MM-DD\nAmount must be a number",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        add(btnSave, gbc);
    }

    public boolean isSaved() {
        return saved;
    }

    public Invoice getInvoice() {
        return invoice;
    }
}
