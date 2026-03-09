package com.languagecenter.ui.teacher;

import com.languagecenter.model.Class;
import com.languagecenter.model.Enrollment;
import com.languagecenter.model.Result;
import com.languagecenter.model.Student;
import com.languagecenter.service.ClassService;
import com.languagecenter.service.EnrollmentService;
import com.languagecenter.service.ResultService;
import com.languagecenter.ui.result.ResultFormDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Teacher panel — view and enter/edit results for students in the teacher's own classes.
 * Teacher can create and update results but cannot delete them.
 */
public class TeacherResultPanel extends JPanel {

    private final Long             teacherId;
    private final ClassService     classService;
    private final EnrollmentService enrollmentService;
    private final ResultService    resultService;

    // UI
    private final JComboBox<ClassItem> cboClass = new JComboBox<>();
    private final ResultTableModel     tableModel = new ResultTableModel();
    private final JTable               table      = new JTable(tableModel);

    private List<Class>  myClasses = new ArrayList<>();
    private List<Result> currentResults = new ArrayList<>();

    public TeacherResultPanel(Long teacherId,
                              ClassService classService,
                              EnrollmentService enrollmentService,
                              ResultService resultService) {
        this.teacherId         = teacherId;
        this.classService      = classService;
        this.enrollmentService = enrollmentService;
        this.resultService     = resultService;

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 247, 250));

        buildUI();
        reload();
    }

    public void reload() {
        loadMyClasses();
    }

    // ─── Build UI ─────────────────────────────────────────────────────────────

    private void buildUI() {
        // Header
        JLabel lblTitle = new JLabel("Student Results");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 130, 76));
        add(lblTitle, BorderLayout.NORTH);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterBar.setOpaque(false);

        JLabel lblClass = new JLabel("Class:");
        lblClass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboClass.setPreferredSize(new Dimension(280, 35));
        cboClass.addActionListener(e -> loadResultsForSelectedClass());

        JButton btnAdd  = createButton("Add Result",  new Color(76,  175,  80));
        JButton btnEdit = createButton("Edit Result",  new Color(255, 167, 38));

        filterBar.add(lblClass);
        filterBar.add(cboClass);
        filterBar.add(btnAdd);
        filterBar.add(btnEdit);

        add(filterBar, BorderLayout.BEFORE_FIRST_LINE);

        // Table
        buildTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
    }

    private void buildTable() {
        table.setRowHeight(32);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(200, 230, 255));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    // ─── Data loading ─────────────────────────────────────────────────────────

    private void loadMyClasses() {
        try {
            myClasses = classService.getAll().stream()
                    .filter(c -> c.getTeacher() != null
                            && c.getTeacher().getId().equals(teacherId))
                    .toList();
            cboClass.removeAllItems();
            for (Class c : myClasses)
                cboClass.addItem(new ClassItem(c.getId(), c.getClassName()));
            loadResultsForSelectedClass();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadResultsForSelectedClass() {
        ClassItem selected = (ClassItem) cboClass.getSelectedItem();
        if (selected == null) { tableModel.setData(List.of()); return; }
        try {
            currentResults = resultService.getByClass(selected.classId);
            tableModel.setData(currentResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get enrolled students for the given class.
     *  If an extra student is supplied (e.g. result entered by admin for a
     *  non-enrolled student), they are appended so the combo is never blank. */
    private List<Student> getEnrolledStudents(Long classId, Student extraIfMissing) {
        try {
            List<Student> enrolled = enrollmentService.getAll().stream()
                    .filter(en -> en.getClassEntity().getId().equals(classId))
                    .map(Enrollment::getStudent)
                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
            // Ensure the existing result's student is always present in the list
            if (extraIfMissing != null) {
                boolean found = enrolled.stream()
                        .anyMatch(s -> s.getId().equals(extraIfMissing.getId()));
                if (!found) enrolled.add(extraIfMissing);
            }
            return enrolled;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Class getSelectedClass() {
        ClassItem item = (ClassItem) cboClass.getSelectedItem();
        if (item == null) return null;
        return myClasses.stream()
                .filter(c -> c.getId().equals(item.classId))
                .findFirst().orElse(null);
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    private void onAdd() {
        Class clazz = getSelectedClass();
        if (clazz == null) {
            JOptionPane.showMessageDialog(this, "Please select a class first.");
            return;
        }
        List<Student> students = getEnrolledStudents(clazz.getId(), null);
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No enrolled students found for this class.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            ResultFormDialog dlg = new ResultFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Add Result", null, students, clazz
            );
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                resultService.create(dlg.getResult());
                loadResultsForSelectedClass();
                JOptionPane.showMessageDialog(this, "Result saved successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a row to edit."); return; }
        Result r = tableModel.getResultAt(table.convertRowIndexToModel(row));
        Class clazz = getSelectedClass();
        if (clazz == null) return;
        // Verify the result belongs to the selected class (safety guard)
        if (!r.getClassEntity().getId().equals(clazz.getId())) {
            JOptionPane.showMessageDialog(this,
                    "This result does not belong to the selected class.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Pass the result's student as extra to guarantee it appears in the combo
        List<Student> students = getEnrolledStudents(clazz.getId(), r.getStudent());
        try {
            ResultFormDialog dlg = new ResultFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Edit Result", r, students, clazz
            );
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                resultService.update(dlg.getResult());
                loadResultsForSelectedClass();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Inner classes ────────────────────────────────────────────────────────

    private record ClassItem(Long classId, String className) {
        @Override public String toString() { return className; }
    }

    /** Compact table model for teacher Result view */
    private static class ResultTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Student", "Score", "Grade", "Comment"};
        private List<Result> data = new ArrayList<>();

        void setData(List<Result> data) { this.data = data; fireTableDataChanged(); }
        Result getResultAt(int row) { return data.get(row); }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int row, int col) {
            Result r = data.get(row);
            return switch (col) {
                case 0 -> r.getId();
                case 1 -> r.getStudent() != null ? r.getStudent().getFullName() : "";
                case 2 -> r.getScore()   != null ? r.getScore().toPlainString() : "";
                case 3 -> r.getGrade()   != null ? r.getGrade()   : "";
                case 4 -> r.getComment() != null ? r.getComment() : "";
                default -> "";
            };
        }
    }
}
