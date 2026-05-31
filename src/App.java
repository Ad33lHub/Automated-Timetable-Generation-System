import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import database.DatabaseManager;
import gui.*;
import javax.swing.*;
import java.awt.*;

public class App extends JFrame {
    private final CardLayout mainLayout = new CardLayout();
    private final JPanel mainContainer = new JPanel(mainLayout);

    public App() {
        setTitle("AutoTime - Automated Timetable Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setMinimumSize(new Dimension(1100, 750));
        setLocationRelativeTo(null);

        // Role Selection is the landing screen
        JPanel roleSelection = new RoleSelectionPanel(this, this::handleRoleSelection);
        mainContainer.add(roleSelection, "landing");

        add(mainContainer);
        mainLayout.show(mainContainer, "landing");
    }

    private void handleRoleSelection(String role) {
        if ("teacher".equals(role)) {
            // Teachers have direct access for now, or you can add login later
            TeacherDashboard td = new TeacherDashboard(() -> mainLayout.show(mainContainer, "landing"));
            mainContainer.add(td, "teacher_dash");
            mainLayout.show(mainContainer, "teacher_dash");
        } else {
            // Admin and Student require login
            LoginPanel lp = new LoginPanel(role,
                () -> onLoginSuccess(role),
                user -> this.authenticatedUser = user,
                () -> mainLayout.show(mainContainer, "landing")
            );
            mainContainer.add(lp, "login");
            mainLayout.show(mainContainer, "login");
        }
    }

    private Object authenticatedUser;

    private void onLoginSuccess(String role) {
        if ("admin".equals(role)) {
            DashboardPanel dp = new DashboardPanel(() -> mainLayout.show(mainContainer, "landing"));
            mainContainer.add(dp, "admin_dash");
            mainLayout.show(mainContainer, "admin_dash");
        } else if ("student".equals(role)) {
            // Cast to student and show dashboard
            models.Student s = (models.Student) authenticatedUser;
            StudentDashboard sd = new StudentDashboard(s, () -> mainLayout.show(mainContainer, "landing"));
            mainContainer.add(sd, "student_dash");
            mainLayout.show(mainContainer, "student_dash");
        }
    }

    public static void main(String[] args) {
        DatabaseManager.getInstance();
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            setupLafDefaults();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new App().setVisible(true);
        });
    }

    private static void setupLafDefaults() {
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Table.rowHeight", 34);
        UIManager.put("Table.selectionBackground", StyleConstants.ACCENT);
        UIManager.put("ScrollBar.showButtons", true);
        UIManager.put("ScrollBar.width", 14);
    }
}
