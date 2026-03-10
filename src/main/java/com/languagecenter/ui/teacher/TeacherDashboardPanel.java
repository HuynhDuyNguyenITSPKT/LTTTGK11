package com.languagecenter.ui.teacher;

import com.formdev.flatlaf.FlatClientProperties;
import com.languagecenter.model.Class;
import com.languagecenter.model.enums.ClassStatus;
import com.languagecenter.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class TeacherDashboardPanel extends JPanel {
    private final Long teacherId;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;

    private final JLabel lblTotalClasses = new JLabel("0");
    private final JLabel lblOngoingClasses = new JLabel("0");
    private final JLabel lblTotalStudents = new JLabel("0");
    private final JLabel lblUpcomingClasses = new JLabel("0");
    private final JLabel lblCompletedClasses = new JLabel("0");
    private final JLabel lblActiveStudents = new JLabel("0");

    private final JTable tableClasses = new JTable();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"#", "Class Name", "Course", "Room", "Start Date", "End Date", "Students", "Status"}, 0
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
        
        // Classes Table Section
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
        
        JLabel lblTitle = new JLabel("📊 Teacher Dashboard - Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(30, 130, 76));
        
        JLabel lblSubtitle = new JLabel("Manage your classes and track student progress");
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
        
        // Row 1: Main metrics
        container.add(createModernStatCard("Total Classes", lblTotalClasses, 
            new Color(52, 152, 219), "📚", "All assigned classes"));
        container.add(createModernStatCard("Ongoing Classes", lblOngoingClasses, 
            new Color(46, 204, 113), "🎯", "Currently active"));
        container.add(createModernStatCard("Upcoming", lblUpcomingClasses, 
            new Color(241, 196, 15), "⏰", "Starting soon"));
        
        // Row 2: Student metrics
        container.add(createModernStatCard("Total Students", lblTotalStudents, 
            new Color(155, 89, 182), "👥", "All enrolled students"));
        container.add(createModernStatCard("Active Students", lblActiveStudents, 
            new Color(26, 188, 156), "✅", "Currently studying"));
        container.add(createModernStatCard("Completed", lblCompletedClasses, 
            new Color(149, 165, 166), "🏆", "Finished classes"));
        
        return container;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        
        // Table header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel lblTableTitle = new JLabel("📋 My Classes Details");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTableTitle.setForeground(new Color(44, 62, 80));
        
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, 
            "background:#1e824c; foreground:#ffffff; arc:8; borderWidth:0");
        btnRefresh.addActionListener(e -> reload());
        
        headerPanel.add(lblTableTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        
        // Table setup
        tableClasses.setModel(tableModel);
        tableClasses.setRowHeight(40);
        tableClasses.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableClasses.setSelectionBackground(new Color(232, 245, 233));
        tableClasses.setSelectionForeground(Color.BLACK);
        tableClasses.setGridColor(new Color(240, 240, 240));
        tableClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Table header styling
        tableClasses.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableClasses.getTableHeader().setBackground(new Color(30, 130, 76));
        tableClasses.getTableHeader().setForeground(Color.WHITE);
        tableClasses.getTableHeader().setPreferredSize(new Dimension(0, 45));
        
        // Column widths
        tableClasses.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableClasses.getColumnModel().getColumn(0).setMaxWidth(60);
        tableClasses.getColumnModel().getColumn(6).setPreferredWidth(80);
        tableClasses.getColumnModel().getColumn(6).setMaxWidth(100);
        tableClasses.getColumnModel().getColumn(7).setPreferredWidth(100);
        tableClasses.getColumnModel().getColumn(7).setMaxWidth(120);
        
        // Custom renderer with colors
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                // Color code status column
                if (column == 7 && value != null) {
                    String status = value.toString();
                    if (status.equals("Ongoing")) {
                        setForeground(new Color(46, 204, 113));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.equals("Upcoming")) {
                        setForeground(new Color(241, 196, 15));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.equals("Completed")) {
                        setForeground(new Color(149, 165, 166));
                    } else {
                        setForeground(Color.BLACK);
                    }
                } else if (!isSelected) {
                    setForeground(Color.BLACK);
                }
                
                return c;
            }
        };
        
        for (int i = 0; i < tableClasses.getColumnCount(); i++) {
            tableClasses.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(tableClasses);
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
            List<Class> allClasses = classService.getAll();
            List<Class> myClasses = allClasses.stream()
                    .filter(c -> c.getTeacher() != null && c.getTeacher().getId().equals(teacherId))
                    .sorted(Comparator.comparing(Class::getStartDate).reversed())
                    .toList();
            
            long totalClasses = myClasses.size();
            long ongoingClasses = myClasses.stream()
                    .filter(c -> c.getStatus() == ClassStatus.Ongoing)
                    .count();
            long upcomingClasses = myClasses.stream()
                    .filter(c -> c.getStatus() == ClassStatus.Planned || c.getStatus() == ClassStatus.Open)
                    .count();
            long completedClasses = myClasses.stream()
                    .filter(c -> c.getStatus() == ClassStatus.Completed)
                    .count();

            long totalStudents = 0;
            long activeStudents = 0;
            
            tableModel.setRowCount(0);

            int stt = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (Class cls : myClasses) {
                long studentCount = enrollmentService.countStudentsByClass(cls.getId());
                totalStudents += studentCount;
                
                if (cls.getStatus() == ClassStatus.Ongoing) {
                    activeStudents += studentCount;
                }

                tableModel.addRow(new Object[]{
                        stt++,
                        cls.getClassName(),
                        cls.getCourse() != null ? cls.getCourse().getCourseName() : "N/A",
                        cls.getRoom() != null ? cls.getRoom().getRoomName() : "N/A",
                        cls.getStartDate() != null ? cls.getStartDate().format(formatter) : "N/A",
                        cls.getEndDate() != null ? cls.getEndDate().format(formatter) : "N/A",
                        studentCount,
                        cls.getStatus() != null ? cls.getStatus().name() : "N/A"
                });
            }

            // Update labels
            lblTotalClasses.setText(String.valueOf(totalClasses));
            lblOngoingClasses.setText(String.valueOf(ongoingClasses));
            lblUpcomingClasses.setText(String.valueOf(upcomingClasses));
            lblCompletedClasses.setText(String.valueOf(completedClasses));
            lblTotalStudents.setText(String.valueOf(totalStudents));
            lblActiveStudents.setText(String.valueOf(activeStudents));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading dashboard data: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
