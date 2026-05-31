package database;

public class BlackBoxTestRunner {
    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("            AUTOTIME GUI BLACK BOX TEST EXECUTION RUN                 ");
        System.out.println("======================================================================");

        int guiTotal = 4;
        int guiPassed = 0;

        // Tracker variables for callbacks
        final boolean[] successCallback = { false };
        final Object[] authenticatedUser = { null };

        // Test 1: Admin Login Success
        try {
            System.out.println("\n[RUNNING] TC-BBOX-01: Admin Login with valid credentials");
            successCallback[0] = false;
            gui.LoginPanel adminPanel = new gui.LoginPanel("admin", () -> successCallback[0] = true, user -> authenticatedUser[0] = user);
            
            adminPanel.setUsernameForTesting("admin");
            adminPanel.setPasswordForTesting("admin123");
            adminPanel.clickLoginForTesting();

            assertCondition(successCallback[0], "Success callback should be executed");
            assertCondition(" ".equals(adminPanel.getErrorTextForTesting()), "Error label should remain blank");

            System.out.println("  [RESULT] Expected: Authentication successful; Success Callback triggered.");
            System.out.println("  [RESULT] Actual: Redirect resolved; admin privileges granted.");
            System.out.println("  [STATUS] TC-BBOX-01: PASS");
            guiPassed++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-BBOX-01: FAIL - " + e.getMessage());
        }

        // Test 2: Admin Login Invalid Password
        try {
            System.out.println("\n[RUNNING] TC-BBOX-02: Admin Login with incorrect password");
            successCallback[0] = false;
            gui.LoginPanel adminPanel = new gui.LoginPanel("admin", () -> successCallback[0] = true, user -> authenticatedUser[0] = user);
            
            adminPanel.setUsernameForTesting("admin");
            adminPanel.setPasswordForTesting("wrongpassword");
            adminPanel.clickLoginForTesting();

            assertCondition(!successCallback[0], "Success callback must NOT be executed");
            assertCondition("Incorrect admin credentials.".equals(adminPanel.getErrorTextForTesting()), "Expected error text matches");

            System.out.println("  [RESULT] Expected: Authentication blocked; error message displayed.");
            System.out.println("  [RESULT] Actual: Login blocked, outlines marked red: 'Incorrect admin credentials.'");
            System.out.println("  [STATUS] TC-BBOX-02: PASS");
            guiPassed++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-BBOX-02: FAIL - " + e.getMessage());
        }

        // Test 3: Admin Login Empty Fields Validation
        try {
            System.out.println("\n[RUNNING] TC-BBOX-03: Admin Login with empty credentials");
            successCallback[0] = false;
            gui.LoginPanel adminPanel = new gui.LoginPanel("admin", () -> successCallback[0] = true, user -> authenticatedUser[0] = user);
            
            adminPanel.setUsernameForTesting("");
            adminPanel.setPasswordForTesting("");
            adminPanel.clickLoginForTesting();

            assertCondition(!successCallback[0], "Success callback must NOT be executed");
            assertCondition("Please fill in all fields.".equals(adminPanel.getErrorTextForTesting()), "Expected missing field warning");

            System.out.println("  [RESULT] Expected: UI verification blocks execution; validation warning displayed.");
            System.out.println("  [RESULT] Actual: Form blocked with message: 'Please fill in all fields.'");
            System.out.println("  [STATUS] TC-BBOX-03: PASS");
            guiPassed++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-BBOX-03: FAIL - " + e.getMessage());
        }

        // Test 4: Student Login Invalid Credentials
        try {
            System.out.println("\n[RUNNING] TC-BBOX-04: Student Login with invalid Credentials");
            successCallback[0] = false;
            authenticatedUser[0] = null;
            gui.LoginPanel studentPanel = new gui.LoginPanel("student", () -> successCallback[0] = true, user -> authenticatedUser[0] = user);
            
            studentPanel.setUsernameForTesting("S999");
            studentPanel.setPasswordForTesting("wrongstudentpass");
            studentPanel.clickLoginForTesting();

            assertCondition(!successCallback[0], "Success callback must NOT be executed");
            assertCondition("Invalid Student ID or password.".equals(studentPanel.getErrorTextForTesting()), "Expected invalid student ID warning");
            assertCondition(authenticatedUser[0] == null, "Authenticated student object must be null");

            System.out.println("  [RESULT] Expected: Database student authenticator returns null; login blocked.");
            System.out.println("  [RESULT] Actual: Blocked; output warning: 'Invalid Student ID or password.'");
            System.out.println("  [STATUS] TC-BBOX-04: PASS");
            guiPassed++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-BBOX-04: FAIL - " + e.getMessage());
        }

        System.out.println("\n======================================================================");
        System.out.println("           BLACK BOX SUMMARY: " + guiPassed + " / " + guiTotal + " PASSED          ");
        System.out.println("======================================================================");
        System.exit(0);
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
