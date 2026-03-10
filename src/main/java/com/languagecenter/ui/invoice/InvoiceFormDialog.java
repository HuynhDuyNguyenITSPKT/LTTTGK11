package com.languagecenter.ui.invoice;

import com.languagecenter.model.Invoice;
import com.languagecenter.model.Student;
import com.languagecenter.model.enums.InvoiceStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class InvoiceFormDialog extends JDialog {

    private final JComboBox<Student> cboStudent = new JComboBox<>();
    private final JTextField txtTotalAmount = new JTextField();
    private final JTextField txtIssueDate   = new JTextField();
    private final JComboBox<InvoiceStatus> cboStatus = new JComboBox<>(InvoiceStatus.values());
    private final JTextArea txtNote = new JTextArea(3, 20);

    private boolean saved = false;
    private final Invoice invoice;
    private final boolean isEdit;

    public InvoiceFormDialog(Frame owner,
                             String title,
                             Invoice existing,
                             List<Student> students) {
        super(owner, title, true);

        this.invoice = existing != null ? existing : new Invoice();
        this.isEdit  = existing != null;

        students.forEach(cboStudent::addItem);

        // Populate fields before building UI so values are ready
        if (existing != null) {
            for (int i = 0; i < cboStudent.getItemCount(); i++) {
                if (cboStudent.getItemAt(i).getId().equals(existing.getStudent().getId())) {
                    cboStudent.setSelectedIndex(i);
                    break;
                }
            }
            txtTotalAmount.setText(String.format("%,.0f", existing.getTotalAmount()));
            txtIssueDate.setText(existing.getIssueDate().toString());
            cboStatus.setSelectedItem(existing.getStatus());
            txtNote.setText(existing.getNote() != null ? existing.getNote() : "");
            cboStudent.setEnabled(false);
        } else {
            txtIssueDate.setText(LocalDate.now().toString());
            cboStatus.setSelectedItem(InvoiceStatus.Issued);
        }

        // Amount is always read-only (auto-calculated from enrollment)
        txtTotalAmount.setEditable(false);
        txtTotalAmount.setBackground(new Color(241, 245, 249));
        txtTotalAmount.setForeground(new Color(71, 85, 105));
        txtTotalAmount.setFont(new Font("SansSerif", Font.BOLD, 13));
        txtTotalAmount.setToolTipText("Số tiền được tính tự động từ đăng ký học");

        buildUI();

        setSize(480, 460);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void buildUI() {
        // ── Colored header bar ──────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 64, 175));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel lblTitle = new JLabel(isEdit ? "Chỉnh sửa Hóa đơn" : "Tạo Hóa đơn");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblSub = new JLabel(isEdit ? "Cập nhật trạng thái và ghi chú" : "Nhập thông tin hóa đơn");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(new Color(147, 197, 253));
        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.add(lblTitle);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(lblSub);
        header.add(titleStack, BorderLayout.CENTER);

        // ── Form body ────────────────────────────────────────────
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 24, 10, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        int row = 0;

        // Student
        addRow(body, gbc, row++, makeLabel("Học viên"), cboStudent);

        // Total Amount (read-only)
        JPanel amountRow = new JPanel(new BorderLayout(6, 0));
        amountRow.setOpaque(false);
        amountRow.add(txtTotalAmount, BorderLayout.CENTER);
        JLabel lockIcon = new JLabel("🔒");
        lockIcon.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lockIcon.setToolTipText("Không được chỉnh sửa — tự động từ đăng ký");
        amountRow.add(lockIcon, BorderLayout.EAST);
        addRow(body, gbc, row++, makeLabel("Tổng tiền (VNĐ)"), amountRow);

        // Issue Date
        styleField(txtIssueDate);
        addRow(body, gbc, row++, makeLabel("Ngày lập (YYYY-MM-DD)"), txtIssueDate);

        // Status
        styleCombo(cboStatus);
        addRow(body, gbc, row++, makeLabel("Trạng thái"), cboStatus);

        // Note
        txtNote.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setBorder(new MatteBorder(1, 1, 1, 1, new Color(203, 213, 225)));
        JScrollPane scrollNote = new JScrollPane(txtNote);
        scrollNote.setPreferredSize(new Dimension(0, 70));
        addRow(body, gbc, row++, makeLabel("Ghi chú"), scrollNote);

        // ── Button bar ───────────────────────────────────────────
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnBar.setBackground(new Color(248, 250, 252));
        btnBar.setBorder(new MatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnCancel = createBtn("Hủy", new Color(100, 116, 139));
        JButton btnSave   = createBtn("Lưu thay đổi", new Color(30, 64, 175));

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        btnBar.add(btnCancel);
        btnBar.add(btnSave);

        // ── Compose ──────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(body,   BorderLayout.CENTER);
        add(btnBar, BorderLayout.SOUTH);
    }

    private void onSave() {
        try {
            invoice.setStudent((Student) cboStudent.getSelectedItem());
            // Amount is readonly — keep existing value; parse only for new (shouldn't happen since amount comes from enrollment)
            if (invoice.getTotalAmount() == null) {
                invoice.setTotalAmount(Double.parseDouble(txtTotalAmount.getText().replaceAll("[^\\d.]", "")));
            }
            invoice.setIssueDate(LocalDate.parse(txtIssueDate.getText().trim()));
            invoice.setStatus((InvoiceStatus) cboStatus.getSelectedItem());
            invoice.setNote(txtNote.getText().trim());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Dữ liệu không hợp lệ!\nĐịnh dạng ngày: YYYY-MM-DD",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, JLabel label, Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(71, 85, 105));
        lbl.setPreferredSize(new Dimension(170, 28));
        return lbl;
    }

    private void styleField(JTextField tf) {
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(new MatteBorder(1, 1, 1, 1, new Color(203, 213, 225)));
        tf.setPreferredSize(new Dimension(0, 32));
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public boolean isSaved() { return saved; }
    public Invoice getInvoice() { return invoice; }
}

