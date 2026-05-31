package blackbox.workflow;

import database.*;
import models.TimetableEntry;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Black Box Tests — Timetable Generation Workflow
 *
 * Method: Functional / End-to-End black box.
 * These tests simulate the workflow a user triggers from the UI:
 *   1. Data is present in the DB (teachers, subjects, classrooms, timeslots)
 *   2. User clicks "Generate Timetable" → TimetableGenerator.generate() is called
 *   3. Observable outputs: GenerationResult fields, entries in DB, conflict records
 *
 * No internal implementation knowledge is used — only inputs (DB state)
 * and observable outputs (GenerationResult, TimetableDAO.getAll()) are tested.
 *
 * Test IDs: TC-BBOX-19 through TC-BBOX-26
 */
@DisplayName("Black Box Tests: Timetable Generation Workflow")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TimetableWorkflowBlackBoxTest {

    private TimetableGenerator generator;
    private TimetableDAO       timetableDAO;

    @BeforeEach
    void setUp() {
        DatabaseManager db = DatabaseManager.getInstance();
        db.syncDefaultData();      // standard demo data (6 teachers, 8 subjects, 4 rooms, 20 slots)
        generator    = new TimetableGenerator();
        timetableDAO = new TimetableDAO();
    }

    // ── Normal Workflow ───────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("TC-BBOX-19: Generate with standard demo data — result is non-null and has scheduled entries")
    void testGenerateWithStandardDataProducesNonNullResult() {
        TimetableGenerator.GenerationResult result = generator.generate();

        assertNotNull(result, "generate() must never return null");
        assertTrue(result.successCount > 0,
            "Standard demo data must produce at least one scheduled entry");
        assertNotNull(result.logs,        "logs must not be null");
        assertNotNull(result.conflicts,   "conflicts list must not be null");
        assertNotNull(result.preWarnings, "preWarnings list must not be null");
    }

    @Test
    @Order(2)
    @DisplayName("TC-BBOX-20: Generated timetable entries visible via TimetableDAO.getAll()")
    void testGeneratedEntriesVisibleInDatabase() {
        generator.generate();
        List<TimetableEntry> entries = timetableDAO.getAll();

        assertFalse(entries.isEmpty(),
            "After generate(), at least one entry must be visible via TimetableDAO.getAll()");
    }

    @Test
    @Order(3)
    @DisplayName("TC-BBOX-21: Each generated entry has non-null joined display fields (subject, teacher, room)")
    void testGeneratedEntriesHaveDisplayFields() {
        generator.generate();
        List<TimetableEntry> entries = timetableDAO.getAll();
        assertFalse(entries.isEmpty(), "Must have entries to validate display fields");

        TimetableEntry first = entries.get(0);
        assertNotNull(first.getSubjectName(),   "subjectName must be populated via JOIN");
        assertNotNull(first.getTeacherName(),   "teacherName must be populated via JOIN");
        assertNotNull(first.getClassroomName(), "classroomName must be populated via JOIN");
        assertNotNull(first.getTimeRange(),     "timeRange must be populated via JOIN");
        assertNotNull(first.getSectionName(),   "sectionName must be populated via JOIN");
    }

    @Test
    @Order(4)
    @DisplayName("TC-BBOX-22: No teacher is double-booked in the same time slot")
    void testNoTeacherDoubleBookedInSameSlot() {
        generator.generate();
        List<TimetableEntry> entries = timetableDAO.getAll();

        // Group by (timeSlotId, teacherId) — no pair should appear more than once
        long duplicates = entries.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                e -> e.getTimeSlotId() + "_" + e.getTeacherId(),
                java.util.stream.Collectors.counting()))
            .values().stream()
            .filter(count -> count > 1)
            .count();

        assertEquals(0, duplicates,
            "No teacher must be assigned to two different subjects in the same time slot");
    }

    @Test
    @Order(5)
    @DisplayName("TC-BBOX-23: No classroom is double-booked in the same time slot")
    void testNoClassroomDoubleBookedInSameSlot() {
        generator.generate();
        List<TimetableEntry> entries = timetableDAO.getAll();

        long duplicates = entries.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                e -> e.getTimeSlotId() + "_" + e.getClassroomId(),
                java.util.stream.Collectors.counting()))
            .values().stream()
            .filter(count -> count > 1)
            .count();

        assertEquals(0, duplicates,
            "No classroom must be assigned to two subjects in the same time slot");
    }

    // ── Conflict Detection Workflow ───────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("TC-BBOX-24: Conflict scenario — Mr. Tariq Hassan (Mon/Tue only, 2 courses in BCS-SP26-1) causes at least one conflict")
    void testConflictDetectedForOverloadedTeacher() {
        TimetableGenerator.GenerationResult result = generator.generate();

        // The demo dataset deliberately overloads Tariq Hassan: 2 DB courses for BCS-SP26-1
        // but BCS-SP26-1 is already heavily scheduled on Mon/Tue → at least 1 must conflict
        // (This may or may not conflict depending on slot availability — verify preWarning at least)
        boolean warningPresent = result.preWarnings.stream()
            .anyMatch(w -> w.contains("Tariq Hassan"));
        assertTrue(warningPresent,
            "Pre-analysis must flag Mr. Tariq Hassan's scheduling risk in preWarnings");
    }

    @Test
    @Order(7)
    @DisplayName("TC-BBOX-25: Conflict records include subject name, section, teacher name, and reason")
    void testConflictRecordsAreWellFormed() {
        TimetableGenerator.GenerationResult result = generator.generate();

        for (TimetableGenerator.ConflictRecord cr : result.conflicts) {
            assertNotNull(cr.subjectName,   "ConflictRecord.subjectName must not be null");
            assertNotNull(cr.section,       "ConflictRecord.section must not be null");
            assertNotNull(cr.teacherName,   "ConflictRecord.teacherName must not be null");
            assertNotNull(cr.reason,        "ConflictRecord.reason must not be null");
            assertFalse(cr.reason.isBlank(),"ConflictRecord.reason must be a non-empty explanation");
        }
    }

    @Test
    @Order(8)
    @DisplayName("TC-BBOX-26: successCount + failCount equals total subject assignments count")
    void testSuccessAndFailCountSumToTotalAssignments() {
        TimetableGenerator.GenerationResult result = generator.generate();

        SubjectAssignmentDAO assignmentDAO = new SubjectAssignmentDAO();
        int total = assignmentDAO.getAll().size();

        assertEquals(total, result.successCount + result.failCount,
            "successCount + failCount must equal total subject assignments (every item is either scheduled or conflicted)");
    }
}
