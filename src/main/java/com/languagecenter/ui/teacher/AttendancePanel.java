package com.languagecenter.ui.teacher;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.*;
import com.languagecenter.model.Class;
import com.languagecenter.model.enums.AttendanceStatus;
import com.languagecenter.service.*;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AttendancePanel extends JPanel {
    private final Long teacherId;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final AttendanceService attendanceService;

    private final JComboBox<ClassItem> cboClass = new JComboBox<>();
    private final DatePicker datePicker = new DatePicker();
    private final JTable table = new JTable();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Student ID", "Student Name", "Status", "Note"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 || column == 3; // Status and Note columns are editable
        }
    };

    public AttendancePanel(Long teacherId, ClassService classService,
                          EnrollmentService enrollmentService,
                          AttendanceService attendanceService) {
        this.teacherId = teacherId;
        this.classService = classService;
        this.enrollmentService = enrollmentService;
        this.attendanceService = attendanceService;

        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 247, 250));

        buildUI();
        loadClasses();
    }

    public void reload() {
        loadClasses();
    }

    private void buildUI() {
        // Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Student Attendance Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 130, 76));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);

        JLabel lblClass = new JLabel("Select Class:");
        lblClass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboClass.setPreferredSize(new Dimension(300, 35));

        JLabel lblDate = new JLabel("Attendance Date:");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        datePicker.setPreferredSize(new Dimension(180, 35));
        datePicker.setDate(LocalDate.now());

        JButton btnLoad = new JButton("Load Students");
        btnLoad.setPreferredSize(new Dimension(150, 35));
        btnLoad.setBackground(new Color(52, 152, 219));
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.addActionListener(e -> loadStudentsForAttendance());

        filterPanel.add(lblClass);
        filterPanel.add(cboClass);
        filterPanel.add(lblDate);
        filterPanel.add(datePicker);
        filterPanel.add(btnLoad);

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JButton btnSave = new JButton("Save Attendance");
        btnSave.setIcon(UIManager.getIcon("Tree.closedIcon"));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveAttendance());

        JButton btnMarkAllPresent = new JButton("Mark All Present");
        btnMarkAllPresent.setBackground(new Color(52, 152, 219));
        btnMarkAllPresent.setForeground(Color.WHITE);
        btnMarkAllPresent.setFocusPainted(false);
        btnMarkAllPresent.addActionListener(e -> markAllStatus(AttendanceStatus.Present));

        JButton btnViewHistory = new JButton("View History");
        btnViewHistory.setBackground(new Color(155, 89, 182));
        btnViewHistory.setForeground(Color.WHITE);
        btnViewHistory.setFocusPainted(false);
        btnViewHistory.addActionListener(e -> viewAttendanceHistory());

        toolBar.add(btnSave);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(btnMarkAllPresent);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(btnViewHistory);

        // Table with Status ComboBox
        table.setModel(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add ComboBox for Status column
        JComboBox<AttendanceStatus> statusCombo = new JComboBox<>(AttendanceStatus.values());
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusCombo));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setOpaque(false);
        topPanel.add(filterPanel, BorderLayout.NORTH);
        topPanel.add(toolBar, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.BEFORE_FIRST_LINE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadClasses() {
        try {
            List<Class> allClasses = classService.getAll();
            List<Class> myClasses = allClasses.stream()
                    .filter(c -> c.getTeacher() != null && c.getTeacher().getId().equals(teacherId))
                    .toList();

            cboClass.removeAllItems();
            for (Class cls : myClasses) {
                cboClass.addItem(new ClassItem(cls.getId(), cls.getClassName()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading classes: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStudentsForAttendance() {
        if (cboClass.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a class first!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (datePicker.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Please select a date!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ClassItem selectedClass = (ClassItem) cboClass.getSelectedItem();
        LocalDate selectedDate = datePicker.getDate();

        try {
            // Get all enrollments for this class
            List<Enrollment> enrollments = enrollmentService.getAll().stream()
                    .filter(e -> e.getClassEntity().getId().equals(selectedClass.classId))
                    .toList();

            // Get existing attendance for this date
            List<Attendance> existingAttendance = attendanceService.getByClassAndDate(
                    selectedClass.classId, selectedDate);

            tableModel.setRowCount(0);

            for (Enrollment enrollment : enrollments) {
                Student student = enrollment.getStudent();

                // Check if attendance already exists for this student
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
                JOptionPane.showMessageDialog(this, "No students enrolled in this class!",
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
        LocalDate selectedDate = datePicker.getDate();

        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Long studentId = (Long) tableModel.getValueAt(i, 0);
                AttendanceStatus status = (AttendanceStatus) tableModel.getValueAt(i, 2);
                String note = (String) tableModel.getValueAt(i, 3);

                // Check if attendance already exists
                List<Attendance> existing = attendanceService.getByClassAndDate(
                        selectedClass.classId, selectedDate);

                Attendance existingForStudent = existing.stream()
                        .filter(a -> a.getStudent().getId().equals(studentId))
                        .findFirst()
                        .orElse(null);

                if (existingForStudent != null) {
                    // Update existing attendance
                    existingForStudent.setStatus(status);
                    existingForStudent.setNote(note);
                    attendanceService.update(existingForStudent);
                } else {
                    // Create new attendance
                    Student student = new Student();
                    student.setId(studentId);

                    Class classEntity = new Class();
                    classEntity.setId(selectedClass.classId);

                    Attendance attendance = new Attendance(student, classEntity, selectedDate, status, note);
                    attendanceService.create(attendance);
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
        if (cboClass.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a class first!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ClassItem selectedClass = (ClassItem) cboClass.getSelectedItem();

        try {
            List<Attendance> history = attendanceService.getByClassId(selectedClass.classId);

            if (history.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No attendance history for this class!",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create a dialog to show history
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Attendance History - " + selectedClass.className, true);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(this);

            DefaultTableModel historyModel = new DefaultTableModel(
                    new String[]{"Date", "Student Name", "Status", "Note"}, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Attendance att : history) {
                historyModel.addRow(new Object[]{
                        att.getAttendDate(),
                        att.getStudent().getFullName(),
                        att.getStatus(),
                        att.getNote()
                });
            }

            JTable historyTable = new JTable(historyModel);
            historyTable.setRowHeight(30);
            JScrollPane scrollPane = new JScrollPane(historyTable);

            dialog.add(scrollPane);
            dialog.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading attendance history: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper class for ComboBox
    private static class ClassItem {
        Long classId;
        String className;

        ClassItem(Long classId, String className) {
            this.classId = classId;
            this.className = className;
        }

        @Override
        public String toString() {
            return className;
        }
    }
}
