package gui;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TeacherDashboard extends JPanel {

    private final JPanel contentPanel = new JPanel(new CardLayout());
    private models.Teacher currentTeacher = null;

    public TeacherDashboard(Runnable onBack) {
        setLayout(new BorderLayout());
        setBackground(StyleConstants.BG_DARK);

        // ── Header ────────────────────────────────────────────────────────
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
        header.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        // Left brand
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBrand.setOpaque(false);

        JPanel badge = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16, 185, 129, 50));
                g2.fillOval(0, 0, 38, 38);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(38, 38); }
        };
        badge.setOpaque(false);
        JLabel teacherIcon = new JLabel("👨‍🏫");
        teacherIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        badge.add(teacherIcon);

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        JLabel titleLbl = new JLabel("Teacher Portal");
        titleLbl.setFont(new Font("Inter", Font.BOLD, 17));
        titleLbl.setForeground(StyleConstants.TEXT_PRIMARY);
        JLabel subLbl = new JLabel("AutoTime");
        subLbl.setFont(StyleConstants.FONT_SMALL);
        subLbl.setForeground(StyleConstants.TEXT_MUTED);
        titleStack.add(titleLbl);
        titleStack.add(subLbl);

        leftBrand.add(badge);
        leftBrand.add(titleStack);
        header.add(leftBrand, BorderLayout.WEST);

        // Right controls
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
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

        JButton logoutBtn = new JButton("← Back");
        logoutBtn.setFont(new Font("Inter", Font.BOLD, 13));
        logoutBtn.setForeground(StyleConstants.DANGER);
        logoutBtn.setOpaque(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> onBack.run());

        headerRight.add(themeToggle);
        headerRight.add(logoutBtn);
        header.add(headerRight, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        showTeacherSelection();
    }

    // ── Teacher selection screen ───────────────────────────────────────────

    private void showTeacherSelection() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        StyleConstants.applyCardStyle(card);
        card.setPreferredSize(new Dimension(460, 380));
        card.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        // Icon circle
        JPanel iconCircle = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16, 185, 129, 40));
                g2.fillOval(0, 0, 72, 72);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(72, 72); }
            @Override public Dimension getMaximumSize()   { return new Dimension(72, 72); }
        };
        iconCircle.setOpaque(false);
        JLabel ic = new JLabel("👨‍🏫");
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconCircle.add(ic);
        iconCircle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel heading = new JLabel("Identity Verification");
        heading.setFont(new Font("Inter", Font.BOLD, 22));
        heading.setForeground(StyleConstants.TEXT_PRIMARY);
        heading.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = StyleConstants.createSecondaryLabel("Select your name to view your schedule");
        sub.setAlignmentX(CENTER_ALIGNMENT);

        // Teacher ComboBox
        JComboBox<models.Teacher> combo = new JComboBox<>();
        List<models.Teacher> teachers = new database.TeacherDAO().getAll();
        for (models.Teacher t : teachers) combo.addItem(t);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        combo.setFont(StyleConstants.FONT_BODY);
        combo.setAlignmentX(CENTER_ALIGNMENT);

        JButton accessBtn = new JButton("View My Schedule  →");
        accessBtn.setBackground(new Color(16, 185, 129));
        accessBtn.setForeground(Color.WHITE);
        accessBtn.setFont(new Font("Inter", Font.BOLD, 14));
        accessBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        accessBtn.setAlignmentX(CENTER_ALIGNMENT);
        accessBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        accessBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        accessBtn.addActionListener(e -> {
            currentTeacher = (models.Teacher) combo.getSelectedItem();
            if (currentTeacher != null) showTeacherSchedule();
        });

        card.add(Box.createVerticalGlue());
        card.add(iconCircle);
        card.add(Box.createVerticalStrut(20));
        card.add(heading);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));
        card.add(combo);
        card.add(Box.createVerticalStrut(24));
        card.add(accessBtn);
        card.add(Box.createVerticalGlue());

        outer.add(card);
        contentPanel.add(outer, "selection");
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "selection");
    }

    // ── Schedule view ─────────────────────────────────────────────────────

    private void showTeacherSchedule() {
        JPanel view = new JPanel(new BorderLayout());
        view.setOpaque(false);

        // Welcome strip
        JPanel strip = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(StyleConstants.BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(StyleConstants.BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        strip.setOpaque(false);
        strip.setPreferredSize(new Dimension(0, 64));
        strip.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameLabel = new JLabel("Prof. " + currentTeacher.getName());
        nameLabel.setFont(new Font("Inter", Font.BOLD, 18));
        nameLabel.setForeground(StyleConstants.TEXT_PRIMARY);

        JLabel specLabel = new JLabel(currentTeacher.getSpecialization());
        specLabel.setFont(StyleConstants.FONT_SMALL);
        specLabel.setForeground(new Color(16, 185, 129));

        info.add(nameLabel);
        info.add(specLabel);
        strip.add(info, BorderLayout.WEST);

        JButton backToSel = new JButton("Switch Teacher");
        backToSel.setFont(StyleConstants.FONT_SMALL);
        backToSel.setForeground(StyleConstants.TEXT_SECONDARY);
        backToSel.setOpaque(false);
        backToSel.setContentAreaFilled(false);
        backToSel.setBorderPainted(false);
        backToSel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToSel.addActionListener(e ->
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "selection"));
        strip.add(backToSel, BorderLayout.EAST);

        view.add(strip, BorderLayout.NORTH);

        ViewTimetablePanel timetable = new ViewTimetablePanel();
        view.add(timetable, BorderLayout.CENTER);

        contentPanel.add(view, "schedule");
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "schedule");
    }
}
