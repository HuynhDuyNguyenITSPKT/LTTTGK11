package com.languagecenter.ui.student;

import com.languagecenter.model.Enrollment;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentDashboardPanel extends JPanel {
    private final Long studentId;
    private final EnrollmentService enrollmentService;

    private final JLabel lblTotalClasses = new JLabel("0");
    private final JLabel lblActiveClasses = new JLabel("0");
    private final JLabel lblCompletedClasses = new JLabel("0");

    private final JTable tableEnrollments = new JTable();
    private final DefaultTableModel enrollmentTableModel = new DefaultTableModel(
            new String[]{"STT", "Class Name", "Course", "Teacher", "Status", "Result"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public StudentDashboardPanel(Long studentId, EnrollmentService enrollmentService) {
        this.studentId = studentId;
        this.enrollmentService = enrollmentService;

        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 247, 250));

        buildUI();
        loadData();
    }

    public void reload() {
        loadData();
    }

    private void buildUI() {
        // Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Student Dashboard - My Learning Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(103, 58, 183));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("Total Classes", lblTotalClasses, new Color(52, 152, 219), "📚"));
        statsPanel.add(createStatCard("Active Classes", lblActiveClasses, new Color(46, 204, 113), "📖"));
        statsPanel.add(createStatCard("Completed Classes", lblCompletedClasses, new Color(149, 165, 166), "✅"));

        // Enrollments Table
        JPanel enrollmentPanel = new JPanel(new BorderLayout(0, 10));
        enrollmentPanel.setOpaque(false);
        JLabel lblEnrollmentTitle = new JLabel("My Enrollments & Classes");
        lblEnrollmentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblEnrollmentTitle.setForeground(new Color(44, 62, 80));

        tableEnrollments.setModel(enrollmentTableModel);
        tableEnrollments.setRowHeight(35);
        tableEnrollments.setFont(new Font("Arial", Font.PLAIN, 13));
        tableEnrollments.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tableEnrollments.getTableHeader().setBackground(new Color(103, 58, 183));
        tableEnrollments.getTableHeader().setForeground(Color.WHITE);
        tableEnrollments.setGridColor(new Color(230, 230, 230));

        // Set column width for STT
        tableEnrollments.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableEnrollments.getColumnModel().getColumn(0).setMaxWidth(70);

        // Center align all cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tableEnrollments.getColumnCount(); i++) {
            tableEnrollments.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollEnrollments = new JScrollPane(tableEnrollments);
        scrollEnrollments.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        enrollmentPanel.add(lblEnrollmentTitle, BorderLayout.NORTH);
        enrollmentPanel.add(scrollEnrollments, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(enrollmentPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setOpaque(false);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(127, 140, 141));

        textPanel.add(valueLabel);
        textPanel.add(lblTitle);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private void loadData() {
        try {
            // Load Enrollments
            List<Enrollment> enrollments = enrollmentService.getAll().stream()
                    .filter(e -> e.getStudent().getId().equals(studentId))
                    .toList();

            enrollmentTableModel.setRowCount(0);
            int stt = 1;
            long activeCount = 0;
            long completedCount = 0;

            for (Enrollment enrollment : enrollments) {
                enrollmentTableModel.addRow(new Object[]{
                        stt++,
                        enrollment.getClassEntity().getClassName(),
                        enrollment.getClassEntity().getCourse() != null ?
                                enrollment.getClassEntity().getCourse().getCourseName() : "N/A",
                        enrollment.getClassEntity().getTeacher() != null ?
                                enrollment.getClassEntity().getTeacher().getFullName() : "N/A",
                        enrollment.getStatus(),
                        enrollment.getResult()
                });

                // Count active and completed
                String status = enrollment.getStatus().toString().toLowerCase();
                if (status.contains("active") || status.contains("enrolled")) {
                    activeCount++;
                } else if (status.contains("completed") || status.contains("finished")) {
                    completedCount++;
                }
            }

            lblTotalClasses.setText(String.valueOf(enrollments.size()));
            lblActiveClasses.setText(String.valueOf(activeCount));
            lblCompletedClasses.setText(String.valueOf(completedCount));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading dashboard data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
