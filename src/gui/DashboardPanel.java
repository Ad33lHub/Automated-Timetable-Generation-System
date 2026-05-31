package gui;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DashboardPanel extends JPanel {

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel     contentPanel  = new JPanel(contentLayout);
    private JButton          activeNavBtn  = null;

    private static final String[][] NAV_ITEMS = {
        {"teachers",    "👨‍🏫", "Teachers",         "Manage faculty records"},
        {"subjects",    "📚", "Subjects",          "Course curriculum"},
        {"classrooms",  "🏫", "Classrooms",        "Room allocations"},
        {"timeslots",   "🕐", "Time Slots",        "Schedule grid"},
        {"assignments", "🔗", "Assignments",       "Teacher–Course links"},
        {"generate",    "⚙",  "Generate",          "Run algorithm"},
        {"view",        "📅", "View Schedule",     "Interactive timetable"},
        {"conflicts",   "⚠",  "Conflict Analysis", "Detect availability issues"},
    };

    public DashboardPanel(Runnable onLogout) {
        setLayout(new BorderLayout());
        setBackground(StyleConstants.BG_DARK);

        // ── Sidebar ────────────────────────────────────────────────────────
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(StyleConstants.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle right border
                g2.setColor(StyleConstants.BORDER);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(256, 0));

        // Brand logo area
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        logoArea.setOpaque(false);
        logoArea.setBorder(BorderFactory.createEmptyBorder(28, 0, 24, 0));

        JPanel logoBadge = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(StyleConstants.ACCENT);
                g2.fillRoundRect(0, 0, 40, 40, 12, 12);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(40, 40); }
            @Override public Dimension getMaximumSize()   { return new Dimension(40, 40); }
        };
        logoBadge.setOpaque(false);
        JLabel logoText = new JLabel("AT");
        logoText.setFont(new Font("Inter", Font.BOLD, 16));
        logoText.setForeground(Color.WHITE);
        logoBadge.add(logoText);

        JPanel logoLabels = new JPanel();
        logoLabels.setLayout(new BoxLayout(logoLabels, BoxLayout.Y_AXIS));
        logoLabels.setOpaque(false);
        JLabel logoName = new JLabel("AutoTime");
        logoName.setFont(new Font("Inter", Font.BOLD, 16));
        logoName.setForeground(StyleConstants.TEXT_PRIMARY);
        JLabel logoSub = new JLabel("Admin Suite");
        logoSub.setFont(new Font("Inter", Font.PLAIN, 11));
        logoSub.setForeground(StyleConstants.TEXT_MUTED);
        logoLabels.add(logoName);
        logoLabels.add(logoSub);

        logoArea.add(logoBadge);
        logoArea.add(logoLabels);
        sidebar.add(logoArea);

        // Divider
        sidebar.add(createSidebarDivider("NAVIGATION"));

        // Nav buttons
        for (String[] item : NAV_ITEMS) {
            JButton btn = buildNavButton(item[1], item[2], item[3], item[0]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(2));
            if (activeNavBtn == null) {
                setActive(btn, true);
                activeNavBtn = btn;
            }
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createSidebarDivider(null));

        // User section
        JPanel userSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        userSection.setOpaque(false);

        JPanel avatar = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(99, 102, 241, 60));
                g2.fillOval(0, 0, 36, 36);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(36, 36); }
        };
        avatar.setOpaque(false);
        JLabel initials = new JLabel("A");
        initials.setFont(new Font("Inter", Font.BOLD, 14));
        initials.setForeground(StyleConstants.ACCENT);
        avatar.add(initials);

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);
        JLabel userNameLbl = new JLabel("Administrator");
        userNameLbl.setFont(new Font("Inter", Font.BOLD, 13));
        userNameLbl.setForeground(StyleConstants.TEXT_PRIMARY);
        JLabel statusLbl  = new JLabel("● Online");
        statusLbl.setFont(StyleConstants.FONT_SMALL);
        statusLbl.setForeground(StyleConstants.SUCCESS);
        userInfo.add(userNameLbl);
        userInfo.add(statusLbl);

        userSection.add(avatar);
        userSection.add(userInfo);
        sidebar.add(userSection);

        // Logout
        JButton logoutBtn = buildNavButton("⎋", "Logout", "Exit session", "logout");
        logoutBtn.setForeground(StyleConstants.DANGER);
        logoutBtn.addActionListener(e -> onLogout.run());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(16));

        add(sidebar, BorderLayout.WEST);

        // ── Top Header ────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(StyleConstants.BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(StyleConstants.BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 68));
        header.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel titleLbl = new JLabel("Academic Scheduling Suite");
        titleLbl.setFont(StyleConstants.FONT_HEADING);
        titleLbl.setForeground(StyleConstants.TEXT_PRIMARY);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        headerRight.setOpaque(false);

        JButton themeToggle = new JButton(StyleConstants.isDark ? "☀ Light" : "🌙 Dark");
        themeToggle.setFont(new Font("Inter", Font.BOLD, 11));
        themeToggle.putClientProperty("JButton.buttonType", "roundRect");
        themeToggle.setBackground(StyleConstants.ACCENT);
        themeToggle.setForeground(Color.WHITE);
        themeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        themeToggle.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JFrame) {
                StyleConstants.toggleTheme((JFrame) w);
                themeToggle.setText(StyleConstants.isDark ? "☀ Light" : "🌙 Dark");
            }
        });
        headerRight.add(themeToggle);

        header.add(titleLbl, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // ── Content area ──────────────────────────────────────────────────
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setOpaque(false);
        mainWrapper.add(header, BorderLayout.NORTH);

        contentPanel.setOpaque(false);
        contentPanel.add(new TeacherPanel(),             "teachers");
        contentPanel.add(new SubjectPanel(),             "subjects");
        contentPanel.add(new ClassroomPanel(),           "classrooms");
        contentPanel.add(new TimeSlotPanel(),            "timeslots");
        contentPanel.add(new SubjectAssignmentPanel(),   "assignments");

        // Wire ConflictAnalysisPanel into GenerationPanel so conflicts auto-refresh after generation
        ConflictAnalysisPanel conflictPanel = new ConflictAnalysisPanel();
        TimetableGenerationPanel genPanel   = new TimetableGenerationPanel();
        genPanel.setConflictPanel(conflictPanel);

        contentPanel.add(genPanel,                       "generate");
        contentPanel.add(new ViewTimetablePanel(),       "view");
        contentPanel.add(conflictPanel,                  "conflicts");

        mainWrapper.add(contentPanel, BorderLayout.CENTER);
        add(mainWrapper, BorderLayout.CENTER);
    }

    // ── Sidebar helpers ────────────────────────────────────────────────────

    private JButton buildNavButton(String icon, String label, String subtitle, String cardName) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                // handled by FlatLaf + client properties; just fill active indicator
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BorderLayout(12, 0));
        btn.setPreferredSize(new Dimension(240, 48));
        btn.setMaximumSize(new Dimension(240, 48));
        btn.setBackground(StyleConstants.BG_SIDEBAR);
        btn.setForeground(StyleConstants.TEXT_SECONDARY);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLbl.setForeground(StyleConstants.TEXT_SECONDARY);
        iconLbl.setPreferredSize(new Dimension(24, 24));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);
        JLabel mainLbl = new JLabel(label);
        mainLbl.setFont(new Font("Inter", Font.BOLD, 13));
        mainLbl.setForeground(StyleConstants.TEXT_SECONDARY);
        textStack.add(mainLbl);

        btn.add(iconLbl, BorderLayout.WEST);
        btn.add(textStack, BorderLayout.CENTER);

        if (!cardName.equals("logout")) {
            btn.addActionListener(e -> {
                if (activeNavBtn != null) setActive(activeNavBtn, false);
                activeNavBtn = btn;
                setActive(btn, true);
                contentLayout.show(contentPanel, cardName);
            });
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeNavBtn) btn.setBackground(StyleConstants.BG_SURFACE);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeNavBtn) btn.setBackground(StyleConstants.BG_SIDEBAR);
            }
        });

        return btn;
    }

    private void setActive(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(
                StyleConstants.ACCENT.getRed(),
                StyleConstants.ACCENT.getGreen(),
                StyleConstants.ACCENT.getBlue(), 28));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, StyleConstants.ACCENT),
                BorderFactory.createEmptyBorder(0, 14, 0, 18)));
            // Tint the icon and label
            for (Component c : btn.getComponents()) {
                if (c instanceof JLabel) ((JLabel) c).setForeground(StyleConstants.ACCENT);
                if (c instanceof JPanel) {
                    for (Component ch : ((JPanel) c).getComponents())
                        if (ch instanceof JLabel) ((JLabel) ch).setForeground(StyleConstants.ACCENT);
                }
            }
        } else {
            btn.setBackground(StyleConstants.BG_SIDEBAR);
            btn.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
            for (Component c : btn.getComponents()) {
                if (c instanceof JLabel) ((JLabel) c).setForeground(StyleConstants.TEXT_SECONDARY);
                if (c instanceof JPanel) {
                    for (Component ch : ((JPanel) c).getComponents())
                        if (ch instanceof JLabel) ((JLabel) ch).setForeground(StyleConstants.TEXT_SECONDARY);
                }
            }
        }
        btn.revalidate();
        btn.repaint();
    }

    private JPanel createSidebarDivider(String label) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setBorder(BorderFactory.createEmptyBorder(8, 18, 4, 18));
        if (label != null) {
            JLabel lbl = new JLabel(label);
            lbl.setFont(new Font("Inter", Font.BOLD, 10));
            lbl.setForeground(StyleConstants.TEXT_MUTED);
            row.add(lbl, BorderLayout.WEST);
        }
        JSeparator sep = new JSeparator();
        sep.setForeground(StyleConstants.BORDER);
        row.add(sep, label == null ? BorderLayout.CENTER : BorderLayout.EAST);
        return row;
    }
}
