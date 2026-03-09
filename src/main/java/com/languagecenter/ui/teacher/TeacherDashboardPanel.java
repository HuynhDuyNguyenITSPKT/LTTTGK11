package com.languagecenter.ui.teacher;

import com.languagecenter.model.Class;
import com.languagecenter.model.enums.ClassStatus;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TeacherDashboardPanel extends JPanel {
    private final Long teacherId;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;

    private final JLabel lblTotalClasses = new JLabel("0");
    private final JLabel lblOngoingClasses = new JLabel("0");
    private final JLabel lblTotalStudents = new JLabel("0");

    private final JTable tableClasses = new JTable();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"STT", "Class Name", "Course", "Room", "Start Date", "End Date", "Students", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public TeacherDashboardPanel(Long teacherId, ClassService classService,
                                 EnrollmentService enrollmentService) {
        this.teacherId = teacherId;
        this.classService = classService;
        this.enrollmentService = enrollmentService;

        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(new Color(245, 247, 250));

        buildUI();
        loadData();
    }

    private void buildUI() {
        // Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Teacher Dashboard - My Classes Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(30, 130, 76));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("Total Classes", lblTotalClasses, new Color(52, 152, 219), "📚"));
        statsPanel.add(createStatCard("Ongoing Classes", lblOngoingClasses, new Color(46, 204, 113), "🎯"));
        statsPanel.add(createStatCard("Total Students", lblTotalStudents, new Color(155, 89, 182), "👨‍🎓"));

        // Classes Table
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);
        JLabel lblTableTitle = new JLabel("My Classes");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTableTitle.setForeground(new Color(44, 62, 80));

        tableClasses.setModel(tableModel);
        tableClasses.setRowHeight(30);
        tableClasses.setFont(new Font("Arial", Font.PLAIN, 13));
        tableClasses.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tableClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column width for STT
        tableClasses.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableClasses.getColumnModel().getColumn(0).setMaxWidth(70);

        // Center align all cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tableClasses.getColumnCount(); i++) {
            tableClasses.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tableClasses);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        tablePanel.add(lblTableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

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
            List<Class> allClasses = classService.getAll();
            List<Class> myClasses = allClasses.stream()
                    .filter(c -> c.getTeacher() != null && c.getTeacher().getId().equals(teacherId))
                    .toList();

            long totalClasses = myClasses.size();
            long ongoingClasses = myClasses.stream()
                    .filter(c -> c.getStatus() == ClassStatus.Ongoing)
                    .count();

            long totalStudents = 0;
            tableModel.setRowCount(0);

            int stt = 1; // Số thứ tự
            for (Class cls : myClasses) {
                long studentCount = enrollmentService.countStudentsByClass(cls.getId());
                totalStudents += studentCount;

                tableModel.addRow(new Object[]{
                        stt++, // STT
                        cls.getClassName(),
                        cls.getCourse() != null ? cls.getCourse().getCourseName() : "N/A",
                        cls.getRoom() != null ? cls.getRoom().getRoomName() : "N/A",
                        cls.getStartDate(),
                        cls.getEndDate(),
                        studentCount,
                        cls.getStatus()
                });
            }

            lblTotalClasses.setText(String.valueOf(totalClasses));
            lblOngoingClasses.setText(String.valueOf(ongoingClasses));
            lblTotalStudents.setText(String.valueOf(totalStudents));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading dashboard data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
