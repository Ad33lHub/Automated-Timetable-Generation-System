package gui;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RoleSelectionPanel extends JPanel {

    private final java.util.function.Consumer<String> onRoleSelected;

    public RoleSelectionPanel(JFrame parentFrame, java.util.function.Consumer<String> onRoleSelected) {
        this.onRoleSelected = onRoleSelected;
        setLayout(new BorderLayout());
        setBackground(StyleConstants.BG_DARK);

        // ── Hero Banner ──────────────────────────────────────────────────────
        JPanel hero = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Deep indigo gradient
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(24, 24, 60),
                    getWidth(), getHeight(), new Color(13, 13, 26));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decorative glow orbs
                g2.setColor(new Color(99, 102, 241, 22));
                g2.fillOval(-120, -120, 400, 400);
                g2.setColor(new Color(79, 70, 229, 14));
                g2.fillOval(getWidth() - 220, -80, 440, 440);
                g2.setColor(new Color(16, 185, 129, 8));
                g2.fillOval(getWidth() / 2 - 100, getHeight() - 60, 250, 200);
                g2.dispose();
            }
        };
        hero.setPreferredSize(new Dimension(0, 200));
        hero.setOpaque(false);

        // Theme toggle — top-right
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 28, 18));
        topBar.setOpaque(false);
        JButton themeBtn = buildThemeButton(parentFrame);
        topBar.add(themeBtn);
        hero.add(topBar, BorderLayout.NORTH);

        // App branding
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 0, 32, 0));

        JLabel appName = new JLabel("AutoTime", SwingConstants.CENTER);
        appName.setFont(new Font("Inter", Font.BOLD, 54));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Intelligent Academic Scheduling System", SwingConstants.CENTER);
        tagline.setFont(new Font("Inter", Font.PLAIN, 16));
        tagline.setForeground(new Color(0xC7, 0xD2, 0xFE)); // indigo-200
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        brand.add(Box.createVerticalGlue());
        brand.add(appName);
        brand.add(Box.createVerticalStrut(7));
        brand.add(tagline);
        brand.add(Box.createVerticalGlue());
        hero.add(brand, BorderLayout.CENTER);
        add(hero, BorderLayout.NORTH);

        // ── Role Cards ───────────────────────────────────────────────────────
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(48, 60, 28, 60));

        JPanel grid = new JPanel(new GridLayout(1, 3, 28, 0));
        grid.setOpaque(false);

        grid.add(roleCard("ADMIN",   "⚙",   "Manage faculty, subjects, classrooms and full system configuration.",
            new Color(99, 102, 241)));
        grid.add(roleCard("TEACHER", "👨‍🏫", "Access your personalized teaching schedule and assigned subjects.",
            new Color(16, 185, 129)));
        grid.add(roleCard("STUDENT", "🎓",  "View your section's weekly timetable organized by department.",
            new Color(59, 130, 246)));

        center.add(grid);
        add(center, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("COMSATS University Vehari  ·  AutoTime v2.0", SwingConstants.CENTER);
        footer.setFont(StyleConstants.FONT_SMALL);
        footer.setForeground(StyleConstants.TEXT_MUTED);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 28, 0));
        add(footer, BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private JButton buildThemeButton(JFrame parentFrame) {
        JButton btn = new JButton(StyleConstants.isDark ? "☀  Light Mode" : "🌙  Dark Mode");
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setForeground(new Color(0xC7, 0xD2, 0xFE));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.addActionListener(e -> {
            StyleConstants.toggleTheme(parentFrame);
            btn.setText(StyleConstants.isDark ? "☀  Light Mode" : "🌙  Dark Mode");
        });
        return btn;
    }

    private JPanel roleCard(String role, String icon, String desc, Color accent) {
        // Use array trick so inner classes can mutate the flag
        final boolean[] hovered = {false};

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background
                g2.setColor(StyleConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                if (hovered[0]) {
                    // Subtle accent tint on hover
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 18));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    // Top accent strip
                    g2.setColor(accent);
                    g2.fillRoundRect(0, 0, getWidth(), 5, 5, 5);
                }

                // Border
                g2.setColor(hovered[0] ? accent : StyleConstants.BORDER);
                g2.setStroke(new BasicStroke(hovered[0] ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(280, 370));
        card.setBorder(BorderFactory.createEmptyBorder(40, 28, 40, 28));

        // Icon circle
        JPanel iconCircle = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 38));
                g2.fillOval(0, 0, 76, 76);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(76, 76); }
            @Override public Dimension getMaximumSize()   { return new Dimension(76, 76); }
        };
        iconCircle.setOpaque(false);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
        iconCircle.add(iconLbl);
        iconCircle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel(role);
        titleLbl.setFont(new Font("Inter", Font.BOLD, 20));
        titleLbl.setForeground(StyleConstants.TEXT_PRIMARY);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        // Accent underline bar
        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(36, 3));
        bar.setMaximumSize(new Dimension(36, 3));
        bar.setAlignmentX(CENTER_ALIGNMENT);

        JLabel descLbl = new JLabel(
            "<html><div style='text-align:center;line-height:1.6;width:180px'>" + desc + "</div></html>");
        descLbl.setFont(StyleConstants.FONT_BODY);
        descLbl.setForeground(StyleConstants.TEXT_SECONDARY);
        descLbl.setHorizontalAlignment(SwingConstants.CENTER);
        descLbl.setAlignmentX(CENTER_ALIGNMENT);

        JButton btn = new JButton("Enter as " + titleCase(role));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setBackground(accent);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(200, 44));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        btn.addActionListener(e -> onRoleSelected.accept(role.toLowerCase()));

        card.add(Box.createVerticalGlue());
        card.add(iconCircle);
        card.add(Box.createVerticalStrut(18));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(bar);
        card.add(Box.createVerticalStrut(16));
        card.add(descLbl);
        card.add(Box.createVerticalStrut(30));
        card.add(btn);
        card.add(Box.createVerticalGlue());

        // Hover animation
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered[0] = true;  card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered[0] = false; card.repaint(); }
        });

        return card;
    }

    private String titleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
