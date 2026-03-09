package com.languagecenter.ui.result;

import com.languagecenter.model.Class;
import com.languagecenter.model.Result;
import com.languagecenter.model.Student;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ResultFormDialog extends JDialog {

    private final JComboBox<Student> cboStudent = new JComboBox<>();
    private final JComboBox<Class>   cboClass   = new JComboBox<>();
    private final JTextField         txtScore   = new JTextField();
    private final JTextField         txtGrade   = new JTextField();
    private final JTextField         txtComment = new JTextField();

    private boolean saved = false;
    private final Result result;

    /** Admin form: student and class are freely selectable on Add; locked on Edit */
    public ResultFormDialog(Frame owner, String title, Result existing,
                            List<Student> students, List<Class> classes) {
        super(owner, title, true);
        this.result = existing != null ? existing : new Result();

        students.forEach(cboStudent::addItem);
        classes.forEach(cboClass::addItem);

        if (existing != null) {
            // Match by ID — avoids detached-entity reference mismatch
            selectStudentById(existing.getStudent().getId());
            selectClassById(existing.getClassEntity().getId());
            cboStudent.setEnabled(false);
            cboClass.setEnabled(false);
            if (existing.getScore() != null)
                txtScore.setText(existing.getScore().toPlainString());
            if (existing.getGrade() != null)
                txtGrade.setText(existing.getGrade());
            if (existing.getComment() != null)
                txtComment.setText(existing.getComment());
        }

        buildUI(false);
        setSize(440, 300);
        setLocationRelativeTo(owner);
    }

    /**
     * Teacher form: class is fixed (selected from teacher's class list),
     * student is selectable from enrolled students of that class.
     */
    public ResultFormDialog(Frame owner, String title, Result existing,
                            List<Student> students, Class fixedClass) {
        super(owner, title, true);
        this.result = existing != null ? existing : new Result();

        students.forEach(cboStudent::addItem);
        cboClass.addItem(fixedClass);
        cboClass.setSelectedItem(fixedClass);
        cboClass.setEnabled(false);

        if (existing != null) {
            // Match by ID — avoids detached-entity reference mismatch
            selectStudentById(existing.getStudent().getId());
            cboStudent.setEnabled(false); // lock student on edit
            if (existing.getScore() != null)
                txtScore.setText(existing.getScore().toPlainString());
            if (existing.getGrade() != null)
                txtGrade.setText(existing.getGrade());
            if (existing.getComment() != null)
                txtComment.setText(existing.getComment());
        }

        buildUI(true);
        setSize(440, 300);
        setLocationRelativeTo(owner);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void selectStudentById(Long id) {
        for (int i = 0; i < cboStudent.getItemCount(); i++) {
            if (cboStudent.getItemAt(i).getId().equals(id)) {
                cboStudent.setSelectedIndex(i);
                return;
            }
        }
        // Student was entered by admin and is not in enrolled list;
        // show a placeholder so the combo is not blank.
        if (cboStudent.getItemCount() == 0 || result.getStudent() != null) {
            cboStudent.addItem(result.getStudent());
            cboStudent.setSelectedIndex(cboStudent.getItemCount() - 1);
        }
    }

    private void selectClassById(Long id) {
        for (int i = 0; i < cboClass.getItemCount(); i++) {
            if (cboClass.getItemAt(i).getId().equals(id)) {
                cboClass.setSelectedIndex(i);
                return;
            }
        }
    }

    private void buildUI(boolean classFixed) {
        setLayout(new GridLayout(6, 2, 10, 10));
        ((JComponent) getContentPane())
                .setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JLabel("Student"));
        add(cboStudent);

        add(new JLabel("Class"));
        add(cboClass);

        add(new JLabel("Score (0–100)"));
        add(txtScore);

        add(new JLabel("Grade (e.g. A, B+)"));
        add(txtGrade);

        add(new JLabel("Comment"));
        add(txtComment);

        JButton btnSave = new JButton("Save");
        btnSave.setBackground(new Color(76, 175, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> doSave());

        add(new JLabel());
        add(btnSave);
    }

    private void doSave() {
        try {
            result.setStudent((Student) cboStudent.getSelectedItem());
            result.setClassEntity((Class) cboClass.getSelectedItem());

            String scoreText = txtScore.getText().trim();
            if (scoreText.isEmpty()) {
                result.setScore(null);
            } else {
                BigDecimal score = new BigDecimal(scoreText);
                if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
                    JOptionPane.showMessageDialog(this, "Score must be between 0 and 100.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                result.setScore(score);
            }

            String grade = txtGrade.getText().trim();
            result.setGrade(grade.isEmpty() ? null : grade);

            String comment = txtComment.getText().trim();
            result.setComment(comment.isEmpty() ? null : comment);

            saved = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Score must be a valid number.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved()   { return saved; }
    public Result  getResult() { return result; }
}
