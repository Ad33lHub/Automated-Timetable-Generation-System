package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class StyleConstants {

    // ─── Brand / Accent Colors ───
    public static Color ACCENT        = new Color(99, 102, 241);   // Indigo-500
    public static Color ACCENT_HOVER  = new Color(79, 70, 229);    // Indigo-600
    public static Color ACCENT_SOFT   = new Color(99, 102, 241, 30);

    public static Color SUCCESS       = new Color(16, 185, 129);   // Emerald-500
    public static Color SUCCESS_SOFT  = new Color(16, 185, 129, 30);
    public static Color DANGER        = new Color(239, 68, 68);    // Red-500
    public static Color DANGER_SOFT   = new Color(239, 68, 68, 30);
    public static Color WARNING       = new Color(245, 158, 11);   // Amber-500
    public static Color INFO          = new Color(59, 130, 246);   // Blue-500

    // ─── Timetable Cell Color Palette (keyed by subject hash) ───
    public static final Color[] CELL_BG = {
        new Color(99, 102, 241, 55),   // Indigo
        new Color(16, 185, 129, 55),   // Emerald
        new Color(245, 158, 11, 55),   // Amber
        new Color(239, 68,  68,  55),  // Red
        new Color(59,  130, 246, 55),  // Blue
        new Color(168, 85,  247, 55),  // Purple
        new Color(20,  184, 166, 55),  // Teal
        new Color(249, 115, 22,  55),  // Orange
    };
    public static final Color[] CELL_ACCENT = {
        new Color(99, 102, 241, 200),
        new Color(16, 185, 129, 200),
        new Color(245, 158, 11, 200),
        new Color(239, 68,  68, 200),
        new Color(59,  130, 246, 200),
        new Color(168, 85,  247, 200),
        new Color(20,  184, 166, 200),
        new Color(249, 115, 22, 200),
    };

    // ─── Surface / Background Colors (Dark Mode default) ───
    public static Color BG_DARK       = new Color(0x0D, 0x0D, 0x1A);
    public static Color BG_CARD       = new Color(0x1A, 0x1A, 0x2E);
    public static Color BG_SIDEBAR    = new Color(0x11, 0x11, 0x26);
    public static Color BG_SURFACE    = new Color(0x24, 0x24, 0x38);
    public static Color BORDER        = new Color(0x2A, 0x2A, 0x45);
    public static Color TEXT_PRIMARY  = new Color(0xF1, 0xF5, 0xF9);
    public static Color TEXT_SECONDARY= new Color(0x94, 0xA3, 0xB8);
    public static Color TEXT_MUTED    = new Color(0x64, 0x74, 0x8B);

    // ─── Typography ───
    public static final Font FONT_TITLE      = new Font("Inter", Font.BOLD, 30);
    public static final Font FONT_HEADING    = new Font("Inter", Font.BOLD, 18);
    public static final Font FONT_SUBHEADING = new Font("Inter", Font.BOLD, 15);
    public static final Font FONT_BODY       = new Font("Inter", Font.PLAIN, 14);
    public static final Font FONT_SMALL      = new Font("Inter", Font.PLAIN, 12);
    public static final Font FONT_LABEL      = new Font("Inter", Font.BOLD, 11);

    public static boolean isDark = true;

    // ─── Theme Switcher ───
    public static void setDarkTheme(boolean dark) {
        isDark = dark;
        if (dark) {
            BG_DARK        = new Color(0x0D, 0x0D, 0x1A);
            BG_CARD        = new Color(0x1A, 0x1A, 0x2E);
            BG_SIDEBAR     = new Color(0x11, 0x11, 0x26);
            BG_SURFACE     = new Color(0x24, 0x24, 0x38);
            BORDER         = new Color(0x2A, 0x2A, 0x45);
            TEXT_PRIMARY   = new Color(0xF1, 0xF5, 0xF9);
            TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);
            TEXT_MUTED     = new Color(0x64, 0x74, 0x8B);
            ACCENT         = new Color(99, 102, 241);
            ACCENT_HOVER   = new Color(79, 70, 229);
            ACCENT_SOFT    = new Color(99, 102, 241, 30);
        } else {
            BG_DARK        = new Color(0xF8, 0xFA, 0xFC);
            BG_CARD        = new Color(0xFF, 0xFF, 0xFF);
            BG_SIDEBAR     = new Color(0xF1, 0xF5, 0xF9);
            BG_SURFACE     = new Color(0xF8, 0xFA, 0xFC);
            BORDER         = new Color(0xE2, 0xE8, 0xF0);
            TEXT_PRIMARY   = new Color(0x1E, 0x29, 0x3B);
            TEXT_SECONDARY = new Color(0x47, 0x55, 0x69);
            TEXT_MUTED     = new Color(0x94, 0xA3, 0xB8);
            ACCENT         = new Color(79, 70, 229);
            ACCENT_HOVER   = new Color(67, 56, 202);
            ACCENT_SOFT    = new Color(79, 70, 229, 20);
        }
    }

    public static void toggleTheme(JFrame frame) {
        try {
            boolean nextDark = !isDark;
            setDarkTheme(nextDark);
            if (nextDark) {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.themes.FlatMacDarkLaf());
            } else {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.themes.FlatMacLightLaf());
            }
            UIManager.put("Table.selectionBackground", ACCENT);
            UIManager.put("Table.rowHeight", 36);
            SwingUtilities.updateComponentTreeUI(frame);
            updatePanelColors(frame);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void updatePanelColors(Container container) {
        if (container == null) return;
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel p = (JPanel) comp;
                Color bg = p.getBackground();
                if (bg != null) {
                    if (matchesAny(bg, 0x1A1A2E, 0xFFFFFF, 0x252422, 0xECEAE4)) p.setBackground(BG_CARD);
                    else if (matchesAny(bg, 0x0D0D1A, 0xF8FAFC, 0x191919, 0xF7F6F3)) p.setBackground(BG_DARK);
                    else if (matchesAny(bg, 0x111126, 0xF1F5F9)) p.setBackground(BG_SIDEBAR);
                    else if (matchesAny(bg, 0x242438)) p.setBackground(BG_SURFACE);
                }
            }
            if (comp instanceof JLabel) {
                JLabel l = (JLabel) comp;
                Color fg = l.getForeground();
                if (matchesAny(fg, 0xF1F5F9, 0x1E293B, 0xECEAE4, 0x191919)) l.setForeground(TEXT_PRIMARY);
                else if (matchesAny(fg, 0x94A3B8, 0x475569, 0x9B9A97, 0x37352F)) l.setForeground(TEXT_SECONDARY);
                else if (matchesAny(fg, 0x64748B)) l.setForeground(TEXT_MUTED);
            }
            if (comp instanceof JTextArea) ((JTextArea) comp).setForeground(TEXT_SECONDARY);
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                String txt = btn.getText();
                if (txt != null && (txt.contains("Light") || txt.contains("Dark"))) {
                    btn.setBackground(ACCENT);
                    btn.setForeground(Color.WHITE);
                }
            }
            if (comp instanceof Container) updatePanelColors((Container) comp);
        }
    }

    private static boolean matchesAny(Color c, int... rgbs) {
        if (c == null) return false;
        int cr = (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
        for (int rgb : rgbs) if (cr == rgb) return true;
        return false;
    }

    // ─── Component Factory Methods ───

    public static void applyRoundedButton(JButton btn, int arc) {
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JComponent.arc", arc);
    }

    public static void applyCardStyle(JPanel panel) {
        panel.setBackground(BG_CARD);
        panel.putClientProperty("FlatLaf.style", "arc: 16; border: 1,1,1,1," + toHex(BORDER));
    }

    public static void applyElevatedCardStyle(JPanel panel) {
        panel.setBackground(BG_SURFACE);
        panel.putClientProperty("FlatLaf.style", "arc: 20; border: 1,1,1,1," + toHex(BORDER));
    }

    public static void applyFieldStyle(JTextField field, String placeholder) {
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("FlatLaf.style",
            "arc: 10; focusColor: " + toHex(ACCENT) + "; margin: 6,12,6,12");
    }

    public static JLabel createSecondaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_SECONDARY);
        label.setFont(FONT_SMALL);
        return label;
    }

    public static JLabel createBadge(String text, Color bg) {
        JLabel badge = new JLabel(" " + text + " ", SwingConstants.CENTER);
        badge.setFont(FONT_LABEL);
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        badge.putClientProperty("FlatLaf.style", "arc: 20");
        return badge;
    }

    public static String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Border createShadowBorder() {
        return BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(1, 1, 1, 1)
        );
    }

    /** Pick a background color from the cell palette using a subject-name hash. */
    public static Color getCellBg(String subject) {
        return CELL_BG[Math.abs(subject.hashCode()) % CELL_BG.length];
    }

    /** Pick an accent (border/text) color matching getCellBg. */
    public static Color getCellAccent(String subject) {
        return CELL_ACCENT[Math.abs(subject.hashCode()) % CELL_ACCENT.length];
    }
}
