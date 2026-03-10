package com.languagecenter.ui.result;

import com.languagecenter.model.Class;
import com.languagecenter.model.Result;
import com.languagecenter.service.ClassService;
import com.languagecenter.service.ResultService;
import com.languagecenter.service.StudentService;
import com.languagecenter.stream.ResultStreamQueries;
import com.languagecenter.util.ResultExcelExporter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin panel — full CRUD for all results across all classes.
 * Supports filter by class (combobox), score range, and alphabetical sort by student name.
 * Allows exporting results of a selected class to Excel.
 */
public class ResultPanel extends JPanel {

    private final ResultService  resultService;
    private final StudentService studentService;
    private final ClassService   classService;

    private final ResultTableModel tableModel = new ResultTableModel();
    private final JTable           table      = new JTable(tableModel);

    private List<Result> allData = new ArrayList<>();
    private List<Class>  allClasses = new ArrayList<>();

    // ── Filter widgets ────────────────────────────────────────────────────────
    private final JTextField txtStudentFilter = new JTextField(12);
    private final JComboBox<ClassComboItem> cboClass = new JComboBox<>();
    private final JTextField txtMinScore = new JTextField(5);
    private final JTextField txtMaxScore = new JTextField(5);

    public ResultPanel(ResultService resultService,
                       StudentService studentService,
                       ClassService classService) {
        this.resultService  = resultService;
        this.studentService = studentService;
        this.classService   = classService;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 251));

        buildToolbar();
        buildTable();
        reload();
    }

    // ─── Build UI ─────────────────────────────────────────────────────────────

    private void buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JButton btnAdd     = createButton("Add",     new Color(34, 197, 94));
        JButton btnEdit    = createButton("Edit",    new Color(245, 158, 11));
        JButton btnDelete  = createButton("Delete",  new Color(239, 68, 68));
        JButton btnRefresh = createButton("Refresh", new Color(100, 116, 139));
        JButton btnExport  = createButton("Export Excel", new Color(21, 128, 61));

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);
        toolbar.add(btnExport);

        // Separator label
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(new JLabel("Student:"));
        toolbar.add(txtStudentFilter);

        toolbar.add(new JLabel("Class:"));
        // All-classes item
        cboClass.setPreferredSize(new Dimension(180, 28));
        cboClass.addItem(new ClassComboItem(null, "— All Classes —"));
        toolbar.add(cboClass);

        toolbar.add(new JLabel("Score:"));
        txtMinScore.setToolTipText("Min score");
        txtMaxScore.setToolTipText("Max score");
        toolbar.add(txtMinScore);
        toolbar.add(new JLabel("–"));
        toolbar.add(txtMaxScore);

        JButton btnFilter = createButton("Filter", new Color(79, 70, 229));
        toolbar.add(btnFilter);

        add(toolbar, BorderLayout.NORTH);

        btnAdd.addActionListener(    e -> onAdd());
        btnEdit.addActionListener(   e -> onEdit());
        btnDelete.addActionListener( e -> onDelete());
        btnRefresh.addActionListener(e -> reload());
        btnFilter.addActionListener( e -> applyFilter());
        btnExport.addActionListener( e -> onExport());
    }

    private void buildTable() {
        table.setRowHeight(32);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(200, 230, 255));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── Data ─────────────────────────────────────────────────────────────────

    public void reload() {
        try {
            allData    = resultService.getAll();
            allClasses = classService.getAll();

            // Refresh class combobox
            ClassComboItem selected = (ClassComboItem) cboClass.getSelectedItem();
            cboClass.removeAllItems();
            cboClass.addItem(new ClassComboItem(null, "— All Classes —"));
            for (Class c : allClasses)
                cboClass.addItem(new ClassComboItem(c.getId(), c.getClassName()));
            // Restore previous selection if possible
            if (selected != null && selected.id() != null) {
                for (int i = 0; i < cboClass.getItemCount(); i++) {
                    if (cboClass.getItemAt(i).id() != null &&
                            cboClass.getItemAt(i).id().equals(selected.id())) {
                        cboClass.setSelectedIndex(i);
                        break;
                    }
                }
            }

            // Apply current filter and sort
            applyFilter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFilter() {
        List<Result> result = new ArrayList<>(allData);

        // Filter by student name
        String studentKw = txtStudentFilter.getText();
        if (studentKw != null && !studentKw.isBlank())
            result = ResultStreamQueries.filterByStudent(result, studentKw);

        // Filter by class (combobox)
        ClassComboItem classItem = (ClassComboItem) cboClass.getSelectedItem();
        if (classItem != null && classItem.id() != null)
            result = ResultStreamQueries.filterByClassId(result, classItem.id());

        // Filter by score range
        String minTxt = txtMinScore.getText().trim();
        String maxTxt = txtMaxScore.getText().trim();
        if (!minTxt.isEmpty() || !maxTxt.isEmpty()) {
            try {
                BigDecimal min = minTxt.isEmpty() ? BigDecimal.ZERO       : new BigDecimal(minTxt);
                BigDecimal max = maxTxt.isEmpty() ? new BigDecimal("100") : new BigDecimal(maxTxt);
                result = ResultStreamQueries.filterByScoreRange(result, min, max);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Score must be a valid number!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Sort alphabetically by student name
        result = ResultStreamQueries.sortByStudentName(result);

        tableModel.setData(result);
    }

    // ─── Export ───────────────────────────────────────────────────────────────

    private void onExport() {
        // Determine which class to export
        ClassComboItem classItem = (ClassComboItem) cboClass.getSelectedItem();
        String className;
        List<Result> toExport;

        if (classItem == null || classItem.id() == null) {
            // No class selected → ask user to pick a class first
            if (allClasses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No classes available.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            // Offer a class selection dialog
            ClassComboItem[] items = new ClassComboItem[allClasses.size()];
            for (int i = 0; i < allClasses.size(); i++)
                items[i] = new ClassComboItem(allClasses.get(i).getId(), allClasses.get(i).getClassName());
            ClassComboItem chosen = (ClassComboItem) JOptionPane.showInputDialog(
                    this, "Select a class to export:", "Export Excel",
                    JOptionPane.QUESTION_MESSAGE, null, items, items[0]);
            if (chosen == null) return;
            className = chosen.name();
            toExport  = ResultStreamQueries.filterByClassId(allData, chosen.id());
        } else {
            className = classItem.name();
            toExport  = ResultStreamQueries.filterByClassId(allData, classItem.id());
        }

        if (toExport.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found for class: " + className,
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // File chooser
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("KetQua_" + className.replaceAll("[^\\w]", "_") + ".xlsx"));
        fc.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".xlsx")) path += ".xlsx";

        try {
            ResultExcelExporter.export(toExport, className, path);
            JOptionPane.showMessageDialog(this,
                    "Exported successfully!\n" + path,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    private void onAdd() {
        try {
            ResultFormDialog dlg = new ResultFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Add Result", null,
                    studentService.getAll(),
                    classService.getAll()
            );
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                resultService.create(dlg.getResult());
                reload();
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
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            return;
        }
        Result r = tableModel.getResultAt(table.convertRowIndexToModel(row));
        try {
            ResultFormDialog dlg = new ResultFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Edit Result", r,
                    studentService.getAll(),
                    classService.getAll()
            );
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                resultService.update(dlg.getResult());
                reload();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }
        Result r = tableModel.getResultAt(table.convertRowIndexToModel(row));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete result for \"" + r.getStudent().getFullName() + "\" in \""
                        + r.getClassEntity().getClassName() + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                resultService.delete(r.getId());
                reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ─── Inner record ─────────────────────────────────────────────────────────

    private record ClassComboItem(Long id, String name) {
        @Override public String toString() { return name; }
    }
}

