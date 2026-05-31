package gui;

import com.formdev.flatlaf.FlatClientProperties;
import database.TimetableGenerator;
import javax.swing.*;
import java.awt.*;

public class TimetableGenerationPanel extends JPanel {

    private final JProgressBar  progressBar      = new JProgressBar(0, 100);
    private final JButton       generateBtn      = new JButton("✨  Generate Optimized Timetable");
    private final JLabel        statusLbl        = new JLabel("System ready — configure options and generate.", SwingConstants.CENTER);

    // Options
    private final JToggleButton avoidSatToggle        = makeToggle(true);
    private final JToggleButton multiTrialToggle      = makeToggle(true);
    private final JToggleButton swapResolutionToggle  = makeToggle(true);
    private final JToggleButton morningFirstToggle    = makeToggle(true);

    // Stats labels (updated after generation)
    private final JLabel statScheduled  = statLbl("—");
    private final JLabel statConflicts  = statLbl("—");
    private final JLabel statSwaps      = statLbl("—");
    private final JLabel statTeachers   = statLbl("—");

    // Optional reference to conflict panel for refresh
    private ConflictAnalysisPanel conflictPanel;

    public void setConflictPanel(ConflictAnalysisPanel p) { this.conflictPanel = p; }

    public TimetableGenerationPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(StyleConstants.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(36, 56, 36, 56));

        // ── Page heading ─────────────────────────────────────────────────
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setOpaque(false);
        hero.setBorder(BorderFactory.createEmptyBorder(0, 0, 32, 0));

        JLabel title = new JLabel("Timetable Generation Engine", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 28));
        title.setForeground(StyleConstants.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel desc = new JLabel(
            "Constraint-satisfaction algorithm with multi-trial optimization & swap resolution",
            SwingConstants.CENTER);
        desc.setFont(new Font("Inter", Font.PLAIN, 14));
        desc.setForeground(StyleConstants.TEXT_SECONDARY);
        desc.setAlignmentX(CENTER_ALIGNMENT);

        hero.add(title);
        hero.add(Box.createVerticalStrut(8));
        hero.add(desc);
        add(hero, BorderLayout.NORTH);

        // ── Three-card layout ─────────────────────────────────────────────
        JPanel cards = new JPanel(new GridBagLayout());
        cards.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(0, 10, 0, 10);
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // ── Card 1: Generation parameters ────────────────────────────────
        JPanel configCard = buildCard();
        configCard.setPreferredSize(new Dimension(460, 400));

        addCardTitle(configCard, "Generation Parameters", "Constraints applied during scheduling");
        configCard.add(Box.createVerticalStrut(24));

        configCard.add(optionRow("Avoid Saturday Classes",
            "Keep weekends free for students", avoidSatToggle));
        configCard.add(Box.createVerticalStrut(14));
        configCard.add(optionRow("Multi-Trial Optimization",
            "Run 3 trials, pick the best result", multiTrialToggle));
        configCard.add(Box.createVerticalStrut(14));
        configCard.add(optionRow("Swap-Based Conflict Resolution",
            "Move placed classes to free blocked slots", swapResolutionToggle));
        configCard.add(Box.createVerticalStrut(14));
        configCard.add(optionRow("Prioritize Morning Slots",
            "Schedule earlier in the day first", morningFirstToggle));
        configCard.add(Box.createVerticalStrut(28));

        generateBtn.setBackground(StyleConstants.ACCENT);
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setFont(new Font("Inter", Font.BOLD, 15));
        generateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        generateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        generateBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        generateBtn.addActionListener(e -> runGeneration());
        configCard.add(generateBtn);
        configCard.add(Box.createVerticalStrut(14));

        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        progressBar.setForeground(StyleConstants.ACCENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        configCard.add(progressBar);
        configCard.add(Box.createVerticalStrut(10));

        statusLbl.setFont(StyleConstants.FONT_SMALL);
        statusLbl.setForeground(StyleConstants.TEXT_MUTED);
        statusLbl.setAlignmentX(CENTER_ALIGNMENT);
        configCard.add(statusLbl);

        gbc.gridx = 0; gbc.weightx = 1.3;
        cards.add(configCard, gbc);

        // ── Card 2: Generation stats ──────────────────────────────────────
        JPanel statsCard = buildCard();
        statsCard.setPreferredSize(new Dimension(260, 400));

        addCardTitle(statsCard, "Last Run Stats", "Updated after each generation");
        statsCard.add(Box.createVerticalStrut(24));

        statsCard.add(statRow("Scheduled Classes", statScheduled, StyleConstants.SUCCESS));
        statsCard.add(Box.createVerticalStrut(12));
        statsCard.add(statRow("Conflicts",          statConflicts, StyleConstants.DANGER));
        statsCard.add(Box.createVerticalStrut(12));
        statsCard.add(statRow("Swap Resolutions",   statSwaps,    StyleConstants.INFO));
        statsCard.add(Box.createVerticalStrut(12));
        statsCard.add(statRow("Active Teachers",    statTeachers, StyleConstants.WARNING));
        statsCard.add(Box.createVerticalGlue());

        // View Conflicts button
        JButton viewConflictsBtn = new JButton("⚠  View Conflict Report");
        viewConflictsBtn.setFont(StyleConstants.FONT_SMALL);
        viewConflictsBtn.setForeground(StyleConstants.WARNING);
        viewConflictsBtn.setBackground(StyleConstants.BG_SURFACE);
        viewConflictsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewConflictsBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        viewConflictsBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        viewConflictsBtn.addActionListener(e -> {
            if (conflictPanel != null) conflictPanel.runAnalysis();
        });
        statsCard.add(viewConflictsBtn);

        gbc.gridx = 1; gbc.weightx = 0.8;
        cards.add(statsCard, gbc);

        // ── Card 3: Data centre ───────────────────────────────────────────
        JPanel dataCard = buildCard();
        dataCard.setPreferredSize(new Dimension(300, 400));

        addCardTitle(dataCard, "Data Centre", "Manage sample data for testing");
        dataCard.add(Box.createVerticalStrut(24));

        JPanel infoBox = new JPanel();
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.setBackground(StyleConstants.BG_SURFACE);
        infoBox.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(StyleConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        infoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        String[] bullets = {
            "📌  6 teachers (inc. limited-availability demo)",
            "📌  8 subjects across 3 sections",
            "📌  4 classrooms + 20 time slots",
            "📌  Built-in conflict scenario included",
        };
        for (String b : bullets) {
            JLabel bl = new JLabel(b);
            bl.setFont(StyleConstants.FONT_SMALL);
            bl.setForeground(StyleConstants.TEXT_SECONDARY);
            infoBox.add(bl);
            infoBox.add(Box.createVerticalStrut(4));
        }
        dataCard.add(infoBox);
        dataCard.add(Box.createVerticalStrut(20));

        JButton populateBtn = new JButton("🎲  Populate Sample Data");
        populateBtn.setBackground(StyleConstants.BG_SURFACE);
        populateBtn.setForeground(StyleConstants.TEXT_PRIMARY);
        populateBtn.setFont(new Font("Inter", Font.BOLD, 13));
        populateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        populateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        populateBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        populateBtn.addActionListener(e -> handlePopulateData());
        dataCard.add(populateBtn);
        dataCard.add(Box.createVerticalStrut(10));

        JButton clearBtn = new JButton("🗑  Clear All System Data");
        clearBtn.setForeground(StyleConstants.DANGER);
        clearBtn.setFont(new Font("Inter", Font.BOLD, 13));
        clearBtn.setOpaque(false);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(StyleConstants.DANGER, 1, true),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        clearBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> handleClearData());
        dataCard.add(clearBtn);

        gbc.gridx = 2; gbc.weightx = 0.9;
        cards.add(dataCard, gbc);

        add(cards, BorderLayout.CENTER);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        StyleConstants.applyCardStyle(card);
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        return card;
    }

    private void addCardTitle(JPanel card, String title, String sub) {
        JLabel t = new JLabel(title);
        t.setFont(StyleConstants.FONT_HEADING);
        t.setForeground(StyleConstants.TEXT_PRIMARY);
        JLabel s = StyleConstants.createSecondaryLabel(sub);
        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(s);
    }

    private JPanel optionRow(String label, String hint, JToggleButton toggle) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(new Font("Inter", Font.BOLD, 13));
        nameLbl.setForeground(StyleConstants.TEXT_PRIMARY);

        JLabel hintLbl = new JLabel(hint);
        hintLbl.setFont(StyleConstants.FONT_SMALL);
        hintLbl.setForeground(StyleConstants.TEXT_MUTED);

        textCol.add(nameLbl);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(hintLbl);

        row.add(textCol, BorderLayout.CENTER);
        row.add(toggle,  BorderLayout.EAST);
        return row;
    }

    private JPanel statRow(String label, JLabel valueLabel, Color accent) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(StyleConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JPanel colorBar = new JPanel();
        colorBar.setBackground(accent);
        colorBar.setPreferredSize(new Dimension(4, 0));

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(StyleConstants.FONT_SMALL);
        nameLbl.setForeground(StyleConstants.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Inter", Font.BOLD, 22));
        valueLabel.setForeground(accent);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(colorBar,  BorderLayout.WEST);
        row.add(nameLbl,   BorderLayout.CENTER);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private static JLabel statLbl(String initial) {
        JLabel l = new JLabel(initial);
        l.setFont(new Font("Inter", Font.BOLD, 22));
        return l;
    }

    private static JToggleButton makeToggle(boolean selected) {
        JToggleButton t = new JToggleButton();
        t.setSelected(selected);
        t.putClientProperty("JButton.buttonType", "switch");
        t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return t;
    }

    // ── Actions ────────────────────────────────────────────────────────────

    private void handlePopulateData() {
        int res = JOptionPane.showConfirmDialog(this,
            "This will DELETE all current data and generate a sample environment.\nContinue?",
            "Populate Sample Data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            new database.SampleDataGenerator().clearAndGenerate();
            statusLbl.setText("✅  Sample data created — ready to generate.");
            statusLbl.setForeground(StyleConstants.SUCCESS);
        }
    }

    private void handleClearData() {
        int res = JOptionPane.showConfirmDialog(this,
            "Wipe ALL system data? This cannot be undone.",
            "Clear Data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            database.DatabaseManager.getInstance().clearTimetable();
            statusLbl.setText("🧹  Timetable data cleared.");
            statusLbl.setForeground(StyleConstants.TEXT_MUTED);
        }
    }

    private void runGeneration() {
        generateBtn.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLbl.setText("Running constraint-satisfaction algorithm…");
        statusLbl.setForeground(StyleConstants.INFO);

        new Thread(() -> {
            database.TimetableGenerator gen = new database.TimetableGenerator();
            TimetableGenerator.GenerationResult result = gen.generate();

            SwingUtilities.invokeLater(() -> {
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                generateBtn.setEnabled(true);

                // Update stats panel
                statScheduled.setText(String.valueOf(result.successCount));
                statConflicts.setText(String.valueOf(result.failCount));
                statSwaps.setText(result.swapResolutions > 0
                    ? String.valueOf(result.swapResolutions) : "0");

                // Count active teachers from DB
                long teacherCount = new database.TeacherDAO().getAll().size();
                statTeachers.setText(String.valueOf(teacherCount));

                // Update status label
                if (result.failCount == 0) {
                    statusLbl.setText("✅  Perfect schedule — no conflicts!");
                    statusLbl.setForeground(StyleConstants.SUCCESS);
                } else {
                    statusLbl.setText("⚠  " + result.failCount + " conflict(s) — check Conflicts tab.");
                    statusLbl.setForeground(StyleConstants.WARNING);
                }

                // Show log dialog
                StringBuilder sb = new StringBuilder();
                for (String line : result.toLogLines()) sb.append(line).append("\n");

                JTextArea logArea = new JTextArea(14, 52);
                logArea.setText(sb.toString());
                logArea.setEditable(false);
                logArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
                logArea.setBackground(StyleConstants.BG_SURFACE);
                logArea.setForeground(StyleConstants.TEXT_PRIMARY);
                logArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                logArea.setCaretPosition(0);

                JScrollPane scroll = new JScrollPane(logArea);
                scroll.setBorder(BorderFactory.createEmptyBorder());
                scroll.setPreferredSize(new Dimension(620, 360));

                JOptionPane.showMessageDialog(this, scroll,
                    "Generation Results", JOptionPane.INFORMATION_MESSAGE);

                // Auto-refresh conflict panel if available
                if (conflictPanel != null) conflictPanel.runAnalysis();
            });
        }).start();
    }
}
