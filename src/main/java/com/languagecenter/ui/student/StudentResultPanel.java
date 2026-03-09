package com.languagecenter.ui.student;

import com.languagecenter.model.Result;
import com.languagecenter.service.ResultService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Student portal — read-only view of the student's own learning results.
 */
public class StudentResultPanel extends JPanel {

    private final Long          studentId;
    private final ResultService resultService;

    private final ResultTableModel tableModel = new ResultTableModel();
    private final JTable           table      = new JTable(tableModel);
    private final JLabel           lblAvg     = new JLabel("Average Score: —");

    public StudentResultPanel(Long studentId, ResultService resultService) {
        this.studentId     = studentId;
        this.resultService = resultService;

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 245, 250));

        buildUI();
        reload();
    }

    public void reload() {
        try {
            List<Result> results = resultService.getByStudent(studentId);
            tableModel.setData(results);
            updateAverage(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("My Learning Results");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(103, 58, 183));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(120, 144, 156));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnRefresh.addActionListener(e -> reload());
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table
        table.setRowHeight(32);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(220, 200, 255));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);

        // Narrow fixed-width columns
        table.getColumnModel().getColumn(0).setPreferredWidth(45);   // STT
        table.getColumnModel().getColumn(0).setMaxWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(75);   // Mã KQ
        table.getColumnModel().getColumn(1).setMaxWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(65);   // Điểm
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Xếp loại

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Footer — average score
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        lblAvg.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvg.setForeground(new Color(103, 58, 183));
        footerPanel.add(lblAvg);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void updateAverage(List<Result> results) {
        long count = results.stream().filter(r -> r.getScore() != null).count();
        if (count == 0) {
            lblAvg.setText("Average Score: —");
            return;
        }
        double avg = results.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(r -> r.getScore().doubleValue())
                .average()
                .orElse(0);
        lblAvg.setText(String.format("Average Score: %.2f", avg));
    }

    // ─── Inner table model ────────────────────────────────────────────────────

    private static class ResultTableModel extends AbstractTableModel {
        private final String[] cols = {"STT", "Mã KQ", "Lớp học", "Điểm", "Xếp loại", "Nhận xét"};
        private List<Result> data = new ArrayList<>();

        void setData(List<Result> data) { this.data = data; fireTableDataChanged(); }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int row, int col) {
            Result r = data.get(row);
            return switch (col) {
                case 0 -> row + 1;                                                          // STT
                case 1 -> r.getId() != null ? "RS-" + r.getId() : "";                      // Mã KQ
                case 2 -> r.getClassEntity() != null ? r.getClassEntity().getClassName() : "";
                case 3 -> r.getScore()   != null ? r.getScore().toPlainString() : "—";
                case 4 -> r.getGrade()   != null ? r.getGrade()   : "—";
                case 5 -> r.getComment() != null ? r.getComment() : "—";
                default -> "";
            };
        }
    }
}
