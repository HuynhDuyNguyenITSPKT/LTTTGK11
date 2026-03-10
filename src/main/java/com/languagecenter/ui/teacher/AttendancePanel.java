package com.languagecenter.ui.teacher;

import com.languagecenter.model.*;
import com.languagecenter.model.Class;
import com.languagecenter.model.enums.AttendanceStatus;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttendancePanel extends JPanel {
    private final Long teacherId;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final AttendanceService attendanceService;
    private final ScheduleService scheduleService;

    private final JComboBox<ClassItem>    cboClass    = new JComboBox<>();
    private final JComboBox<ScheduleItem> cboSchedule = new JComboBox<>();
    private final JLabel lblScheduleInfo = new JLabel(" ");
    private boolean isUpdating = false;

    private final JTable table = new JTable();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Student ID", "Full Name", "Status", "Note"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 || column == 3;
        }
    };

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public AttendancePanel(Long teacherId, ClassService classService,
                           EnrollmentService enrollmentService,
                           AttendanceService attendanceService,
                           ScheduleService scheduleService) {
        this.teacherId = teacherId;
        this.classService = classService;
        this.enrollmentService = enrollmentService;
        this.attendanceService = attendanceService;
        this.scheduleService = scheduleService;

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 247, 250));

        buildUI();
        loadClasses();
    }

    public void reload() {
        loadClasses();
    }

    private void buildUI() {
        // ── Title ──────────────────────────────────────────────
        JLabel lblTitle = new JLabel("Attendance Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 130, 76));
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        headerPanel.add(lblTitle);

        // ── Filter row ─────────────────────────────────────────
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        filterPanel.setOpaque(false);

        JLabel lblClass = new JLabel("Class:");
        lblClass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboClass.setPreferredSize(new Dimension(230, 33));
        cboClass.addActionListener(e -> {
            if (!isUpdating) loadScheduleDates();
        });

        JLabel lblSch = new JLabel("Session:");
        lblSch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboSchedule.setPreferredSize(new Dimension(310, 33));
        cboSchedule.addActionListener(e -> updateScheduleInfo());

        JButton btnLoad = new JButton("Load Students");
        btnLoad.setPreferredSize(new Dimension(130, 33));
        btnLoad.setBackground(new Color(37, 99, 235));
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.addActionListener(e -> loadStudentsForAttendance());

        filterPanel.add(lblClass);
        filterPanel.add(cboClass);
        filterPanel.add(lblSch);
        filterPanel.add(cboSchedule);
        filterPanel.add(btnLoad);

        // ── Schedule info label ───────────────────────────────
        lblScheduleInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblScheduleInfo.setForeground(new Color(80, 80, 110));
        lblScheduleInfo.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        // ── Toolbar ────────────────────────────────────────────
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JButton btnSave = makeBtn("Save Attendance", new Color(22, 163, 74));
        btnSave.addActionListener(e -> saveAttendance());

        JButton btnPresent = makeBtn("All Present", new Color(37, 99, 235));
        btnPresent.addActionListener(e -> markAllStatus(AttendanceStatus.Present));

        JButton btnAbsent = makeBtn("All Absent", new Color(220, 38, 38));
        btnAbsent.addActionListener(e -> markAllStatus(AttendanceStatus.Absent));

        JButton btnHistory = makeBtn("View History", new Color(109, 40, 217));
        btnHistory.addActionListener(e -> viewAttendanceHistory());

        toolBar.add(btnSave);
        toolBar.addSeparator();
        toolBar.add(btnPresent);
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(btnAbsent);
        toolBar.addSeparator();
        toolBar.add(btnHistory);

        // ── Table ──────────────────────────────────────────────
        table.setModel(tableModel);
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JComboBox<AttendanceStatus> statusCombo = new JComboBox<>(AttendanceStatus.values());
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusCombo));

        // Centre-align Student ID and Status columns
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 225)));

        // ── Combine top area ───────────────────────────────────
        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setOpaque(false);
        topArea.add(headerPanel);
        topArea.add(Box.createVerticalStrut(6));
        topArea.add(filterPanel);
        topArea.add(lblScheduleInfo);
        topArea.add(toolBar);

        add(topArea, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ── Data loading methods ───────────────────────────────────

    private void loadClasses() {
        try {
            List<Class> allClasses = classService.getAll();
            List<Class> myClasses = allClasses.stream()
                    .filter(c -> c.getTeacher() != null && c.getTeacher().getId().equals(teacherId))
                    .toList();

            isUpdating = true;
            cboClass.removeAllItems();
            for (Class cls : myClasses) {
                cboClass.addItem(new ClassItem(cls.getId(), cls.getClassName()));
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
        lblScheduleInfo.setText(" ");
        tableModel.setRowCount(0);

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

    private void updateScheduleInfo() {
        ScheduleItem item = (ScheduleItem) cboSchedule.getSelectedItem();
        if (item == null) {
            lblScheduleInfo.setText(" ");
            return;
        }
        lblScheduleInfo.setText("Phòng: " + item.room + "  |  Ca: " + item.startTime + " – " + item.endTime);
    }

    private void loadStudentsForAttendance() {
        ClassItem selectedClass = (ClassItem) cboClass.getSelectedItem();
        ScheduleItem selectedSchedule = (ScheduleItem) cboSchedule.getSelectedItem();

        if (selectedClass == null) {
            JOptionPane.showMessageDialog(this, "Please select a class first!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedSchedule == null) {
            JOptionPane.showMessageDialog(this, "No sessions scheduled for this class.\nPlease create a schedule first.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate selectedDate = selectedSchedule.studyDate;

        try {
            List<Enrollment> enrollments = enrollmentService.getAll().stream()
                    .filter(e -> e.getClassEntity().getId().equals(selectedClass.classId))
                    .toList();

            List<Attendance> existingAttendance = attendanceService.getByClassAndDate(
                    selectedClass.classId, selectedDate);

            tableModel.setRowCount(0);
            for (Enrollment enrollment : enrollments) {
                Student student = enrollment.getStudent();
                Attendance existing = existingAttendance.stream()
                        .filter(a -> a.getStudent().getId().equals(student.getId()))
                        .findFirst()
                        .orElse(null);

                AttendanceStatus status = existing != null ? existing.getStatus() : AttendanceStatus.Present;
                String note = existing != null ? existing.getNote() : "";

                tableModel.addRow(new Object[]{
                        student.getId(),
                        student.getFullName(),
                        status,
                        note
                });
            }

            if (enrollments.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students enrolled in this class.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAttendance() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No students to save attendance for!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ClassItem selectedClass = (ClassItem) cboClass.getSelectedItem();
        ScheduleItem selectedSchedule = (ScheduleItem) cboSchedule.getSelectedItem();
        if (selectedClass == null || selectedSchedule == null) return;

        LocalDate selectedDate = selectedSchedule.studyDate;

        try {
            List<Attendance> existingList = attendanceService.getByClassAndDate(
                    selectedClass.classId, selectedDate);

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Long studentId = (Long) tableModel.getValueAt(i, 0);
                AttendanceStatus status = (AttendanceStatus) tableModel.getValueAt(i, 2);
                String note = tableModel.getValueAt(i, 3) != null
                        ? tableModel.getValueAt(i, 3).toString() : "";

                Attendance existing = existingList.stream()
                        .filter(a -> a.getStudent().getId().equals(studentId))
                        .findFirst().orElse(null);

                if (existing != null) {
                    existing.setStatus(status);
                    existing.setNote(note);
                    attendanceService.update(existing);
                } else {
                    Student student = new Student();
                    student.setId(studentId);
                    Class classEntity = new Class();
                    classEntity.setId(selectedClass.classId);
                    attendanceService.create(
                            new Attendance(student, classEntity, selectedDate, status, note));
                }
            }
            JOptionPane.showMessageDialog(this, "Attendance saved successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markAllStatus(AttendanceStatus status) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(status, i, 2);
        }
    }

    private void viewAttendanceHistory() {
        ClassItem selectedClass = (ClassItem) cboClass.getSelectedItem();
        if (selectedClass == null) {
            JOptionPane.showMessageDialog(this, "Please select a class first!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Attendance> history = attendanceService.getByClassId(selectedClass.classId);

            if (history.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No attendance records found for this class.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Attendance History - " + selectedClass.className, true);
            dialog.setSize(820, 520);
            dialog.setLocationRelativeTo(this);

            DefaultTableModel historyModel = new DefaultTableModel(
                    new String[]{"Date", "Full Name", "Status", "Note"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            for (Attendance att : history) {
                historyModel.addRow(new Object[]{
                        att.getAttendDate().format(DATE_FMT),
                        att.getStudent().getFullName(),
                        att.getStatus(),
                        att.getNote()
                });
            }

            JTable historyTable = new JTable(historyModel);
            historyTable.setRowHeight(30);
            historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            dialog.add(new JScrollPane(historyTable));
            dialog.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading history: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ────────────────────────────────────────────────
    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    // ── Inner types ────────────────────────────────────────────
    private static class ClassItem {
        final Long classId;
        final String className;

        ClassItem(Long classId, String className) {
            this.classId = classId;
            this.className = className;
        }

        @Override
        public String toString() { return className; }
    }

    private static class ScheduleItem {
        final Long scheduleId;
        final LocalDate studyDate;
        final String room;
        final String startTime;
        final String endTime;

        ScheduleItem(Schedule s) {
            this.scheduleId = s.getId();
            this.studyDate  = s.getStudyDate();
            this.room       = (s.getRoom() != null) ? s.getRoom().getRoomName() : "—";
            this.startTime  = (s.getStartTime() != null)
                    ? s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
            this.endTime    = (s.getEndTime() != null)
                    ? s.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        }

        @Override
        public String toString() {
            return studyDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + "  (" + startTime + " – " + endTime + ")  – " + room;
        }
    }
}
