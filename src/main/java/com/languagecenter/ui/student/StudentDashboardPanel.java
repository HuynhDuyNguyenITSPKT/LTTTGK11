package com.languagecenter.ui.student;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.Enrollment;
import com.languagecenter.model.Result;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class StudentDashboardPanel extends JPanel {
    private final Long studentId;
    private final EnrollmentService enrollmentService;

    private final JLabel lblTotalClasses = new JLabel("0");
    private final JLabel lblActiveClasses = new JLabel("0");
    private final JLabel lblCompletedClasses = new JLabel("0");
    private final JLabel lblPendingClasses = new JLabel("0");
    private final JLabel lblAverageGrade = new JLabel("N/A");
    private final JLabel lblTotalCredits = new JLabel("0");

    private final JTable tableEnrollments = new JTable();
    private final DefaultTableModel enrollmentTableModel = new DefaultTableModel(
            new String[]{"#", "Class Name", "Course", "Teacher", "Start Date", "Status", "Grade"}, 0
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
        setBorder(new EmptyBorder(25, 30, 25, 30));
        setBackground(new Color(245, 247, 250));

        buildUI();
        loadData();
    }

    public void reload() {
        loadData();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setOpaque(false);
        
        // Header Section
        JPanel headerPanel = createHeaderPanel();
        
        // Stats Panel - 2 rows
        JPanel statsPanel = createStatsPanel();
        
        // Enrollments Table Section
        JPanel tablePanel = createTablePanel();
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerContent = new JPanel(new BorderLayout(0, 15));
        centerContent.setOpaque(false);
        centerContent.add(statsPanel, BorderLayout.NORTH);
        centerContent.add(tablePanel, BorderLayout.CENTER);
        
        mainPanel.add(centerContent, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("📊 Student Dashboard - My Learning Journey");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(103, 58, 183));
        
        JLabel lblSubtitle = new JLabel("Track your progress and manage your courses");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(127, 140, 141));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(lblSubtitle);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel container = new JPanel(new GridLayout(2, 3, 15, 15));
        container.setOpaque(false);
        
        // Row 1: Class metrics
        container.add(createModernStatCard("Total Enrolled", lblTotalClasses, 
            new Color(52, 152, 219), "📚", "All enrolled classes"));
        container.add(createModernStatCard("Active Classes", lblActiveClasses, 
            new Color(46, 204, 113), "📖", "Currently studying"));
        container.add(createModernStatCard("Pending", lblPendingClasses, 
            new Color(241, 196, 15), "⏳", "Waiting to start"));
        
        // Row 2: Performance metrics
        container.add(createModernStatCard("Completed", lblCompletedClasses, 
            new Color(149, 165, 166), "✅", "Finished classes"));
        container.add(createModernStatCard("Average Grade", lblAverageGrade, 
            new Color(155, 89, 182), "⭐", "Overall performance"));
        container.add(createModernStatCard("Total Credits", lblTotalCredits, 
            new Color(26, 188, 156), "🎓", "Credits earned"));
        
        return container;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        
        // Table header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel lblTableTitle = new JLabel("📋 My Enrollments & Progress");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTableTitle.setForeground(new Color(44, 62, 80));
        
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, 
            "background:#673ab7; foreground:#ffffff; arc:8; borderWidth:0");
        btnRefresh.addActionListener(e -> reload());
        
        headerPanel.add(lblTableTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        
        // Table setup
        tableEnrollments.setModel(enrollmentTableModel);
        tableEnrollments.setRowHeight(42);
        tableEnrollments.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableEnrollments.setSelectionBackground(new Color(237, 231, 246));
        tableEnrollments.setSelectionForeground(Color.BLACK);
        tableEnrollments.setGridColor(new Color(240, 240, 240));
        tableEnrollments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Table header styling
        tableEnrollments.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableEnrollments.getTableHeader().setBackground(new Color(103, 58, 183));
        tableEnrollments.getTableHeader().setForeground(Color.WHITE);
        tableEnrollments.getTableHeader().setPreferredSize(new Dimension(0, 45));
        
        // Column widths
        tableEnrollments.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableEnrollments.getColumnModel().getColumn(0).setMaxWidth(60);
        tableEnrollments.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableEnrollments.getColumnModel().getColumn(4).setMaxWidth(120);
        tableEnrollments.getColumnModel().getColumn(5).setPreferredWidth(100);
        tableEnrollments.getColumnModel().getColumn(5).setMaxWidth(120);
        tableEnrollments.getColumnModel().getColumn(6).setPreferredWidth(80);
        tableEnrollments.getColumnModel().getColumn(6).setMaxWidth(100);
        
        // Custom renderer with colors
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                // Color code status column
                if (column == 5 && value != null) {
                    String status = value.toString().toLowerCase();
                    if (status.contains("active") || status.contains("enrolled")) {
                        setForeground(new Color(46, 204, 113));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.contains("completed")) {
                        setForeground(new Color(52, 152, 219));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.contains("pending")) {
                        setForeground(new Color(241, 196, 15));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(Color.BLACK);
                    }
                } 
                // Color code grade column
                else if (column == 6 && value != null && !value.toString().equals("N/A")) {
                    String gradeStatus = value.toString();
                    if (gradeStatus.equals("Passed")) {
                        setForeground(new Color(46, 204, 113));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (gradeStatus.equals("Failed")) {
                        setForeground(new Color(231, 76, 60));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (gradeStatus.equals("In Progress")) {
                        setForeground(new Color(241, 196, 15));
                    } else {
                        setForeground(Color.BLACK);
                    }
                } else if (!isSelected) {
                    setForeground(Color.BLACK);
                }
                
                return c;
            }
        };
        
        for (int i = 0; i < tableEnrollments.getColumnCount(); i++) {
            tableEnrollments.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(tableEnrollments);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createModernStatCard(String title, JLabel valueLabel, Color color, 
                                       String icon, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            new EmptyBorder(18, 20, 18, 20)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:12");
        
        // Icon panel
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(60, 60));
        
        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(70, 70, 70));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitle.setForeground(new Color(150, 150, 150));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblSubtitle);
        
        card.add(lblIcon, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }

    private void loadData() {
        try {
            // Load Enrollments
            List<Enrollment> enrollments = enrollmentService.getAll().stream()
                    .filter(e -> e.getStudent().getId().equals(studentId))
                    .sorted(Comparator.comparing(e -> e.getClassEntity().getStartDate(), 
                        Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();

            enrollmentTableModel.setRowCount(0);
            int stt = 1;
            long activeCount = 0;
            long completedCount = 0;
            long pendingCount = 0;
            double totalGrade = 0.0;

            int totalCredits = 0;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Enrollment enrollment : enrollments) {
                String className = enrollment.getClassEntity().getClassName();
                String courseName = enrollment.getClassEntity().getCourse() != null ?
                        enrollment.getClassEntity().getCourse().getCourseName() : "N/A";
                String teacherName = enrollment.getClassEntity().getTeacher() != null ?
                        enrollment.getClassEntity().getTeacher().getFullName() : "N/A";
                String startDate = enrollment.getClassEntity().getStartDate() != null ?
                        enrollment.getClassEntity().getStartDate().format(formatter) : "N/A";
                String status = enrollment.getStatus() != null ? enrollment.getStatus().toString() : "N/A";
                
                // Get grade from result status (simplified display)
                String gradeStr = "N/A";
                if (enrollment.getResult() != null) {
                    String resultStatus = enrollment.getResult().toString();
                    if (resultStatus.equals("Passed")) {
                        gradeStr = "Passed";
                    } else if (resultStatus.equals("Failed")) {
                        gradeStr = "Failed";
                    } else if (resultStatus.equals("In_Progress")) {
                        gradeStr = "In Progress";
                    }
                }

                enrollmentTableModel.addRow(new Object[]{
                        stt++,
                        className,
                        courseName,
                        teacherName,
                        startDate,
                        status,
                        gradeStr
                });

                // Count statuses
                String statusLower = status.toLowerCase();
                if (statusLower.contains("active") || statusLower.contains("enrolled")) {
                    activeCount++;
                    // Estimate 3 credits per active course
                    totalCredits += 3;
                } else if (statusLower.contains("completed") || statusLower.contains("finished")) {
                    completedCount++;
                    totalCredits += 3;
                } else if (statusLower.contains("pending") || statusLower.contains("registered")) {
                    pendingCount++;
                }
            }

            // Update labels
            lblTotalClasses.setText(String.valueOf(enrollments.size()));
            lblActiveClasses.setText(String.valueOf(activeCount));
            lblCompletedClasses.setText(String.valueOf(completedCount));
            lblPendingClasses.setText(String.valueOf(pendingCount));
            lblTotalCredits.setText(String.valueOf(totalCredits));
            
            // Calculate average grade from passed courses (simplified)
            long passedCount = enrollments.stream()
                .filter(e -> e.getResult() != null && e.getResult().toString().equals("Passed"))
                .count();
            
            if (passedCount > 0 && completedCount > 0) {
                double estimatedAvg = (passedCount * 100.0) / completedCount;
                lblAverageGrade.setText(String.format("%.0f%%", estimatedAvg));
            } else {
                lblAverageGrade.setText("N/A");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading dashboard data: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
