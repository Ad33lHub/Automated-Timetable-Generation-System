package database;

import models.*;

public class UnitTestRunner {
    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("              AUTOTIME SYSTEM UNIT TEST EXECUTION RUN                 ");
        System.out.println("======================================================================");

        int totalTests = 7;
        int passedTests = 0;

        // --------------------------------------------------------------------
        // TEST CASE 1: Teacher Model Integrity
        // --------------------------------------------------------------------
        try {
            System.out.println("\n[RUNNING] TC-UNIT-01: Teacher Model Encapsulation & Getters/Setters");
            Teacher teacher = new Teacher(12, "Dr. John Smith", "Software Architecture", "Tuesday, Thursday");
            
            assertCondition(12 == teacher.getId(), "Expected ID to be 12, got " + teacher.getId());
            assertCondition("Dr. John Smith".equals(teacher.getName()), "Expected Name to match Dr. John Smith");
            assertCondition("Software Architecture".equals(teacher.getSpecialization()), "Expected Spec to match");
            assertCondition("Tuesday, Thursday".equals(teacher.getAvailability()), "Expected Availability to match");

            // Setter modification
            teacher.setId(45);
            teacher.setName("Dr. Sarah Jane");
            teacher.setSpecialization("Database Systems");
            teacher.setAvailability("Monday, Friday");

            assertCondition(45 == teacher.getId(), "Expected modified ID to be 45");
            assertCondition("Dr. Sarah Jane".equals(teacher.getName()), "Expected modified Name to match");
            assertCondition("Database Systems".equals(teacher.getSpecialization()), "Expected modified Spec to match");
            assertCondition("Monday, Friday".equals(teacher.getAvailability()), "Expected modified Availability to match");
            assertCondition("Dr. Sarah Jane".equals(teacher.toString()), "Expected toString() to yield Teacher Name");

            System.out.println("  [RESULT] Expected: Teacher object handles attributes and toString successfully.");
            System.out.println("  [RESULT] Actual: Data integrity verified successfully.");
            System.out.println("  [STATUS] TC-UNIT-01: PASS");
            passedTests++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-UNIT-01: FAIL - " + e.getMessage());
        }

        // --------------------------------------------------------------------
        // TEST CASE 2: Classroom Model Integrity
        // --------------------------------------------------------------------
        try {
            System.out.println("\n[RUNNING] TC-UNIT-02: Classroom Model Encapsulation & Getters/Setters");
            Classroom classroom = new Classroom(5, "Lab 3", 40, "Laboratory");

            assertCondition(5 == classroom.getId(), "Expected ID to be 5");
            assertCondition("Lab 3".equals(classroom.getRoomNumber()), "Expected Room to match Lab 3");
            assertCondition(40 == classroom.getCapacity(), "Expected Capacity to be 40");
            assertCondition("Laboratory".equals(classroom.getType()), "Expected Type to be Laboratory");

            // Setters
            classroom.setId(9);
            classroom.setRoomNumber("Room 401");
            classroom.setCapacity(60);
            classroom.setType("Lecture Hall");

            assertCondition(9 == classroom.getId(), "Expected modified ID to be 9");
            assertCondition("Room 401".equals(classroom.getRoomNumber()), "Expected modified Room to match Room 401");
            assertCondition(60 == classroom.getCapacity(), "Expected modified Capacity to be 60");
            assertCondition("Lecture Hall".equals(classroom.getType()), "Expected modified Type to be Lecture Hall");
            assertCondition("Room 401 (Lecture Hall)".equals(classroom.toString()), "Expected toString() to yield formatted Room representation");

            System.out.println("  [RESULT] Expected: Classroom attributes match boundary configurations.");
            System.out.println("  [RESULT] Actual: Classroom encapsulation validated successfully.");
            System.out.println("  [STATUS] TC-UNIT-02: PASS");
            passedTests++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-UNIT-02: FAIL - " + e.getMessage());
        }

        // --------------------------------------------------------------------
        // TEST CASE 3: TimeSlot Model Integrity
        // --------------------------------------------------------------------
        try {
            System.out.println("\n[RUNNING] TC-UNIT-03: TimeSlot Model Encapsulation & Getters/Setters");
            TimeSlot slot = new TimeSlot(1, "Monday", "08:30", "10:00");

            assertCondition(1 == slot.getId(), "Expected ID to be 1");
            assertCondition("Monday".equals(slot.getDay()), "Expected Day to be Monday");
            assertCondition("08:30".equals(slot.getStartTime()), "Expected Start Time to be 08:30");
            assertCondition("10:00".equals(slot.getEndTime()), "Expected End Time to be 10:00");

            // Setters
            slot.setId(22);
            slot.setDay("Wednesday");
            slot.setStartTime("14:00");
            slot.setEndTime("15:30");

            assertCondition(22 == slot.getId(), "Expected modified ID to be 22");
            assertCondition("Wednesday".equals(slot.getDay()), "Expected modified Day to be Wednesday");
            assertCondition("14:00".equals(slot.getStartTime()), "Expected modified Start Time to be 14:00");
            assertCondition("15:30".equals(slot.getEndTime()), "Expected modified End Time to be 15:30");
            assertCondition("Wednesday 14:00 - 15:30".equals(slot.toString()), "Expected toString() to yield formatted range");

            System.out.println("  [RESULT] Expected: TimeSlot encapsulation operations completed.");
            System.out.println("  [RESULT] Actual: TimeSlot getters, setters, and toString output match standard format.");
            System.out.println("  [STATUS] TC-UNIT-03: PASS");
            passedTests++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-UNIT-03: FAIL - " + e.getMessage());
        }

        // --------------------------------------------------------------------
        // TimetableGenerator Logic Instances
        // --------------------------------------------------------------------
        TimetableGenerator generator = new TimetableGenerator();

        // --------------------------------------------------------------------
        // TEST CASE 4: Teacher Availability - Exact Day Matches
        // --------------------------------------------------------------------
        try {
            System.out.println("\n[RUNNING] TC-UNIT-04: Teacher Availability Exact Day Constraints");
            
            boolean monAvail = generator.isTeacherAvailable("Monday, Wednesday", "Monday");
            boolean wedAvail = generator.isTeacherAvailable("Monday, Wednesday", "Wednesday");
            
            assertCondition(monAvail, "Teacher should be available on Monday");
            assertCondition(wedAvail, "Teacher should be available on Wednesday");

            System.out.println("  [RESULT] Expected: Exact day string availability resolved to True.");
            System.out.println("  [RESULT] Actual: Day validations resolved successfully.");
            System.out.println("  [STATUS] TC-UNIT-04: PASS");
            passedTests++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-UNIT-04: FAIL - " + e.getMessage());
        }

        // --------------------------------------------------------------------
        // TEST CASE 5: Teacher Availability - Shorthand Day Abbreviations
        // --------------------------------------------------------------------
        try {
            System.out.println("\n[RUNNING] TC-UNIT-05: Teacher Availability Shorthand Abbreviation Checks");
            
            boolean fullToShort = generator.isTeacherAvailable("Monday, Wednesday", "Wed");
            boolean shortToFull = generator.isTeacherAvailable("Mon, Wed", "Monday");
            
            assertCondition(fullToShort, "Teacher availability 'Wednesday' should match input shorthand 'Wed'");
            assertCondition(shortToFull, "Teacher availability shorthand 'Mon' should match full day 'Monday'");

            System.out.println("  [RESULT] Expected: Full day matches short day abbreviations.");
            System.out.println("  [RESULT] Actual: Substring abbreviation boundaries verified.");
            System.out.println("  [STATUS] TC-UNIT-05: PASS");
            passedTests++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-UNIT-05: FAIL - " + e.getMessage());
        }

        // --------------------------------------------------------------------
        // TEST CASE 6: Teacher Availability - Unbounded Default Availability
        // --------------------------------------------------------------------
        try {
            System.out.println("\n[RUNNING] TC-UNIT-06: Teacher Availability Wildcard and Null Handling");
            
            boolean allAvail = generator.isTeacherAvailable("All", "Tuesday");
            boolean wordAvail = generator.isTeacherAvailable("Available", "Friday");
            boolean nullAvail = generator.isTeacherAvailable(null, "Monday");
            boolean emptyAvail = generator.isTeacherAvailable("", "Monday");
            
            assertCondition(allAvail, "All-wildcard availability must yield true");
            assertCondition(wordAvail, "Available-wildcard availability must yield true");
            assertCondition(nullAvail, "Null availability must default to true (always available)");
            assertCondition(emptyAvail, "Empty availability must default to true (always available)");

            System.out.println("  [RESULT] Expected: Open and empty states evaluate to True.");
            System.out.println("  [RESULT] Actual: System correctly recovers wildcards as open available days.");
            System.out.println("  [STATUS] TC-UNIT-06: PASS");
            passedTests++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-UNIT-06: FAIL - " + e.getMessage());
        }

        // --------------------------------------------------------------------
        // TEST CASE 7: Teacher Availability - Day Mismatch Conflict Blocking
        // --------------------------------------------------------------------
        try {
            System.out.println("\n[RUNNING] TC-UNIT-07: Teacher Availability Out-of-bounds Day Conflict Prevention");
            
            boolean wrongDay = generator.isTeacherAvailable("Monday, Wednesday", "Tuesday");
            boolean differentAbbrev = generator.isTeacherAvailable("Mon, Wed", "Thursday");
            
            assertCondition(!wrongDay, "Teacher restricted to Monday/Wednesday should NOT be available on Tuesday");
            assertCondition(!differentAbbrev, "Teacher restricted to Mon/Wed should NOT be available on Thursday");

            System.out.println("  [RESULT] Expected: Conflict detected, day mismatches block availability (return False).");
            System.out.println("  [RESULT] Actual: Clashing scheduling days correctly blocked.");
            System.out.println("  [STATUS] TC-UNIT-07: PASS");
            passedTests++;
        } catch (Exception e) {
            System.out.println("  [STATUS] TC-UNIT-07: FAIL - " + e.getMessage());
        }

        System.out.println("\n======================================================================");
        System.out.println("             UNIT TEST SUMMARY: " + passedTests + " / " + totalTests + " PASSED          ");
        System.out.println("======================================================================");
        System.exit(0);
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
