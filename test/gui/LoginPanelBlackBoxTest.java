package gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Login Panel GUI - Automated Black Box Tests")
public class LoginPanelBlackBoxTest {

    private boolean loginSuccessCalled;
    private Object authenticatedUser;
    private LoginPanel adminPanel;
    private LoginPanel studentPanel;

    @BeforeEach
    public void setUp() {
        loginSuccessCalled = false;
        authenticatedUser = null;

        // Instantiate panels under test as black-boxes
        adminPanel = new LoginPanel("admin", () -> loginSuccessCalled = true, user -> authenticatedUser = user);
        studentPanel = new LoginPanel("student", () -> loginSuccessCalled = true, user -> authenticatedUser = user);
    }

    @Test
    @DisplayName("TC-BBOX-01: Admin Login with valid credentials - Success Redirect")
    public void testAdminLoginSuccess() {
        // Input valid credentials (EP: Valid admin partition)
        adminPanel.setUsernameForTesting("admin");
        adminPanel.setPasswordForTesting("admin123");

        // Action
        adminPanel.clickLoginForTesting();

        // Assert
        assertTrue(loginSuccessCalled, "Login success callback should be invoked for valid admin credentials");
        assertEquals(" ", adminPanel.getErrorTextForTesting(), "Error text should remain empty/blank");
    }

    @Test
    @DisplayName("TC-BBOX-02: Admin Login with invalid credentials - Block Access")
    public void testAdminLoginInvalid() {
        // Input invalid password (EP: Invalid password partition)
        adminPanel.setUsernameForTesting("admin");
        adminPanel.setPasswordForTesting("wrongpassword");

        // Action
        adminPanel.clickLoginForTesting();

        // Assert
        assertFalse(loginSuccessCalled, "Login success should NOT be invoked for invalid admin credentials");
        assertEquals("Incorrect admin credentials.", adminPanel.getErrorTextForTesting(), "Descriptive error message should be displayed");
    }

    @Test
    @DisplayName("TC-BBOX-03: Admin Login with empty fields - Input Validation")
    public void testAdminLoginEmptyFields() {
        // Input empty credentials (EP: Missing values partition)
        adminPanel.setUsernameForTesting("");
        adminPanel.setPasswordForTesting("");

        // Action
        adminPanel.clickLoginForTesting();

        // Assert
        assertFalse(loginSuccessCalled, "Login should fail immediately on empty fields");
        assertEquals("Please fill in all fields.", adminPanel.getErrorTextForTesting(), "Validation warning must be displayed");
    }

    @Test
    @DisplayName("TC-BBOX-04: Student Login with invalid credentials - Authentication Block")
    public void testStudentLoginInvalid() {
        // Input invalid student details (EP: Non-existent student partition)
        studentPanel.setUsernameForTesting("S999");
        studentPanel.setPasswordForTesting("wrongstudentpass");

        // Action
        studentPanel.clickLoginForTesting();

        // Assert
        assertFalse(loginSuccessCalled, "Student login must fail with incorrect details");
        assertEquals("Invalid Student ID or password.", studentPanel.getErrorTextForTesting(), "Student error warning must be displayed");
        assertNull(authenticatedUser, "Authenticated user must remain null");
    }
}
