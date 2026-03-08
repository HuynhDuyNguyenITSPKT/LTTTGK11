package com.languagecenter.ui.payment;

import com.languagecenter.model.Invoice;
import com.languagecenter.model.Payment;
import com.languagecenter.model.Student;
import com.languagecenter.model.enums.InvoiceStatus;
import com.languagecenter.model.enums.PaymentMethod;
import com.languagecenter.model.enums.PaymentStatus;
import com.languagecenter.service.PaymentService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentFormDialog extends JDialog {

    private final JComboBox<Student> cboStudent = new JComboBox<>();
    private final JComboBox<Invoice> cboInvoice = new JComboBox<>();
    private final JLabel lblInvoiceInfo = new JLabel();
    private final JTextField txtAmount = new JTextField();
    private final JTextField txtPaymentDate = new JTextField();
    private final JComboBox<PaymentMethod> cboPaymentMethod =
            new JComboBox<>(PaymentMethod.values());
    private final JComboBox<PaymentStatus> cboStatus =
            new JComboBox<>(PaymentStatus.values());
    private final JTextField txtReferenceCode = new JTextField();

    private boolean saved = false;
    private final Payment payment;
    private final PaymentService paymentService;

    private final List<Invoice> allInvoices;

    private final DateTimeFormatter dateTimeFormat =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PaymentFormDialog(Frame owner,
                            String title,
                            Payment existing,
                            List<Student> students,
                            List<Invoice> invoices,
                            PaymentService paymentService) {

        super(owner, title, true);

        this.payment = existing != null ? existing : new Payment();
        this.paymentService = paymentService;
        this.allInvoices = invoices;

        // Load students
        students.forEach(cboStudent::addItem);

        buildUI();

        if (existing != null) {
            // Editing existing payment
            // Tìm student theo ID thay vì setSelectedItem trực tiếp
            if (existing.getStudent() != null) {
                for (int i = 0; i < cboStudent.getItemCount(); i++) {
                    Student s = cboStudent.getItemAt(i);
                    if (s.getId().equals(existing.getStudent().getId())) {
                        cboStudent.setSelectedIndex(i);
                        break;
                    }
                }
            }

            filterInvoicesByStudent();

            // Tìm invoice theo ID thay vì setSelectedItem trực tiếp
            if (existing.getInvoice() != null) {
                for (int i = 0; i < cboInvoice.getItemCount(); i++) {
                    Invoice inv = cboInvoice.getItemAt(i);
                    if (inv.getId().equals(existing.getInvoice().getId())) {
                        cboInvoice.setSelectedIndex(i);
                        break;
                    }
                }
            }

            updateInvoiceInfo();
            txtAmount.setText(existing.getAmount().toString());
            txtPaymentDate.setText(existing.getPaymentDate().format(dateTimeFormat));
            cboPaymentMethod.setSelectedItem(existing.getPaymentMethod());
            cboStatus.setSelectedItem(existing.getStatus());
            txtReferenceCode.setText(existing.getReferenceCode() != null ? existing.getReferenceCode() : "");

            // Disable student khi update
            cboStudent.setEnabled(false);
        } else {
            // New payment
            txtPaymentDate.setText(LocalDateTime.now().format(dateTimeFormat));
            cboStatus.setSelectedItem(PaymentStatus.Completed);
        }

        // Add listener to Student combo - filter invoices
        cboStudent.addActionListener(e -> {
            filterInvoicesByStudent();
            updateInvoiceInfo();
        });

        // Add listener to Invoice combo - update info and amount
        cboInvoice.addActionListener(e -> {
            updateInvoiceInfo();
        });

        setSize(500, 500);
        setLocationRelativeTo(owner);
    }

    private void filterInvoicesByStudent() {
        Student selectedStudent = (Student) cboStudent.getSelectedItem();
        cboInvoice.removeAllItems();

        if (selectedStudent == null) {
            return;
        }

        // Filter invoices: chỉ hiển thị invoice của student và chưa trả đủ (status != Paid)
        List<Invoice> unpaidInvoices = allInvoices.stream()
                .filter(inv -> inv.getStudent() != null
                        && inv.getStudent().getId().equals(selectedStudent.getId())
                        && inv.getStatus() != InvoiceStatus.Paid)
                .collect(Collectors.toList());

        // Nếu đang edit và invoice hiện tại không có trong danh sách (do đã Paid), thêm vào
        if (payment.getId() != null && payment.getInvoice() != null) {
            boolean containsCurrent = unpaidInvoices.stream()
                    .anyMatch(inv -> inv.getId().equals(payment.getInvoice().getId()));
            if (!containsCurrent && payment.getInvoice().getStudent() != null
                    && payment.getInvoice().getStudent().getId().equals(selectedStudent.getId())) {
                unpaidInvoices.add(0, payment.getInvoice()); // Thêm vào đầu danh sách
            }
        }

        unpaidInvoices.forEach(cboInvoice::addItem);

        // Custom renderer cho Invoice ComboBox
        cboInvoice.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                         int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Invoice inv) {
                    String className = "N/A";
                    if (inv.getEnrollment() != null && inv.getEnrollment().getClassEntity() != null) {
                        className = inv.getEnrollment().getClassEntity().getClassName();
                    }
                    setText(String.format("#%d - %s - %,.0f đ [%s]",
                            inv.getId(), className, inv.getTotalAmount(), inv.getStatus()));
                }
                return this;
            }
        });
    }

    private void updateInvoiceInfo() {
        Invoice selectedInvoice = (Invoice) cboInvoice.getSelectedItem();
        if (selectedInvoice != null) {
            try {
                Double remaining = paymentService.getRemainingAmount(selectedInvoice.getId());

                String className = "N/A";
                if (selectedInvoice.getEnrollment() != null
                    && selectedInvoice.getEnrollment().getClassEntity() != null) {
                    className = selectedInvoice.getEnrollment().getClassEntity().getClassName();
                }

                lblInvoiceInfo.setText(String.format(
                    "<html>Class: %s<br/>Total: %,.0f VNĐ<br/><b>Remaining: %,.0f VNĐ</b></html>",
                    className, selectedInvoice.getTotalAmount(), remaining));

                // Auto fill amount với số tiền còn thiếu (chỉ khi tạo mới)
                if (payment.getId() == null && txtAmount.getText().isEmpty()) {
                    txtAmount.setText(String.format("%.0f", remaining));
                }

            } catch (Exception ex) {
                lblInvoiceInfo.setText("Error loading invoice info");
            }
        } else {
            lblInvoiceInfo.setText("No invoice selected");
        }
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

        // Invoice
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel("Invoice:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(cboInvoice, gbc);

        row++;

        // Invoice Info (read-only)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        lblInvoiceInfo.setFont(lblInvoiceInfo.getFont().deriveFont(12f));
        lblInvoiceInfo.setForeground(new Color(50, 50, 150));
        lblInvoiceInfo.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        add(lblInvoiceInfo, gbc);
        gbc.gridwidth = 1;

        row++;

        // Amount
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel("Amount:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(txtAmount, gbc);

        row++;

        // Payment Date
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel("Date (yyyy-MM-dd HH:mm):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(txtPaymentDate, gbc);

        row++;

        // Payment Method
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel("Payment Method:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(cboPaymentMethod, gbc);

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

        // Reference Code
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel("Reference Code:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(txtReferenceCode, gbc);

        row++;

        // Button Save
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton btnSave = new JButton("Save");
        btnSave.setPreferredSize(new Dimension(100, 30));

        btnSave.addActionListener(e -> {
            try {
                Student selectedStudent = (Student) cboStudent.getSelectedItem();
                Invoice selectedInvoice = (Invoice) cboInvoice.getSelectedItem();

                if (selectedStudent == null) {
                    JOptionPane.showMessageDialog(this,
                        "Please select a Student!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (selectedInvoice == null) {
                    JOptionPane.showMessageDialog(this,
                        "Please select an Invoice!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Tự động set student và enrollment từ invoice
                payment.setInvoice(selectedInvoice);
                payment.setStudent(selectedInvoice.getStudent());
                payment.setEnrollment(selectedInvoice.getEnrollment());

                payment.setAmount(Double.parseDouble(txtAmount.getText().trim()));
                payment.setPaymentDate(LocalDateTime.parse(txtPaymentDate.getText().trim(), dateTimeFormat));
                payment.setPaymentMethod((PaymentMethod) cboPaymentMethod.getSelectedItem());
                payment.setStatus((PaymentStatus) cboStatus.getSelectedItem());
                payment.setReferenceCode(txtReferenceCode.getText().trim());

                saved = true;
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid input!\nDate format: yyyy-MM-dd HH:mm\nAmount must be a number",
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

    public Payment getPayment() {
        return payment;
    }
}
