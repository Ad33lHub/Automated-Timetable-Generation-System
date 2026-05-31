package integration.auth;

import database.DatabaseManager;
import database.StudentDAO;
import models.Student;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests — Authentication Layer
 * Tests the login flow end-to-end:
 *   LoginPanel → DatabaseManager.authenticate() → SQLite (admin)
 *   LoginPanel → StudentDAO.authenticate()       → SQLite (student)
 *
 * These tests use the real database connection established by DatabaseManager.
 * Default admin credentials: admin / admin123 (seeded by seedDefaultAdmin())
 * Default student credentials: S101 / stud123 (seeded by syncDefaultData())
 *
 * Test IDs: TC-INTG-14 through TC-INTG-19
 */
@DisplayName("Integration Tests: Authentication — Admin and Student Login")
public class AuthenticationIntegrationTest {

    private DatabaseManager dbManager;
    private StudentDAO studentDAO;

    @BeforeEach
    void setUp() {
        dbManager  = DatabaseManager.getInstance();
        studentDAO = new StudentDAO();
        // Ensure default data is present
        dbManager.syncDefaultData();
    }

    // ── Admin Authentication ──────────────────────────────────────────────────

    @Test
    @DisplayName("TC-INTG-14: Admin authenticate() returns TRUE for valid default credentials (admin/admin123)")
    void testAdminAuthValidCredentials() {
        boolean result = dbManager.authenticate("admin", "admin123");
        assertTrue(result,
            "DatabaseManager.authenticate() must return true for seeded admin credentials");
    }

    @Test
    @DisplayName("TC-INTG-15: Admin authenticate() returns FALSE for correct username but wrong password")
    void testAdminAuthWrongPassword() {
        boolean result = dbManager.authenticate("admin", "wrongpass");
        assertFalse(result,
            "DatabaseManager.authenticate() must return false for incorrect password");
    }

    @Test
    @DisplayName("TC-INTG-16: Admin authenticate() returns FALSE for non-existent username")
    void testAdminAuthNonExistentUser() {
        boolean result = dbManager.authenticate("ghost_user_xyz", "anything");
        assertFalse(result,
            "DatabaseManager.authenticate() must return false for a username that does not exist in DB");
    }

    @Test
    @DisplayName("TC-INTG-17: Admin authenticate() returns FALSE for empty username and password")
    void testAdminAuthEmptyCredentials() {
        boolean result = dbManager.authenticate("", "");
        assertFalse(result,
            "DatabaseManager.authenticate() must return false for empty string credentials");
    }

    @Test
    @DisplayName("TC-INTG-18: Admin authenticate() handles SQL injection attempt safely (does NOT authenticate)")
    void testAdminAuthSqlInjectionBlocked() {
        // Attempt classic SQL injection — must not authenticate
        boolean result = dbManager.authenticate("admin'--", "' OR '1'='1");
        assertFalse(result,
            "PreparedStatement must parameterize input; SQL injection attempt must not authenticate");
    }

    // ── Student Authentication ────────────────────────────────────────────────

    @Test
    @DisplayName("TC-INTG-19: StudentDAO.authenticate() returns Student object for valid credentials (S101/stud123)")
    void testStudentAuthValidCredentials() {
        Student student = studentDAO.authenticate("S101", "stud123");
        assertNotNull(student,                 "Valid student login must return a non-null Student object");
        assertEquals("S101", student.getStudentId(), "Returned student must have correct ID");
        assertNotNull(student.getName(),       "Student name must not be null");
        assertNotNull(student.getSection(),    "Student section must not be null");
    }

    @Test
    @DisplayName("TC-INTG-20: StudentDAO.authenticate() returns NULL for wrong student password")
    void testStudentAuthWrongPassword() {
        Student result = studentDAO.authenticate("S101", "wrongpass");
        assertNull(result,
            "StudentDAO.authenticate() must return null for incorrect password");
    }

    @Test
    @DisplayName("TC-INTG-21: StudentDAO.authenticate() returns NULL for non-existent student ID")
    void testStudentAuthNonExistentId() {
        Student result = studentDAO.authenticate("S999", "stud123");
        assertNull(result,
            "StudentDAO.authenticate() must return null for a student ID not in the database");
    }
}
