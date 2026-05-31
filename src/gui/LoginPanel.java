package gui;

import com.formdev.flatlaf.FlatClientProperties;
import database.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {

    private final JTextField    usernameField;
    private final JPasswordField passwordField;
    private final JLabel        errorLabel;
    private final String        role;
    private final Runnable      onLoginSuccess;
    private java.util.function.Consumer<Object> onUserAuthenticated;

    // ── Primary constructor (backward-compatible: onBack=null hides the back button) ──
    public LoginPanel(String role, Runnable onLoginSuccess,
                      java.util.function.Consumer<Object> onUserAuthenticated) {
        this(role, onLoginSuccess, onUserAuthenticated, null);
    }

    public LoginPanel(String role, Runnable onLoginSuccess,
                      java.util.function.Consumer<Object> onUserAuthenticated,
                      Runnable onBack) {
        this.role              = role;
        this.onLoginSuccess    = onLoginSuccess;
        this.onUserAuthenticated = onUserAuthenticated;

        setLayout(new BorderLayout());
        setBackground(StyleConstants.BG_DARK);

        // ── Left decorative panel ─────────────────────────────────────────
        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0,          new Color(24, 24, 60),
                    getWidth(), getHeight(), new Color(49, 46, 129));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decorative orbs
                g2.setColor(new Color(99, 102, 241, 28));
                g2.fillOval(-90, -90, 340, 340);
                g2.setColor(new Color(79, 70, 229, 16));
                g2.fillOval(getWidth() - 180, getHeight() - 220, 360, 360);
                g2.dispose();
            }
        };
        leftPanel.setPreferredSize(new Dimension(420, 0));
        leftPanel.setOpaque(false);

        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);
        brandBox.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));

        String roleIcon = role.equalsIgnoreCase("student") ? "🎓" : "⚙";
        JLabel roleLogo = new JLabel(roleIcon, SwingConstants.CENTER);
        roleLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        roleLogo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel brandName = new JLabel("AutoTime");
        brandName.setFont(new Font("Inter", Font.BOLD, 42));
        brandName.setForeground(Color.WHITE);
        brandName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel portalLbl = new JLabel(titleCase(role) + " Portal");
        portalLbl.setFont(new Font("Inter", Font.PLAIN, 18));
        portalLbl.setForeground(new Color(0xC7, 0xD2, 0xFE));
        portalLbl.setAlignmentX(CENTER_ALIGNMENT);

        String[] features = {"Automated Scheduling", "Conflict Resolution", "Role-Based Access"};

        brandBox.add(Box.createVerticalGlue());
        brandBox.add(roleLogo);
        brandBox.add(Box.createVerticalStrut(22));
        brandBox.add(brandName);
        brandBox.add(Box.createVerticalStrut(7));
        brandBox.add(portalLbl);
        brandBox.add(Box.createVerticalStrut(38));
        for (String f : features) {
            JLabel feat = new JLabel("✓  " + f);
            feat.setFont(new Font("Inter", Font.PLAIN, 14));
            feat.setForeground(new Color(0xC7, 0xD2, 0xFE));
            feat.setAlignmentX(CENTER_ALIGNMENT);
            brandBox.add(feat);
            brandBox.add(Box.createVerticalStrut(10));
        }
        brandBox.add(Box.createVerticalGlue());

        leftPanel.add(brandBox);
        add(leftPanel, BorderLayout.WEST);

        // ── Right form panel ──────────────────────────────────────────────
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(StyleConstants.BG_DARK);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(400, 520));
        form.setMaximumSize(new Dimension(400, 520));

        // Back button
        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Inter", Font.BOLD, 13));
        backBtn.setForeground(StyleConstants.ACCENT);
        backBtn.setOpaque(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(LEFT_ALIGNMENT);
        if (onBack != null) {
            backBtn.addActionListener(e -> onBack.run());
        } else {
            backBtn.setVisible(false);
        }
        form.add(backBtn);
        form.add(Box.createVerticalStrut(20));

        // Heading
        JLabel heading = new JLabel("Welcome back");
        heading.setFont(new Font("Inter", Font.BOLD, 32));
        heading.setForeground(StyleConstants.TEXT_PRIMARY);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subheading = new JLabel("Sign in to your " + role.toLowerCase() + " account");
        subheading.setFont(StyleConstants.FONT_BODY);
        subheading.setForeground(StyleConstants.TEXT_SECONDARY);
        subheading.setAlignmentX(LEFT_ALIGNMENT);

        form.add(heading);
        form.add(Box.createVerticalStrut(6));
        form.add(subheading);
        form.add(Box.createVerticalStrut(40));

        // Username / Student ID
        boolean isStudent = role.equalsIgnoreCase("student");
        JLabel userLabel = new JLabel(isStudent ? "STUDENT ID" : "USERNAME");
        userLabel.setFont(StyleConstants.FONT_LABEL);
        userLabel.setForeground(StyleConstants.TEXT_SECONDARY);
        userLabel.setAlignmentX(LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        StyleConstants.applyFieldStyle(usernameField, isStudent ? "e.g. S101" : "Administrator username");
        usernameField.setAlignmentX(LEFT_ALIGNMENT);

        // Password
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(StyleConstants.FONT_LABEL);
        passLabel.setForeground(StyleConstants.TEXT_SECONDARY);
        passLabel.setAlignmentX(LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        StyleConstants.applyFieldStyle(passwordField, "••••••••");
        passwordField.setAlignmentX(LEFT_ALIGNMENT);

        // Error
        errorLabel = new JLabel(" ");
        errorLabel.setFont(StyleConstants.FONT_SMALL);
        errorLabel.setForeground(StyleConstants.DANGER);
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Sign In button
        JButton loginBtn = new JButton("Sign In  →");
        loginBtn.setBackground(StyleConstants.ACCENT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Inter", Font.BOLD, 15));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 12,20,12,20");
        loginBtn.setAlignmentX(LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginBtn.addActionListener(e -> attemptLogin());

        // Hint
        String hintText = isStudent ? "Demo credentials:  S101 / stud123" : "Default credentials:  admin / admin123";
        JLabel hint = new JLabel(hintText);
        hint.setFont(StyleConstants.FONT_SMALL);
        hint.setForeground(StyleConstants.TEXT_MUTED);
        hint.setAlignmentX(LEFT_ALIGNMENT);

        form.add(userLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(20));
        form.add(passLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(8));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(14));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(18));
        form.add(hint);

        rightPanel.add(form);
        add(rightPanel, BorderLayout.CENTER);

        // Enter-key submits
        passwordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptLogin();
            }
        });
    }

    // ── Auth logic ────────────────────────────────────────────────────────────

    private void attemptLogin() {
        String idUser = usernameField.getText().trim();
        String pass   = new String(passwordField.getPassword());
        if (idUser.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }
        if (role.equalsIgnoreCase("admin")) {
            if (DatabaseManager.getInstance().authenticate(idUser, pass)) {
                onLoginSuccess.run();
            } else {
                showError("Incorrect admin credentials.");
            }
        } else if (role.equalsIgnoreCase("student")) {
            models.Student s = new database.StudentDAO().authenticate(idUser, pass);
            if (s != null) {
                if (onUserAuthenticated != null) onUserAuthenticated.accept(s);
                onLoginSuccess.run();
            } else {
                showError("Invalid Student ID or password.");
            }
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        usernameField.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        passwordField.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        passwordField.setText("");
    }

    private String titleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    // ── Test support ──────────────────────────────────────────────────────────
    public void setUsernameForTesting(String user) { usernameField.setText(user); }
    public void setPasswordForTesting(String pass)  { passwordField.setText(pass); }
    public void clickLoginForTesting()              { attemptLogin(); }
    public String getErrorTextForTesting()          { return errorLabel.getText(); }
}
