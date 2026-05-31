package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Timetable Generator Constraint Validation Tests")
public class TimetableGeneratorTest {

    private TimetableGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new TimetableGenerator();
    }

    @Test
    @DisplayName("Verify Teacher Availability - Perfect Matches")
    public void testTeacherAvailableExactMatch() {
        // Test exact matches for availability
        assertTrue(generator.isTeacherAvailable("Monday, Wednesday", "Monday"), "Should be available on Monday");
        assertTrue(generator.isTeacherAvailable("Monday, Wednesday", "Wednesday"), "Should be available on Wednesday");
    }

    @Test
    @DisplayName("Verify Teacher Availability - Short Form Match")
    public void testTeacherAvailableShortFormMatch() {
        // Test short-form vs full-form matching (e.g. "Mon" matches "Monday")
        assertTrue(generator.isTeacherAvailable("Mon, Wed", "Monday"), "Should match Monday with Mon");
        assertTrue(generator.isTeacherAvailable("Monday, Wednesday", "Wed"), "Should match Wed with Wednesday");
    }

    @Test
    @DisplayName("Verify Teacher Availability - Default Open States")
    public void testTeacherAvailableDefaultStates() {
        // Test default states that should allow availability on any day
        assertTrue(generator.isTeacherAvailable("All", "Tuesday"), "All should make teacher available on any day");
        assertTrue(generator.isTeacherAvailable("Available", "Friday"), "Available should make teacher available on any day");
        assertTrue(generator.isTeacherAvailable(null, "Monday"), "Null availability should default to available");
        assertTrue(generator.isTeacherAvailable("", "Monday"), "Empty availability should default to available");
    }

    @Test
    @DisplayName("Verify Teacher Availability - Conflict / Non-Available States")
    public void testTeacherNotAvailable() {
        // Test conflict/non-available states
        assertFalse(generator.isTeacherAvailable("Monday, Wednesday", "Tuesday"), "Should NOT be available on Tuesday if restricted to Mon, Wed");
        assertFalse(generator.isTeacherAvailable("Mon, Wed", "Thursday"), "Should NOT be available on Thursday");
    }
}
