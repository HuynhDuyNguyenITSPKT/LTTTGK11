package com.languagecenter.ui.invoice;

import com.languagecenter.model.Invoice;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InvoiceTableModel extends AbstractTableModel {

    private final String[] columns = {
            "STT", "ID", "Student", "Class", "Issue Date", "Total Amount", "Status", "Note"
    };

    private List<Invoice> data = new ArrayList<>();

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public void setData(List<Invoice> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public Invoice getInvoiceAt(int row) {
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

        Invoice invoice = data.get(row);

        return switch (col) {
            case 0 -> row + 1;
            case 1 -> invoice.getId();
            case 2 -> invoice.getStudent() != null ? invoice.getStudent().getFullName() : "N/A";
            case 3 -> {
                if (invoice.getEnrollment() != null && invoice.getEnrollment().getClassEntity() != null) {
                    yield invoice.getEnrollment().getClassEntity().getClassName();
                }
                yield "N/A";
            }
            case 4 -> invoice.getIssueDate().format(dateFormat);
            case 5 -> String.format("%,.0f đ", invoice.getTotalAmount());
            case 6 -> invoice.getStatus();
            case 7 -> invoice.getNote() != null ? invoice.getNote() : "";
            default -> "";
        };
    }
}
