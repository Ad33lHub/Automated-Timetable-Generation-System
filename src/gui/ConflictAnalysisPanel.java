package gui;

import com.formdev.flatlaf.FlatClientProperties;
import database.TimetableGenerator;
import database.TimetableGenerator.GenerationResult;
import database.TimetableGenerator.ConflictRecord;
import database.TeacherDAO;
import database.TimeSlotDAO;
import database.SubjectAssignmentDAO;
import models.Teacher;
import models.TimeSlot;
import models.SubjectAssignment;
import models.TimetableEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Conflict Analysis Panel
 * Yeh panel teacher availability conflicts detect karta hai aur resolution suggest karta hai.
 * Shows: teacher workload, unscheduled subjects, conflict reasons, and suggested fixes.
 */
public class ConflictAnalysisPanel extends JPanel {

    private final TimetableGenerator gen          = new TimetableGenerator();
    private final TeacherDAO         teacherDAO   = new TeacherDAO();
    private final TimeSlotDAO        slotDAO      = new TimeSlotDAO();
    private final SubjectAssignmentDAO saDAO      = new SubjectAssignmentDAO();
    private final database.TimetableDAO ttDAO     = new database.TimetableDAO();

    // ── Workload table ────────────────────────────────────────────────────────
    private final DefaultTableModel workloadModel = new DefaultTableModel(
        new String[]{"Teacher", "Specialization", "Available Days", "Courses Assigned", "Scheduled", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable workloadTable = new JTable(workloadModel);

    // ── Conflict table ────────────────────────────────────────────────────────
    private final DefaultTableModel conflictModel = new DefaultTableModel(
        new String[]{"Subject", "Section", "Teacher", "Conflict Reason", "Suggested Fix"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable conflictTable = new JTable(conflictModel);

    // ── Summary labels ────────────────────────────────────────────────────────
    private final JLabel lblScheduled  = summaryNum("—", StyleConstants.SUCCESS);
    private final JLabel lblConflicts  = summaryNum("—", StyleConstants.DANGER);
    private final JLabel lblTeachers   = summaryNum("—", StyleConstants.ACCENT);
    private final JLabel lblAvailIssue = summaryNum("—", StyleConstants.WARNING);

    private final JLabel statusLbl = new JLabel("Click 'Analyze Now' to run conflict detection.", SwingConstants.CENTER);

    public ConflictAnalysisPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(StyleConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // ── Header ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);
        JLabel title = new JLabel("Conflict Analysis");
        title.setFont(StyleConstants.FONT_TITLE);
        title.setForeground(StyleConstants.TEXT_PRIMARY);
        JLabel sub = StyleConstants.createSecondaryLabel(
            "Detect teacher availability conflicts, section overloads, and scheduling gaps");
        sub.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        titleGroup.add(title);
        titleGroup.add(sub);
        header.add(titleGroup, BorderLayout.WEST);

        JPanel actGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actGrp.setOpaque(false);
        JButton analyzeBtn = actionBtn("↻  Analyze Now", StyleConstants.ACCENT);
        JButton exportBtn  = actionBtn("📋  Export Report", StyleConstants.BG_SURFACE);
        exportBtn.setForeground(StyleConstants.TEXT_PRIMARY);
        analyzeBtn.addActionListener(e -> runAnalysis());
        exportBtn.addActionListener(e -> exportReport());
        actGrp.add(analyzeBtn);
        actGrp.add(exportBtn);
        header.add(actGrp, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Summary row ───────────────────────────────────────────────────
        JPanel summaryRow = new JPanel(new GridLayout(1, 4, 16, 0));
        summaryRow.setOpaque(false);
        summaryRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        summaryRow.add(summaryCard("Scheduled Classes", lblScheduled, StyleConstants.SUCCESS));
        summaryRow.add(summaryCard("Unresolved Conflicts", lblConflicts, StyleConstants.DANGER));
        summaryRow.add(summaryCard("Total Teachers", lblTeachers, StyleConstants.ACCENT));
        summaryRow.add(summaryCard("Availability Issues", lblAvailIssue, StyleConstants.WARNING));

        // ── Workload table card ───────────────────────────────────────────
        styleTable(workloadTable);
        workloadTable.getColumnModel().getColumn(5).setCellRenderer(statusBadgeRenderer());

        JScrollPane workloadScroll = new JScrollPane(workloadTable);
        workloadScroll.setBorder(BorderFactory.createEmptyBorder());
        workloadScroll.getViewport().setBackground(StyleConstants.BG_CARD);

        JPanel workloadCard = buildCard("Teacher Workload",
            "Courses assigned vs. slots available per teacher", workloadScroll);

        // ── Conflict table card ───────────────────────────────────────────
        styleTable(conflictTable);
        conflictTable.getColumnModel().getColumn(3).setCellRenderer(reasonRenderer());

        JScrollPane conflictScroll = new JScrollPane(conflictTable);
        conflictScroll.setBorder(BorderFactory.createEmptyBorder());
        conflictScroll.getViewport().setBackground(StyleConstants.BG_CARD);

        JPanel conflictCard = buildCard("Conflict Details",
            "Subjects that could not be scheduled and why", conflictScroll);

        // ── Status bar ────────────────────────────────────────────────────
        statusLbl.setFont(StyleConstants.FONT_SMALL);
        statusLbl.setForeground(StyleConstants.TEXT_MUTED);
        statusLbl.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // ── Assemble center ───────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(summaryRow,   BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, workloadCard, conflictCard);
        split.setDividerLocation(230);
        split.setResizeWeight(0.45);
        split.setOpaque(false);
        split.setBorder(null);
        split.setDividerSize(6);

        center.add(split,      BorderLayout.CENTER);
        center.add(statusLbl,  BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    // ── Public: run the full analysis ────────────────────────────────────────
    public void runAnalysis() {
        statusLbl.setText("Analyzing…");
        statusLbl.setForeground(StyleConstants.INFO);

        SwingUtilities.invokeLater(() -> {
            try {
                populateWorkloadTable();
                populateConflictTable();
                statusLbl.setText("Analysis complete — " + new java.util.Date());
                statusLbl.setForeground(StyleConstants.SUCCESS);
            } catch (Exception ex) {
                statusLbl.setText("Error: " + ex.getMessage());
                statusLbl.setForeground(StyleConstants.DANGER);
                ex.printStackTrace();
            }
        });
    }

    // ── Populate teacher workload table ───────────────────────────────────────
    private void populateWorkloadTable() {
        workloadModel.setRowCount(0);
        List<Teacher>          teachers    = teacherDAO.getAll();
        List<TimeSlot>         slots       = slotDAO.getAll();
        List<SubjectAssignment> assignments = saDAO.getAll();
        List<TimetableEntry>   scheduled   = ttDAO.getAll();

        // Count slots per teacher in the current timetable
        Map<Integer, Long> scheduledPerTeacher = scheduled.stream()
            .collect(Collectors.groupingBy(TimetableEntry::getTeacherId, Collectors.counting()));

        // Count assignments per teacher
        Map<Integer, Long> assignedPerTeacher = assignments.stream()
            .collect(Collectors.groupingBy(SubjectAssignment::getTeacherId, Collectors.counting()));

        int availIssues = 0;
        int totalScheduled = scheduled.size();

        for (Teacher t : teachers) {
            long availSlots = slots.stream()
                .filter(s -> gen.isTeacherAvailable(t.getAvailability(), s.getDay()))
                .count();
            long assigned   = assignedPerTeacher.getOrDefault(t.getId(), 0L);
            long sched      = scheduledPerTeacher.getOrDefault(t.getId(), 0L);

            String status;
            if (assigned == 0) {
                status = "No Courses";
            } else if (assigned > availSlots) {
                status = "OVERLOADED";
                availIssues++;
            } else if (sched < assigned) {
                status = "Partial";
                availIssues++;
            } else {
                status = "OK";
            }

            workloadModel.addRow(new Object[]{
                t.getName(), t.getSpecialization(),
                t.getAvailability(),
                assigned, sched + " / " + assigned,
                status
            });
        }

        // Update summary labels
        lblScheduled.setText(String.valueOf(totalScheduled));
        lblTeachers.setText(String.valueOf(teachers.size()));
        lblAvailIssue.setText(String.valueOf(availIssues));
    }

    // ── Populate conflict details table ───────────────────────────────────────
    private void populateConflictTable() {
        conflictModel.setRowCount(0);

        GenerationResult analysis = gen.analyzeCurrentState();
        lblConflicts.setText(String.valueOf(analysis.conflicts.size()));

        for (ConflictRecord c : analysis.conflicts) {
            conflictModel.addRow(new Object[]{
                c.subjectName, c.section, c.teacherName,
                c.reason,
                c.suggestion != null ? c.suggestion : "—"
            });
        }
    }

    // ── Export conflict report to text file ───────────────────────────────────
    private void exportReport() {
        try {
            java.io.File file = new java.io.File("Conflict_Analysis_Report.txt");
            java.io.PrintWriter pw = new java.io.PrintWriter(file);
            pw.println("AUTOMATED TIMETABLE — CONFLICT ANALYSIS REPORT");
            pw.println("Generated: " + new java.util.Date());
            pw.println("=".repeat(70));
            pw.println();
            pw.println("TEACHER WORKLOAD");
            pw.println("-".repeat(70));
            for (int r = 0; r < workloadModel.getRowCount(); r++) {
                pw.printf("%-30s  Days: %-40s  Assigned: %s  Scheduled: %s  Status: %s%n",
                    workloadModel.getValueAt(r, 0),
                    workloadModel.getValueAt(r, 2),
                    workloadModel.getValueAt(r, 3),
                    workloadModel.getValueAt(r, 4),
                    workloadModel.getValueAt(r, 5));
            }
            pw.println();
            pw.println("CONFLICT DETAILS");
            pw.println("-".repeat(70));
            for (int r = 0; r < conflictModel.getRowCount(); r++) {
                pw.printf("Subject  : %s [%s]%n", conflictModel.getValueAt(r, 0), conflictModel.getValueAt(r, 1));
                pw.printf("Teacher  : %s%n", conflictModel.getValueAt(r, 2));
                pw.printf("Reason   : %s%n", conflictModel.getValueAt(r, 3));
                pw.printf("Fix      : %s%n", conflictModel.getValueAt(r, 4));
                pw.println();
            }
            pw.close();
            JOptionPane.showMessageDialog(this,
                "Report saved to:\n" + file.getAbsolutePath(),
                "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── UI builders ───────────────────────────────────────────────────────────

    private JPanel buildCard(String title, String subtitle, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        StyleConstants.applyCardStyle(card);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(StyleConstants.FONT_SUBHEADING);
        t.setForeground(StyleConstants.TEXT_PRIMARY);
        JLabel s = StyleConstants.createSecondaryLabel(subtitle);
        titleRow.add(t, BorderLayout.WEST);
        titleRow.add(s, BorderLayout.EAST);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(content,  BorderLayout.CENTER);
        return card;
    }

    private JPanel summaryCard(String label, JLabel valueLbl, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        StyleConstants.applyCardStyle(card);
        card.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.MatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(StyleConstants.FONT_SMALL);
        nameLbl.setForeground(StyleConstants.TEXT_SECONDARY);

        card.add(nameLbl,   BorderLayout.NORTH);
        card.add(valueLbl,  BorderLayout.CENTER);
        return card;
    }

    private static JLabel summaryNum(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Inter", Font.BOLD, 32));
        l.setForeground(color);
        return l;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(46);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(StyleConstants.BORDER);
        table.getTableHeader().setBackground(StyleConstants.BG_CARD);
        table.getTableHeader().setForeground(StyleConstants.TEXT_SECONDARY);
        table.getTableHeader().setFont(StyleConstants.FONT_LABEL);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionBackground(new Color(
            StyleConstants.ACCENT.getRed(), StyleConstants.ACCENT.getGreen(),
            StyleConstants.ACCENT.getBlue(), 50));
        table.setSelectionForeground(StyleConstants.TEXT_PRIMARY);
        table.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
    }

    private DefaultTableCellRenderer statusBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String status = v == null ? "" : v.toString();
                switch (status) {
                    case "OK"         -> { lbl.setForeground(StyleConstants.SUCCESS);   lbl.setFont(new Font("Inter", Font.BOLD, 12)); }
                    case "OVERLOADED" -> { lbl.setForeground(StyleConstants.DANGER);    lbl.setFont(new Font("Inter", Font.BOLD, 12)); }
                    case "Partial"    -> { lbl.setForeground(StyleConstants.WARNING);   lbl.setFont(new Font("Inter", Font.BOLD, 12)); }
                    case "No Courses" -> { lbl.setForeground(StyleConstants.TEXT_MUTED);lbl.setFont(StyleConstants.FONT_SMALL); }
                    default           -> lbl.setFont(StyleConstants.FONT_SMALL);
                }
                lbl.setBackground(sel ? new Color(StyleConstants.ACCENT.getRed(),
                    StyleConstants.ACCENT.getGreen(), StyleConstants.ACCENT.getBlue(), 50)
                    : StyleConstants.BG_CARD);
                lbl.setOpaque(true);
                return lbl;
            }
        };
    }

    private DefaultTableCellRenderer reasonRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lbl.setFont(StyleConstants.FONT_SMALL);
                lbl.setForeground(StyleConstants.DANGER);
                lbl.setBackground(sel ? new Color(StyleConstants.ACCENT.getRed(),
                    StyleConstants.ACCENT.getGreen(), StyleConstants.ACCENT.getBlue(), 50)
                    : StyleConstants.BG_CARD);
                lbl.setOpaque(true);
                lbl.setToolTipText(v == null ? "" : v.toString());
                return lbl;
            }
        };
    }

    private JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Inter", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 8,16,8,16");
        return b;
    }
}
