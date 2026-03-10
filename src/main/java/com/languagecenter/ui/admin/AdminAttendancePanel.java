package com.languagecenter.ui.admin;

import com.languagecenter.model.Attendance;
import com.languagecenter.model.Class;
import com.languagecenter.model.Schedule;
import com.languagecenter.model.enums.AttendanceStatus;
import com.languagecenter.service.AttendanceService;
import com.languagecenter.service.ClassService;
import com.languagecenter.service.ScheduleService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Admin portal — read-only view of attendance records across all classes.
 */
public class AdminAttendancePanel extends JPanel {

    private final AttendanceService attendanceService;
    private final ClassService      classService;
    private final ScheduleService   scheduleService;

    private final JComboBox<ClassItem>    cboClass    = new JComboBox<>();
    private final JComboBox<ScheduleItem> cboSchedule = new JComboBox<>();
    private final JLabel                  lblInfo     = new JLabel(" ");
    private boolean isUpdating = false;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"#", "Student ID", "Full Name", "Status", "Note"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JLabel lblTotal   = new JLabel("Total: 0");
    private final JLabel lblPresent = new JLabel("Present: 0");
    private final JLabel lblAbsent  = new JLabel("Absent: 0");
    private final JLabel lblLate    = new JLabel("Late: 0");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public AdminAttendancePanel(AttendanceService attendanceService,
                                ClassService classService,
                                ScheduleService scheduleService) {
        this.attendanceService = attendanceService;
        this.classService      = classService;
        this.scheduleService   = scheduleService;

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 247, 250));

        buildUI();
        loadClasses();
    }

    public void reload() {
        loadClasses();
    }

    // ── UI construction ───────────────────────────────────────

    private void buildUI() {
        // Title
        JLabel lblTitle = new JLabel("Attendance Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        headerPanel.add(lblTitle);

        // Filter row
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        filterPanel.setOpaque(false);

        JLabel lblCls = new JLabel("Class:");
        lblCls.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboClass.setPreferredSize(new Dimension(230, 33));
        cboClass.addActionListener(e -> { if (!isUpdating) loadScheduleDates(); });

        JLabel lblSch = new JLabel("Session:");
        lblSch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboSchedule.setPreferredSize(new Dimension(310, 33));
        cboSchedule.addActionListener(e -> updateInfo());

        JButton btnLoad = new JButton("Load Attendance");
        btnLoad.setPreferredSize(new Dimension(145, 33));
        btnLoad.setBackground(new Color(44, 62, 80));
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.addActionListener(e -> loadAttendance());

        filterPanel.add(lblCls);
        filterPanel.add(cboClass);
        filterPanel.add(lblSch);
        filterPanel.add(cboSchedule);
        filterPanel.add(btnLoad);

        // Info label (room + time)
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(new Color(80, 80, 110));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        // Stats bar
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 5));
        statsPanel.setBackground(new Color(236, 240, 241));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        Font statFont = new Font("Segoe UI", Font.BOLD, 13);
        lblTotal  .setFont(statFont); lblTotal  .setForeground(new Color(44,  62,  80));
        lblPresent.setFont(statFont); lblPresent.setForeground(new Color(22, 101,  52));
        lblAbsent .setFont(statFont); lblAbsent .setForeground(new Color(153, 27,  27));
        lblLate   .setFont(statFont); lblLate   .setForeground(new Color(120, 53,  15));

        statsPanel.add(lblTotal);
        statsPanel.add(new JLabel("|"));
        statsPanel.add(lblPresent);
        statsPanel.add(new JLabel("|"));
        statsPanel.add(lblAbsent);
        statsPanel.add(new JLabel("|"));
        statsPanel.add(lblLate);

        // Table
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new AttendanceRowRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 225)));

        // Top section assembly
        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setOpaque(false);
        topArea.add(headerPanel);
        topArea.add(Box.createVerticalStrut(6));
        topArea.add(filterPanel);
        topArea.add(lblInfo);
        topArea.add(Box.createVerticalStrut(4));
        topArea.add(statsPanel);

        add(topArea,    BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ── Data loading ──────────────────────────────────────────

    private void loadClasses() {
        try {
            List<Class> classes = classService.getAll();
            isUpdating = true;
            cboClass.removeAllItems();
            for (Class c : classes) {
                cboClass.addItem(new ClassItem(c.getId(), c.getClassName()));
            }
            isUpdating = false;
            loadScheduleDates();
        } catch (Exception e) {
            isUpdating = false;
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading classes: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadScheduleDates() {
        cboSchedule.removeAllItems();
        lblInfo.setText(" ");
        tableModel.setRowCount(0);
        resetStats();

        ClassItem selected = (ClassItem) cboClass.getSelectedItem();
        if (selected == null) return;

        try {
            List<Schedule> schedules = scheduleService.getByClass(selected.classId);
            for (Schedule s : schedules) {
                cboSchedule.addItem(new ScheduleItem(s));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateInfo() {
        ScheduleItem item = (ScheduleItem) cboSchedule.getSelectedItem();
        if (item == null) { lblInfo.setText(" "); return; }
        lblInfo.setText("Room: " + item.room + "  |  Time: " + item.startTime + " \u2013 " + item.endTime);
    }

    private void loadAttendance() {
        ClassItem    cls = (ClassItem)    cboClass.getSelectedItem();
        ScheduleItem sch = (ScheduleItem) cboSchedule.getSelectedItem();

        if (cls == null) {
            JOptionPane.showMessageDialog(this, "Please select a class first!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sch == null) {
            JOptionPane.showMessageDialog(this, "No sessions available for this class.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Attendance> records = attendanceService.getByClassAndDate(cls.classId, sch.studyDate);
            tableModel.setRowCount(0);
            int i = 1;
            for (Attendance a : records) {
                tableModel.addRow(new Object[]{
                        i++,
                        a.getStudent().getId(),
                        a.getStudent().getFullName(),
                        a.getStatus(),
                        a.getNote() != null ? a.getNote() : ""
                });
            }
            updateStats(records);

            if (records.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No attendance records for this session yet.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading attendance: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStats(List<Attendance> records) {
        long total   = records.size();
        long present = records.stream().filter(a -> a.getStatus() == AttendanceStatus.Present).count();
        long absent  = records.stream().filter(a -> a.getStatus() == AttendanceStatus.Absent).count();
        long late    = records.stream().filter(a -> a.getStatus() == AttendanceStatus.Late).count();
        lblTotal  .setText("Total: "   + total);
        lblPresent.setText("Present: " + present);
        lblAbsent .setText("Absent: "  + absent);
        lblLate   .setText("Late: "    + late);
    }

    private void resetStats() {
        lblTotal.setText("Total: 0"); lblPresent.setText("Present: 0");
        lblAbsent.setText("Absent: 0"); lblLate.setText("Late: 0");
    }

    // ── Row renderer ──────────────────────────────────────────

    private static class AttendanceRowRenderer extends DefaultTableCellRenderer {
        private static final Color COLOR_PRESENT = new Color(220, 252, 231);
        private static final Color COLOR_ABSENT  = new Color(254, 226, 226);
        private static final Color COLOR_LATE    = new Color(254, 243, 199);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // centre columns: # (0), Student ID (1), Status (3)
            if (column == 0 || column == 1 || column == 3) {
                setHorizontalAlignment(JLabel.CENTER);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }
            if (!isSelected) {
                Object status = table.getModel().getValueAt(row, 3);
                if (AttendanceStatus.Present.equals(status))     c.setBackground(COLOR_PRESENT);
                else if (AttendanceStatus.Absent.equals(status)) c.setBackground(COLOR_ABSENT);
                else if (AttendanceStatus.Late.equals(status))   c.setBackground(COLOR_LATE);
                else                                             c.setBackground(Color.WHITE);
            }
            return c;
        }
    }

    // ── Inner helper types ────────────────────────────────────

    private static class ClassItem {
        final Long classId;
        final String className;
        ClassItem(Long id, String name) { classId = id; className = name; }
        @Override public String toString() { return className; }
    }

    private static class ScheduleItem {
        final java.time.LocalDate studyDate;
        final String room, startTime, endTime;
        ScheduleItem(Schedule s) {
            studyDate = s.getStudyDate();
            room      = (s.getRoom() != null) ? s.getRoom().getRoomName() : "\u2014";
            startTime = (s.getStartTime() != null) ? s.getStartTime().format(TIME_FMT) : "";
            endTime   = (s.getEndTime()   != null) ? s.getEndTime()  .format(TIME_FMT) : "";
        }
        @Override public String toString() {
            return studyDate.format(DATE_FMT) + "  (" + startTime + " \u2013 " + endTime + ")  \u2013 " + room;
        }
    }
}
