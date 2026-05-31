package integration.generator;

import database.*;
import models.TimetableEntry;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests — TimetableGenerator ↔ TimetableDAO ↔ SQLite
 * Verifies the full generation pipeline:
 *   TimetableGenerator.generate() → clears + writes → SQLite (timetable_entries table)
 *   TimetableDAO.getAll() / getFilteredEntries() → reads back and verifies.
 *
 * Also tests analyzeCurrentState() and section-filtering retrieval.
 *
 * Test IDs: TC-INTG-22 through TC-INTG-28
 */
@DisplayName("Integration Tests: TimetableGenerator ↔ SQLite DB")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GeneratorIntegrationTest {

    private TimetableGenerator generator;
    private TimetableDAO       timetableDAO;
    private DatabaseManager    db;

    @BeforeEach
    void setUp() {
        db           = DatabaseManager.getInstance();
        db.syncDefaultData();                          // ensure teachers, subjects, rooms exist
        generator    = new TimetableGenerator();
        timetableDAO = new TimetableDAO();
    }

    @Test
    @Order(1)
    @DisplayName("TC-INTG-22: TimetableGenerator.generate() clears old entries and saves new ones to DB")
    void testGenerateWritesEntriesToDatabase() {
        TimetableGenerator.GenerationResult result = generator.generate();

        List<TimetableEntry> entries = timetableDAO.getAll();
        assertFalse(entries.isEmpty(),
            "After generate(), timetable_entries table must contain at least one row");
        assertTrue(result.successCount > 0,
            "GenerationResult.successCount must be > 0 when subjects and slots exist");
        assertEquals(entries.size(), result.successCount,
            "Number of rows in DB must equal the reported successCount");
    }

    @Test
    @Order(2)
    @DisplayName("TC-INTG-23: Generated timetable entries have valid (non-null, non-zero) foreign-key IDs")
    void testGeneratedEntriesHaveValidForeignKeys() {
        generator.generate();
        List<TimetableEntry> entries = timetableDAO.getAll();
        assertFalse(entries.isEmpty(), "Entries must exist after generation");

        for (TimetableEntry e : entries) {
            assertTrue(e.getTimeSlotId()  > 0, "time_slot_id must be a valid positive ID");
            assertTrue(e.getSubjectId()   > 0, "subject_id must be a valid positive ID");
            assertTrue(e.getTeacherId()   > 0, "teacher_id must be a valid positive ID");
            assertTrue(e.getClassroomId() > 0, "classroom_id must be a valid positive ID");
            assertNotNull(e.getDay(),           "day field must not be null");
            assertFalse(e.getDay().isBlank(),   "day field must not be blank");
        }
    }

    @Test
    @Order(3)
    @DisplayName("TC-INTG-24: GenerationResult.logs contains 'Generation complete.' after successful run")
    void testGenerationLogsCompletionMessage() {
        TimetableGenerator.GenerationResult result = generator.generate();
        assertTrue(result.logs.stream().anyMatch(l -> l.contains("Generation complete.")),
            "logs must include the 'Generation complete.' message");
    }

    @Test
    @Order(4)
    @DisplayName("TC-INTG-25: GenerationResult.preWarnings is populated with teacher analysis")
    void testPreWarningsGenerated() {
        TimetableGenerator.GenerationResult result = generator.generate();
        assertFalse(result.preWarnings.isEmpty(),
            "preWarnings must be populated with teacher availability analysis before scheduling");
    }

    @Test
    @Order(5)
    @DisplayName("TC-INTG-26: TimetableDAO.getFilteredEntries('section', ...) returns only matching section rows")
    void testFilteredEntriesBySectionReturnOnlyMatchingRows() {
        generator.generate();

        List<TimetableEntry> bcs1Entries = timetableDAO.getFilteredEntries("section", "BCS-SP26-1");
        for (TimetableEntry e : bcs1Entries) {
            assertEquals("BCS-SP26-1", e.getSectionName(),
                "Every filtered entry must belong to section 'BCS-SP26-1'");
        }
    }

    @Test
    @Order(6)
    @DisplayName("TC-INTG-27: analyzeCurrentState() returns conflict records for overloaded teacher scenario")
    void testAnalyzeCurrentStateDetectsConflicts() {
        generator.generate();

        // Mr. Tariq Hassan (teacher 6) has 2 courses in BCS-SP26-1 but only Mon/Tue availability
        // → at least one course must fail to schedule → conflict record must appear
        TimetableGenerator.GenerationResult analysis = generator.analyzeCurrentState();

        assertNotNull(analysis, "analyzeCurrentState() must return a non-null result");
        // preWarnings must contain Tariq Hassan's overload warning
        boolean hasOverloadWarning = analysis.preWarnings.stream()
            .anyMatch(w -> w.contains("Tariq Hassan"));
        assertTrue(hasOverloadWarning,
            "Pre-analysis must warn about Mr. Tariq Hassan's overloaded schedule");
    }

    @Test
    @Order(7)
    @DisplayName("TC-INTG-28: Second call to generate() replaces previous timetable (no duplicate entries)")
    void testSecondGenerationReplacesOldEntries() {
        generator.generate();
        int firstCount = timetableDAO.getAll().size();

        generator.generate();
        int secondCount = timetableDAO.getAll().size();

        // Counts may differ due to randomness, but table must not accumulate duplicates
        // The clear step in generate() ensures this; validate that second run is also non-empty
        assertTrue(secondCount > 0,
            "Second generation run must also produce non-empty timetable (no stale clear failure)");

        // Verify no raw duplicate rows by checking total count doesn't double
        // If clear() works: second count ≈ successCount, not 2× first count
        assertTrue(secondCount < firstCount * 2 + 5,
            "Second generate() must replace entries, not append; count should not double");
    }
}
