package com.languagecenter.ui.student;

import com.languagecenter.model.Attendance;
import com.languagecenter.model.enums.AttendanceStatus;
import com.languagecenter.service.AttendanceService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Student portal — read-only view of the student's own attendance records.
 */
public class StudentAttendancePanel extends JPanel {

    private final Long studentId;
    private final AttendanceService attendanceService;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"#", "Date", "Class", "Status", "Note"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JLabel lblTotal   = new JLabel("Total Sessions: 0");
    private final JLabel lblPresent = new JLabel("Present: 0");
    private final JLabel lblAbsent  = new JLabel("Absent: 0");
    private final JLabel lblLate    = new JLabel("Late: 0");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public StudentAttendancePanel(Long studentId, AttendanceService attendanceService) {
        this.studentId = studentId;
        this.attendanceService = attendanceService;

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 245, 250));

        buildUI();
        reload();
    }

    public void reload() {
        try {
            List<Attendance> records = attendanceService.getByStudentId(studentId);
            populateTable(records);
            updateStats(records);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateTable(List<Attendance> records) {
        tableModel.setRowCount(0);
        int stt = 1;
        for (Attendance a : records) {
            String className = (a.getClassEntity() != null)
                    ? a.getClassEntity().getClassName() : "—";
            String date = (a.getAttendDate() != null)
                    ? a.getAttendDate().format(DATE_FMT) : "—";
            tableModel.addRow(new Object[]{
                    stt++,
                    date,
                    className,
                    a.getStatus(),
                    a.getNote() != null ? a.getNote() : ""
            });
        }
    }

    private void updateStats(List<Attendance> records) {
        long total   = records.size();
        long present = records.stream().filter(a -> a.getStatus() == AttendanceStatus.Present).count();
        long absent  = records.stream().filter(a -> a.getStatus() == AttendanceStatus.Absent).count();
        long late    = records.stream().filter(a -> a.getStatus() == AttendanceStatus.Late).count();

        lblTotal  .setText("Total Sessions: " + total);
        lblPresent.setText("Present: " + present);
        lblAbsent .setText("Absent: "  + absent);
        lblLate   .setText("Late: "    + late);
    }

    private void buildUI() {
        // ── Header ─────────────────────────────────────────────
        JLabel lblTitle = new JLabel("My Attendance");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(103, 58, 183));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(120, 144, 156));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> reload());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(lblTitle,   BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        // ── Stats bar ──────────────────────────────────────────
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statsPanel.setBackground(new Color(237, 233, 254));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 181, 253)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        Font statFont = new Font("Segoe UI", Font.BOLD, 13);
        lblTotal  .setFont(statFont); lblTotal  .setForeground(new Color(55, 48, 163));
        lblPresent.setFont(statFont); lblPresent.setForeground(new Color(22, 101, 52));
        lblAbsent .setFont(statFont); lblAbsent .setForeground(new Color(153, 27, 27));
        lblLate   .setFont(statFont); lblLate   .setForeground(new Color(120, 53, 15));

        statsPanel.add(lblTotal);
        statsPanel.add(new JLabel("|"));
        statsPanel.add(lblPresent);
        statsPanel.add(new JLabel("|"));
        statsPanel.add(lblAbsent);
        statsPanel.add(new JLabel("|"));
        statsPanel.add(lblLate);

        // ── Table ──────────────────────────────────────────────
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new AttendanceRowRenderer());

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 225)));

        // ── Top section ────────────────────────────────────────
        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(headerPanel, BorderLayout.NORTH);
        topSection.add(statsPanel,  BorderLayout.SOUTH);

        add(topSection,  BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
    }

        // ── Row renderer: colour by status + centre alignment ────
    private static class AttendanceRowRenderer extends DefaultTableCellRenderer {
        private static final Color COLOR_PRESENT = new Color(220, 252, 231); // light green
        private static final Color COLOR_ABSENT  = new Color(254, 226, 226); // light red
        private static final Color COLOR_LATE    = new Color(254, 243, 199); // light amber
        private static final Color COLOR_DEFAULT = Color.WHITE;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // centre columns: # (0), Date (1), Status (3)
            if (column == 0 || column == 1 || column == 3) {
                setHorizontalAlignment(JLabel.CENTER);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }
            if (!isSelected) {
                Object status = table.getModel().getValueAt(row, 3); // column 3 = status
                if (AttendanceStatus.Present.equals(status)) {
                    c.setBackground(COLOR_PRESENT);
                } else if (AttendanceStatus.Absent.equals(status)) {
                    c.setBackground(COLOR_ABSENT);
                } else if (AttendanceStatus.Late.equals(status)) {
                    c.setBackground(COLOR_LATE);
                } else {
                    c.setBackground(COLOR_DEFAULT);
                }
            }
            return c;
        }
    }
}
