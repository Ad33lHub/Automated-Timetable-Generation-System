package blackbox.login;

import gui.LoginPanel;
import database.DatabaseManager;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Black Box Tests — Admin Login Panel
 *
 * Method: Equivalence Partitioning + Boundary Value Analysis
 * Treats LoginPanel as a BLACK BOX: only inputs (credentials) and
 * observable outputs (callback invoked, error message text) are tested.
 * No knowledge of internal implementation is used.
 *
 * Equivalence Partitions:
 *   EP1 — Valid admin credentials   (username=admin, password=admin123)
 *   EP2 — Valid username, wrong pw  (username=admin, password=<anything else>)
 *   EP3 — Non-existent username     (username=<unknown>, password=<any>)
 *   EP4 — Empty username field      (username="", password=<any>)
 *   EP5 — Empty password field      (username=<any>, password="")
 *   EP6 — Both fields empty         (username="", password="")
 *
 * Test IDs: TC-BBOX-01 through TC-BBOX-10
 */
@DisplayName("Black Box Tests: Admin Login Panel")
public class AdminLoginBlackBoxTest {

    private boolean callbackFired;
    private LoginPanel panel;

    @BeforeEach
    void setUp() {
        callbackFired = false;
        DatabaseManager.getInstance().syncDefaultData(); // ensure admin seeded
        panel = new LoginPanel("admin", () -> callbackFired = true, user -> {});
    }

    // ── EP1: Valid credentials ────────────────────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-01: EP1 — Valid admin credentials trigger login-success callback")
    void testValidAdminCredentialsSucceed() {
        panel.setUsernameForTesting("admin");
        panel.setPasswordForTesting("admin123");
        panel.clickLoginForTesting();

        assertTrue(callbackFired,    "onLoginSuccess callback MUST fire for valid credentials");
        assertEquals(" ", panel.getErrorTextForTesting(),
            "Error label must remain blank/empty on successful login");
    }

    // ── EP2: Valid username, wrong password ───────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-02: EP2 — Correct username + wrong password blocks login and shows error")
    void testWrongPasswordBlocksLogin() {
        panel.setUsernameForTesting("admin");
        panel.setPasswordForTesting("wrongPassword");
        panel.clickLoginForTesting();

        assertFalse(callbackFired, "Callback must NOT fire for incorrect password");
        assertEquals("Incorrect admin credentials.", panel.getErrorTextForTesting(),
            "Error message must be 'Incorrect admin credentials.'");
    }

    @Test
    @DisplayName("TC-BBOX-03: EP2 — Password with correct prefix but wrong suffix is rejected")
    void testPartialPasswordRejected() {
        panel.setUsernameForTesting("admin");
        panel.setPasswordForTesting("admin12");  // one char short
        panel.clickLoginForTesting();

        assertFalse(callbackFired, "Partially correct password must NOT authenticate");
        assertEquals("Incorrect admin credentials.", panel.getErrorTextForTesting());
    }

    // ── EP3: Non-existent username ────────────────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-04: EP3 — Non-existent username with any password is rejected")
    void testNonExistentUsernameRejected() {
        panel.setUsernameForTesting("superadmin_ghost");
        panel.setPasswordForTesting("admin123");
        panel.clickLoginForTesting();

        assertFalse(callbackFired, "Non-existent username must be rejected");
        assertEquals("Incorrect admin credentials.", panel.getErrorTextForTesting());
    }

    // ── EP4: Empty username ───────────────────────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-05: EP4 — Empty username with valid password triggers field validation error")
    void testEmptyUsernameTriggersValidation() {
        panel.setUsernameForTesting("");
        panel.setPasswordForTesting("admin123");
        panel.clickLoginForTesting();

        assertFalse(callbackFired, "Login must fail when username is empty");
        assertEquals("Please fill in all fields.", panel.getErrorTextForTesting(),
            "Validation error must say 'Please fill in all fields.'");
    }

    // ── EP5: Empty password ───────────────────────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-06: EP5 — Valid username with empty password triggers field validation error")
    void testEmptyPasswordTriggersValidation() {
        panel.setUsernameForTesting("admin");
        panel.setPasswordForTesting("");
        panel.clickLoginForTesting();

        assertFalse(callbackFired, "Login must fail when password is empty");
        assertEquals("Please fill in all fields.", panel.getErrorTextForTesting(),
            "Validation error must say 'Please fill in all fields.'");
    }

    // ── EP6: Both fields empty ────────────────────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-07: EP6 — Both fields empty triggers field validation, not DB query")
    void testBothFieldsEmptyTriggersValidation() {
        panel.setUsernameForTesting("");
        panel.setPasswordForTesting("");
        panel.clickLoginForTesting();

        assertFalse(callbackFired, "Empty fields must fail before any DB lookup");
        assertEquals("Please fill in all fields.", panel.getErrorTextForTesting());
    }

    // ── Security / Edge Cases ─────────────────────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-08: SQL injection in username field is blocked by PreparedStatement")
    void testSqlInjectionInUsernameBlocked() {
        panel.setUsernameForTesting("admin' OR '1'='1' --");
        panel.setPasswordForTesting("anything");
        panel.clickLoginForTesting();

        assertFalse(callbackFired,
            "SQL injection attempt must NOT authenticate (PreparedStatement parameterizes input)");
        assertEquals("Incorrect admin credentials.", panel.getErrorTextForTesting());
    }

    @Test
    @DisplayName("TC-BBOX-09: SQL injection in password field is blocked by PreparedStatement")
    void testSqlInjectionInPasswordBlocked() {
        panel.setUsernameForTesting("admin");
        panel.setPasswordForTesting("' OR '1'='1");
        panel.clickLoginForTesting();

        assertFalse(callbackFired,
            "SQL injection in password field must NOT bypass authentication");
        assertEquals("Incorrect admin credentials.", panel.getErrorTextForTesting());
    }

    @Test
    @DisplayName("TC-BBOX-10: Case-sensitive username — 'Admin' (capital A) is rejected")
    void testUsernameCaseSensitivity() {
        panel.setUsernameForTesting("Admin");   // capital A — wrong
        panel.setPasswordForTesting("admin123");
        panel.clickLoginForTesting();

        assertFalse(callbackFired,
            "Authentication must be case-sensitive; 'Admin' ≠ 'admin'");
    }
}
