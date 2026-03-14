package com.languagecenter.ui.payment;

import com.languagecenter.model.Payment;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PaymentTableModel extends AbstractTableModel {

    private final String[] columns = {
            "STT", "ID", "Student", "Invoice ID", "Amount", "Payment Date", "Method", "Status", "Reference"
    };

    private List<Payment> data = new ArrayList<>();

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public void setData(List<Payment> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public Payment getPaymentAt(int row) {
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int row, int col) {

        Payment payment = data.get(row);

        return switch (col) {
            case 0 -> row + 1;
            case 1 -> payment.getId();
            case 2 -> payment.getStudent() != null ? payment.getStudent().getFullName() : "N/A";
            case 3 -> payment.getInvoice() != null ? payment.getInvoice().getId() : "N/A";
            case 4 -> String.format("%,.0f đ", payment.getAmount());
            case 5 -> payment.getPaymentDate().format(dateFormat);
            case 6 -> payment.getPaymentMethod();
            case 7 -> payment.getStatus();
            case 8 -> payment.getReferenceCode() != null ? payment.getReferenceCode() : "";
            default -> "";
        };
    }
}
