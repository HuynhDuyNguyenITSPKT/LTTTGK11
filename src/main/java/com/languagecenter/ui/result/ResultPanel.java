package com.languagecenter.ui.result;

import com.languagecenter.model.Result;
import com.languagecenter.service.ClassService;
import com.languagecenter.service.ResultService;
import com.languagecenter.service.StudentService;
import com.languagecenter.stream.ResultStreamQueries;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Admin panel — full CRUD for all results across all classes.
 */
public class ResultPanel extends JPanel {

    private final ResultService  resultService;
    private final StudentService studentService;
    private final ClassService   classService;

    private final ResultTableModel tableModel = new ResultTableModel();
    private final JTable           table      = new JTable(tableModel);

    private List<Result> allData;

    private final JTextField txtStudentFilter = new JTextField(12);
    private final JTextField txtClassFilter   = new JTextField(12);

    public ResultPanel(ResultService resultService,
                       StudentService studentService,
                       ClassService classService) {
        this.resultService  = resultService;
        this.studentService = studentService;
        this.classService   = classService;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        buildToolbar();
        buildTable();
        reload();
    }

    // ─── Build UI ─────────────────────────────────────────────────────────────

    private void buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(30, 136, 229));
        toolbar.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JButton btnAdd     = createButton("Add",     new Color(76,  175, 80));
        JButton btnEdit    = createButton("Edit",    new Color(255, 167, 38));
        JButton btnDelete  = createButton("Delete",  new Color(244,  67, 54));
        JButton btnRefresh = createButton("Refresh", new Color(120, 144, 156));

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        toolbar.add(new JLabel("  Student:"));
        toolbar.add(txtStudentFilter);

        toolbar.add(new JLabel("  Class:"));
        toolbar.add(txtClassFilter);

        JButton btnFilter = createButton("Filter", new Color(33, 150, 243));
        toolbar.add(btnFilter);

        add(toolbar, BorderLayout.NORTH);

        btnAdd.addActionListener(    e -> onAdd());
        btnEdit.addActionListener(   e -> onEdit());
        btnDelete.addActionListener( e -> onDelete());
        btnRefresh.addActionListener(e -> reload());
        btnFilter.addActionListener( e -> applyFilter());
    }

    private void buildTable() {
        table.setRowHeight(32);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(200, 230, 255));

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
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    // ─── Data ─────────────────────────────────────────────────────────────────

    public void reload() {
        try {
            allData = resultService.getAll();
            tableModel.setData(allData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFilter() {
        List<Result> filtered = allData;
        if (!txtStudentFilter.getText().isBlank())
            filtered = ResultStreamQueries.filterByStudent(filtered, txtStudentFilter.getText());
        if (!txtClassFilter.getText().isBlank())
            filtered = ResultStreamQueries.filterByClass(filtered, txtClassFilter.getText());
        tableModel.setData(filtered);
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
}
