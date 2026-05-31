package unit.generator;

import database.TimetableGenerator;
import database.TimetableGenerator.ConflictRecord;
import database.TimetableGenerator.GenerationResult;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests — TimetableGenerator business logic
 * Only tests pure logic that does NOT touch the database.
 * isTeacherAvailable() is the primary testable public method.
 *
 * Test IDs: TC-UNIT-19 through TC-UNIT-26
 */
@DisplayName("Unit Tests: TimetableGenerator Logic")
public class TimetableGeneratorUnitTest {

    private TimetableGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new TimetableGenerator();
    }

    // ─────────────── isTeacherAvailable ─────────────────────────────────────

    @Test
    @DisplayName("TC-UNIT-19: Exact day match — teacher available on listed day")
    void testAvailableExactDayMatch() {
        assertTrue(generator.isTeacherAvailable("Monday, Wednesday, Friday", "Monday"),
            "Monday is explicitly listed → available");
        assertTrue(generator.isTeacherAvailable("Monday, Wednesday, Friday", "Wednesday"),
            "Wednesday is explicitly listed → available");
        assertTrue(generator.isTeacherAvailable("Monday, Wednesday, Friday", "Friday"),
            "Friday is explicitly listed → available");
    }

    @Test
    @DisplayName("TC-UNIT-20: Exact day mismatch — teacher NOT available on unlisted day")
    void testNotAvailableUnlistedDay() {
        assertFalse(generator.isTeacherAvailable("Monday, Wednesday", "Tuesday"),
            "Tuesday not listed → must return false");
        assertFalse(generator.isTeacherAvailable("Tuesday, Thursday", "Friday"),
            "Friday not listed → must return false");
        assertFalse(generator.isTeacherAvailable("Monday, Tuesday", "Wednesday"),
            "Wednesday not listed → the conflict demo teacher scenario");
    }

    @Test
    @DisplayName("TC-UNIT-21: Short-form availability 'Mon' matches full day name 'Monday'")
    void testShortFormMatchesFull() {
        assertTrue(generator.isTeacherAvailable("Mon, Wed", "Monday"),
            "Abbreviated 'Mon' must match full slot day 'Monday'");
        assertTrue(generator.isTeacherAvailable("Tue, Thu", "Tuesday"),
            "Abbreviated 'Tue' must match 'Tuesday'");
        assertTrue(generator.isTeacherAvailable("Wed, Fri", "Wednesday"),
            "Abbreviated 'Wed' must match 'Wednesday'");
    }

    @Test
    @DisplayName("TC-UNIT-22: Full availability 'Monday' matches short slot day 'Mon'")
    void testFullMatchesShortFormSlot() {
        assertTrue(generator.isTeacherAvailable("Monday, Wednesday", "Mon"),
            "Full 'Monday' must match abbreviated slot 'Mon'");
    }

    @Test
    @DisplayName("TC-UNIT-23: Keyword 'All' means available on every day")
    void testKeywordAll() {
        assertTrue(generator.isTeacherAvailable("All", "Monday"),   "All → available Monday");
        assertTrue(generator.isTeacherAvailable("All", "Tuesday"),  "All → available Tuesday");
        assertTrue(generator.isTeacherAvailable("All", "Saturday"), "All → available Saturday");
    }

    @Test
    @DisplayName("TC-UNIT-24: Keyword 'Available' means available on every day")
    void testKeywordAvailable() {
        assertTrue(generator.isTeacherAvailable("Available", "Wednesday"), "Available → any day");
        assertTrue(generator.isTeacherAvailable("Available", "Friday"),    "Available → any day");
    }

    @Test
    @DisplayName("TC-UNIT-25: Keyword 'All Days' means available on every day")
    void testKeywordAllDays() {
        assertTrue(generator.isTeacherAvailable("All Days", "Thursday"), "All Days → any day");
    }

    @Test
    @DisplayName("TC-UNIT-26: Null availability defaults to available (universal access)")
    void testNullAvailabilityDefaultsToAvailable() {
        assertTrue(generator.isTeacherAvailable(null, "Monday"),
            "Null availability means no restriction — teacher is available");
    }

    @Test
    @DisplayName("TC-UNIT-27: Blank/empty string availability defaults to available")
    void testBlankAvailabilityDefaultsToAvailable() {
        assertTrue(generator.isTeacherAvailable("", "Friday"),
            "Empty string means no restriction — teacher is available");
        assertTrue(generator.isTeacherAvailable("   ", "Tuesday"),
            "Whitespace-only means no restriction — teacher is available");
    }

    @Test
    @DisplayName("TC-UNIT-28: Case-insensitive — lowercase availability matches title-case day")
    void testCaseInsensitiveMatch() {
        assertTrue(generator.isTeacherAvailable("monday, wednesday", "Monday"),
            "Lowercase availability must match any case of day name");
        assertTrue(generator.isTeacherAvailable("MONDAY, FRIDAY", "Monday"),
            "Uppercase availability must match any case of day name");
    }

    // ─────────────── GenerationResult ───────────────────────────────────────

    @Test
    @DisplayName("TC-UNIT-29: GenerationResult initializes with zero counts and empty collections")
    void testGenerationResultInitialState() {
        GenerationResult result = new GenerationResult();

        assertEquals(0, result.successCount, "successCount must start at 0");
        assertEquals(0, result.failCount,    "failCount must start at 0");
        assertEquals(0, result.swapResolutions, "swapResolutions must start at 0");
        assertNotNull(result.conflicts,   "conflicts list must be initialized (not null)");
        assertNotNull(result.logs,        "logs list must be initialized (not null)");
        assertNotNull(result.preWarnings, "preWarnings list must be initialized (not null)");
        assertTrue(result.conflicts.isEmpty(),   "conflicts must be empty on creation");
        assertTrue(result.logs.isEmpty(),        "logs must be empty on creation");
        assertTrue(result.preWarnings.isEmpty(), "preWarnings must be empty on creation");
    }

    @Test
    @DisplayName("TC-UNIT-30: GenerationResult.toLogLines() includes summary line with scheduled count")
    void testGenerationResultToLogLinesIncludesSummary() {
        GenerationResult result = new GenerationResult();
        result.successCount = 6;
        result.failCount    = 2;
        result.logs.add("Generation complete.");

        var lines = result.toLogLines();
        assertFalse(lines.isEmpty(), "toLogLines() must return non-empty list");
        assertTrue(lines.stream().anyMatch(l -> l.contains("Scheduled") && l.contains("6")),
            "Summary must report scheduled count of 6");
    }

    // ─────────────── ConflictRecord ─────────────────────────────────────────

    @Test
    @DisplayName("TC-UNIT-31: ConflictRecord stores all five fields immutably")
    void testConflictRecordFieldStorage() {
        ConflictRecord cr = new ConflictRecord(
            "Database Systems", "BCS-SP26-1",
            "Mr. Tariq Hassan",
            "Teacher available on Monday, Tuesday only — section fully booked on those days.",
            "Extend teacher availability or reduce course load.");

        assertEquals("Database Systems",   cr.subjectName);
        assertEquals("BCS-SP26-1",         cr.section);
        assertEquals("Mr. Tariq Hassan",   cr.teacherName);
        assertNotNull(cr.reason,            "reason must not be null");
        assertNotNull(cr.suggestion,        "suggestion must not be null");
        assertTrue(cr.reason.length() > 0, "reason must be non-empty string");
    }

    @Test
    @DisplayName("TC-UNIT-32: ConflictRecord allows null suggestion without NPE")
    void testConflictRecordNullSuggestion() {
        ConflictRecord cr = new ConflictRecord(
            "Operating Systems", "BCS-SP26-1", "Dr. Mudassar",
            "Slot conflict detected.", null);

        assertNull(cr.suggestion, "null suggestion must be stored as null (not thrown)");
    }
}
