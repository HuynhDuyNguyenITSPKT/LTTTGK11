package com.languagecenter.ui.payment;

import com.languagecenter.model.Invoice;
import com.languagecenter.model.Payment;
import com.languagecenter.model.Student;
import com.languagecenter.model.enums.InvoiceStatus;
import com.languagecenter.model.enums.PaymentMethod;
import com.languagecenter.model.enums.PaymentStatus;
import com.languagecenter.service.PaymentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentFormDialog extends JDialog {

    private final JComboBox<Student>       cboStudent     = new JComboBox<>();
    private final JComboBox<Invoice>       cboInvoice     = new JComboBox<>();
    private final JLabel                   lblInvoiceInfo = new JLabel(" ");
    private final JTextField               txtAmount      = new JTextField();
    private final JTextField               txtPaymentDate = new JTextField();
    private final JComboBox<PaymentMethod> cboPaymentMethod = new JComboBox<>(PaymentMethod.values());
    private final JComboBox<PaymentStatus> cboStatus        = new JComboBox<>(PaymentStatus.values());
    private final JTextField               txtReferenceCode = new JTextField();

    private boolean saved = false;
    private final Payment        payment;
    private final PaymentService paymentService;
    private final List<Invoice>  allInvoices;
    private final boolean        isEdit;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PaymentFormDialog(Frame owner,
                             String title,
                             Payment existing,
                             List<Student> students,
                             List<Invoice> invoices,
                             PaymentService paymentService) {
        super(owner, title, true);

        this.payment        = existing != null ? existing : new Payment();
        this.paymentService = paymentService;
        this.allInvoices    = invoices;
        this.isEdit         = existing != null;

        students.forEach(cboStudent::addItem);

        // ── Pre-populate ──────────────────────────────────────────────────────
        if (existing != null) {
            if (existing.getStudent() != null) {
                for (int i = 0; i < cboStudent.getItemCount(); i++) {
                    if (cboStudent.getItemAt(i).getId().equals(existing.getStudent().getId())) {
                        cboStudent.setSelectedIndex(i);
                        break;
                    }
                }
            }
            filterInvoicesByStudent();
            if (existing.getInvoice() != null) {
                for (int i = 0; i < cboInvoice.getItemCount(); i++) {
                    if (cboInvoice.getItemAt(i).getId().equals(existing.getInvoice().getId())) {
                        cboInvoice.setSelectedIndex(i);
                        break;
                    }
                }
            }
            updateInvoiceInfo();
            txtAmount.setText(existing.getAmount() != null ? String.format("%.0f", existing.getAmount()) : "");
            txtPaymentDate.setText(existing.getPaymentDate().format(DATE_FMT));
            cboPaymentMethod.setSelectedItem(existing.getPaymentMethod());
            cboStatus.setSelectedItem(existing.getStatus());
            txtReferenceCode.setText(existing.getReferenceCode() != null ? existing.getReferenceCode() : "");
            cboStudent.setEnabled(false);
        } else {
            txtPaymentDate.setText(LocalDateTime.now().format(DATE_FMT));
            cboStatus.setSelectedItem(PaymentStatus.Completed);
            filterInvoicesByStudent();
        }

        cboStudent.addActionListener(e -> { filterInvoicesByStudent(); updateInvoiceInfo(); });
        cboInvoice.addActionListener(e -> updateInvoiceInfo());

        buildUI();

        setSize(520, 560);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    private void filterInvoicesByStudent() {
        Student sel = (Student) cboStudent.getSelectedItem();
        cboInvoice.removeAllItems();
        if (sel == null) return;

        List<Invoice> filtered = allInvoices.stream()
                .filter(inv -> inv.getStudent() != null
                        && inv.getStudent().getId().equals(sel.getId())
                        && inv.getStatus() != InvoiceStatus.Paid)
                .collect(Collectors.toList());

        // Keep current invoice even if Paid (edit mode safety)
        if (isEdit && payment.getInvoice() != null) {
            boolean found = filtered.stream().anyMatch(i -> i.getId().equals(payment.getInvoice().getId()));
            if (!found && payment.getInvoice().getStudent() != null
                       && payment.getInvoice().getStudent().getId().equals(sel.getId())) {
                filtered.add(0, payment.getInvoice());
            }
        }

        filtered.forEach(cboInvoice::addItem);
        applyInvoiceRenderer();
    }

    private void applyInvoiceRenderer() {
        cboInvoice.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Invoice inv) {
                    String cls = inv.getEnrollment() != null && inv.getEnrollment().getClassEntity() != null
                            ? inv.getEnrollment().getClassEntity().getClassName() : "N/A";
                    setText(String.format("#%d — %s — %,.0f đ  [%s]",
                            inv.getId(), cls, inv.getTotalAmount(), inv.getStatus()));
                }
                return this;
            }
        });
    }

    private void updateInvoiceInfo() {
        Invoice inv = (Invoice) cboInvoice.getSelectedItem();
        if (inv == null) { lblInvoiceInfo.setText("<html><i>Chưa chọn hóa đơn</i></html>"); return; }
        try {
            Double remaining = paymentService.getRemainingAmount(inv.getId());
            String cls = inv.getEnrollment() != null && inv.getEnrollment().getClassEntity() != null
                    ? inv.getEnrollment().getClassEntity().getClassName() : "N/A";
            lblInvoiceInfo.setText(String.format(
                "<html><b>Lớp:</b> %s &nbsp;&nbsp; <b>Tổng:</b> %,.0f đ" +
                "&nbsp;&nbsp; <font color='#b45309'><b>Còn lại: %,.0f đ</b></font></html>",
                cls, inv.getTotalAmount(), remaining));
            if (!isEdit && txtAmount.getText().isBlank())
                txtAmount.setText(String.format("%.0f", remaining));
        } catch (Exception ex) {
            lblInvoiceInfo.setText("<html><font color='red'>Lỗi tải hóa đơn</font></html>");
        }
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void buildUI() {
        // ── Header ───────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(17, 94, 89));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(isEdit ? "Chỉnh sửa Thanh toán" : "Tạo Thanh toán mới");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(isEdit ? "Cập nhật thông tin giao dịch" : "Nhập thông tin thanh toán");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(new Color(153, 246, 228));

        titleStack.add(lblTitle);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(lblSub);
        header.add(titleStack, BorderLayout.CENTER);

        // ── Invoice info banner ───────────────────────────────────
        lblInvoiceInfo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblInvoiceInfo.setForeground(new Color(120, 53, 15));
        lblInvoiceInfo.setOpaque(true);
        lblInvoiceInfo.setBackground(new Color(255, 251, 235));
        lblInvoiceInfo.setBorder(new EmptyBorder(8, 16, 8, 16));

        // ── Form body ─────────────────────────────────────────────
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 24, 8, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        int row = 0;

        styleCombo(cboStudent);
        addRow(body, gbc, row++, lbl("Học viên"), cboStudent);

        addRow(body, gbc, row++, lbl("Hóa đơn"), cboInvoice);

        // Amount
        styleField(txtAmount);
        addRow(body, gbc, row++, lbl("Số tiền (VNĐ)"), txtAmount);

        // Payment date
        styleField(txtPaymentDate);
        addRow(body, gbc, row++, lbl("Ngày TT (yyyy-MM-dd HH:mm)"), txtPaymentDate);

        // Method
        styleCombo(cboPaymentMethod);
        addRow(body, gbc, row++, lbl("Phương thức TT"), cboPaymentMethod);

        // Status
        styleCombo(cboStatus);
        addRow(body, gbc, row++, lbl("Trạng thái"), cboStatus);

        // Reference code
        styleField(txtReferenceCode);
        txtReferenceCode.setToolTipText("Mã giao dịch ngân hàng / chuyển khoản (nếu có)");
        addRow(body, gbc, row++, lbl("Mã tham chiếu"), txtReferenceCode);

        // ── Button bar ────────────────────────────────────────────
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnBar.setBackground(new Color(248, 250, 252));
        btnBar.setBorder(new MatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnCancel = createBtn("Hủy",          new Color(100, 116, 139));
        JButton btnSave   = createBtn("Lưu thanh toán", new Color(17, 94, 89));

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        btnBar.add(btnCancel);
        btnBar.add(btnSave);

        // ── Compose ───────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(header,        BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setBackground(Color.WHITE);
        centerWrap.add(lblInvoiceInfo, BorderLayout.NORTH);
        centerWrap.add(body,           BorderLayout.CENTER);

        add(centerWrap, BorderLayout.CENTER);
        add(btnBar,     BorderLayout.SOUTH);
    }

    private void onSave() {
        try {
            Student selStudent = (Student) cboStudent.getSelectedItem();
            Invoice selInvoice = (Invoice) cboInvoice.getSelectedItem();

            if (selStudent == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Học viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selInvoice == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Hóa đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            payment.setInvoice(selInvoice);
            payment.setStudent(selInvoice.getStudent());
            payment.setEnrollment(selInvoice.getEnrollment());
            payment.setAmount(Double.parseDouble(txtAmount.getText().trim().replaceAll("[^\\d.]", "")));
            payment.setPaymentDate(LocalDateTime.parse(txtPaymentDate.getText().trim(), DATE_FMT));
            payment.setPaymentMethod((PaymentMethod) cboPaymentMethod.getSelectedItem());
            payment.setStatus((PaymentStatus) cboStatus.getSelectedItem());
            payment.setReferenceCode(txtReferenceCode.getText().trim());

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Dữ liệu không hợp lệ!\nĐịnh dạng ngày: yyyy-MM-dd HH:mm\nSố tiền phải là số",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, JLabel label, Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(new Color(71, 85, 105));
        l.setPreferredSize(new Dimension(185, 28));
        return l;
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
    public Payment getPayment() { return payment; }
}
