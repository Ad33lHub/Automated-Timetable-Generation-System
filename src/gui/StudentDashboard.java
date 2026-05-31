package gui;

import javax.swing.*;
import java.awt.*;

public class StudentDashboard extends JPanel {

    public StudentDashboard(models.Student student, Runnable onBack) {
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
        header.setPreferredSize(new Dimension(0, 72));
        header.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        // Left: avatar + student info
        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftInfo.setOpaque(false);

        // Avatar circle with initial
        String initial = student.getName().isEmpty() ? "S" :
            String.valueOf(student.getName().charAt(0)).toUpperCase();
        JPanel avatar = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246, 50));
                g2.fillOval(0, 0, 44, 44);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(44, 44); }
        };
        avatar.setOpaque(false);
        JLabel avatarLbl = new JLabel(initial);
        avatarLbl.setFont(new Font("Inter", Font.BOLD, 18));
        avatarLbl.setForeground(new Color(59, 130, 246));
        avatar.add(avatarLbl);

        JPanel studentMeta = new JPanel();
        studentMeta.setLayout(new BoxLayout(studentMeta, BoxLayout.Y_AXIS));
        studentMeta.setOpaque(false);

        JLabel nameLbl = new JLabel(student.getName());
        nameLbl.setFont(new Font("Inter", Font.BOLD, 17));
        nameLbl.setForeground(StyleConstants.TEXT_PRIMARY);

        // Section + Dept badges in one row
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badges.setOpaque(false);

        JLabel sectionBadge = new JLabel(" " + student.getSection() + " ");
        sectionBadge.setFont(new Font("Inter", Font.BOLD, 11));
        sectionBadge.setForeground(Color.WHITE);
        sectionBadge.setOpaque(true);
        sectionBadge.setBackground(new Color(59, 130, 246));
        sectionBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        sectionBadge.putClientProperty("FlatLaf.style", "arc: 20");

        JLabel deptBadge = new JLabel(" " + student.getDepartment() + " ");
        deptBadge.setFont(new Font("Inter", Font.BOLD, 11));
        deptBadge.setForeground(StyleConstants.TEXT_SECONDARY);
        deptBadge.setOpaque(true);
        deptBadge.setBackground(StyleConstants.BG_SURFACE);
        deptBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        deptBadge.putClientProperty("FlatLaf.style", "arc: 20");

        badges.add(sectionBadge);
        badges.add(deptBadge);

        studentMeta.add(nameLbl);
        studentMeta.add(badges);

        leftInfo.add(avatar);
        leftInfo.add(studentMeta);
        header.add(leftInfo, BorderLayout.WEST);

        // Right: theme + logout
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

        JButton logoutBtn = new JButton("← Logout");
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

        // ── Timetable (locked to student's section) ───────────────────────
        ViewTimetablePanel timetable = new ViewTimetablePanel();
        timetable.setSectionFilter(student.getSection());
        add(timetable, BorderLayout.CENTER);
    }
}
