package blackbox.login;

import gui.LoginPanel;
import database.DatabaseManager;
import models.Student;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Black Box Tests — Student Login Panel
 *
 * Method: Equivalence Partitioning
 * Treats LoginPanel("student", ...) as a BLACK BOX.
 * Observes only: callback fired (yes/no), error text, and authenticated user object.
 *
 * Equivalence Partitions:
 *   EP1 — Valid student credentials  (S101 / stud123)
 *   EP2 — Valid ID, wrong password   (S101 / wrongpass)
 *   EP3 — Non-existent student ID    (S999 / stud123)
 *   EP4 — Empty field(s)             (empty ID or empty password)
 *
 * Test IDs: TC-BBOX-11 through TC-BBOX-18
 */
@DisplayName("Black Box Tests: Student Login Panel")
public class StudentLoginBlackBoxTest {

    private boolean callbackFired;
    private Object  authenticatedUser;
    private LoginPanel studentPanel;

    @BeforeEach
    void setUp() {
        callbackFired     = false;
        authenticatedUser = null;
        DatabaseManager.getInstance().syncDefaultData(); // ensure S101 exists
        studentPanel = new LoginPanel(
            "student",
            () -> callbackFired = true,
            user -> authenticatedUser = user
        );
    }

    // ── EP1: Valid student credentials ────────────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-11: EP1 — Valid student S101/stud123 fires callback and returns Student object")
    void testValidStudentLoginSucceeds() {
        studentPanel.setUsernameForTesting("S101");
        studentPanel.setPasswordForTesting("stud123");
        studentPanel.clickLoginForTesting();

        assertTrue(callbackFired,           "onLoginSuccess callback must fire for valid student");
        assertNotNull(authenticatedUser,    "onUserAuthenticated must receive a non-null Student object");
        assertTrue(authenticatedUser instanceof Student,
            "Authenticated object must be an instance of Student model");

        Student s = (Student) authenticatedUser;
        assertEquals("S101", s.getStudentId(), "Returned student ID must match login input");
    }

    @Test
    @DisplayName("TC-BBOX-12: EP1 — Second valid student S102/stud123 also succeeds")
    void testSecondValidStudentLoginSucceeds() {
        studentPanel.setUsernameForTesting("S102");
        studentPanel.setPasswordForTesting("stud123");
        studentPanel.clickLoginForTesting();

        assertTrue(callbackFired, "Second demo student S102 must also authenticate");
        assertNotNull(authenticatedUser, "Student object must be returned for S102");
    }

    // ── EP2: Valid ID, wrong password ─────────────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-13: EP2 — Valid student ID with wrong password blocks login")
    void testWrongPasswordBlocksStudentLogin() {
        studentPanel.setUsernameForTesting("S101");
        studentPanel.setPasswordForTesting("wrongpassword");
        studentPanel.clickLoginForTesting();

        assertFalse(callbackFired,        "Callback must NOT fire for wrong student password");
        assertNull(authenticatedUser,     "authenticatedUser must remain null on failed login");
        assertEquals("Invalid Student ID or password.", studentPanel.getErrorTextForTesting(),
            "Error message must be 'Invalid Student ID or password.'");
    }

    // ── EP3: Non-existent student ID ─────────────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-14: EP3 — Non-existent student ID S999 is rejected")
    void testNonExistentStudentIdRejected() {
        studentPanel.setUsernameForTesting("S999");
        studentPanel.setPasswordForTesting("stud123");
        studentPanel.clickLoginForTesting();

        assertFalse(callbackFired,    "Login must fail for student ID not in DB");
        assertNull(authenticatedUser, "authenticatedUser must be null when student ID not found");
        assertEquals("Invalid Student ID or password.", studentPanel.getErrorTextForTesting());
    }

    @Test
    @DisplayName("TC-BBOX-15: EP3 — Numeric-only ID that doesn't exist is rejected cleanly")
    void testNumericOnlyNonExistentIdRejected() {
        studentPanel.setUsernameForTesting("12345");
        studentPanel.setPasswordForTesting("stud123");
        studentPanel.clickLoginForTesting();

        assertFalse(callbackFired, "Numeric-only non-existent ID must be rejected");
        assertNull(authenticatedUser);
    }

    // ── EP4: Empty fields ─────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-BBOX-16: EP4 — Empty student ID with valid password triggers validation error")
    void testEmptyStudentIdTriggersValidation() {
        studentPanel.setUsernameForTesting("");
        studentPanel.setPasswordForTesting("stud123");
        studentPanel.clickLoginForTesting();

        assertFalse(callbackFired, "Empty student ID must not pass validation");
        assertEquals("Please fill in all fields.", studentPanel.getErrorTextForTesting());
        assertNull(authenticatedUser);
    }

    @Test
    @DisplayName("TC-BBOX-17: EP4 — Valid student ID with empty password triggers validation error")
    void testEmptyStudentPasswordTriggersValidation() {
        studentPanel.setUsernameForTesting("S101");
        studentPanel.setPasswordForTesting("");
        studentPanel.clickLoginForTesting();

        assertFalse(callbackFired, "Empty password must not pass validation");
        assertEquals("Please fill in all fields.", studentPanel.getErrorTextForTesting());
    }

    @Test
    @DisplayName("TC-BBOX-18: EP4 — Both student fields empty triggers validation error")
    void testBothStudentFieldsEmptyTriggersValidation() {
        studentPanel.setUsernameForTesting("");
        studentPanel.setPasswordForTesting("");
        studentPanel.clickLoginForTesting();

        assertFalse(callbackFired, "Both empty fields must fail validation before DB hit");
        assertEquals("Please fill in all fields.", studentPanel.getErrorTextForTesting());
        assertNull(authenticatedUser);
    }
}
