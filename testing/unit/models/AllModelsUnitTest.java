package unit.models;

import models.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests — Domain Models
 * Tests every POJO in isolation: constructors, getters, setters, toString().
 * No database, no GUI — pure in-memory validation.
 *
 * Test IDs: TC-UNIT-01 through TC-UNIT-12
 */
@DisplayName("Unit Tests: Domain Model POJOs")
public class AllModelsUnitTest {

    // ─────────────────────────── TEACHER ────────────────────────────────────

    @Test
    @DisplayName("TC-UNIT-01: Teacher all-args constructor populates all fields correctly")
    void testTeacherAllArgsConstructor() {
        Teacher t = new Teacher(10, "Dr. Ali Raza", "Computer Science", "Monday, Wednesday");

        assertEquals(10, t.getId(),                    "ID must match constructor argument");
        assertEquals("Dr. Ali Raza", t.getName(),      "Name must match constructor argument");
        assertEquals("Computer Science", t.getSpecialization(), "Specialization must match");
        assertEquals("Monday, Wednesday", t.getAvailability(), "Availability must match");
    }

    @Test
    @DisplayName("TC-UNIT-02: Teacher setters mutate each field independently")
    void testTeacherSetters() {
        Teacher t = new Teacher();
        t.setId(99);
        t.setName("Ms. Sana Baig");
        t.setSpecialization("Networks");
        t.setAvailability("Tuesday, Thursday, Friday");

        assertEquals(99,              t.getId());
        assertEquals("Ms. Sana Baig", t.getName());
        assertEquals("Networks",      t.getSpecialization());
        assertEquals("Tuesday, Thursday, Friday", t.getAvailability());
    }

    @Test
    @DisplayName("TC-UNIT-03: Teacher no-arg constructor produces default null/zero values")
    void testTeacherNoArgConstructor() {
        Teacher t = new Teacher();
        assertEquals(0,    t.getId(),             "Default id should be 0");
        assertNull(t.getName(),                   "Default name should be null");
        assertNull(t.getSpecialization(),          "Default specialization should be null");
        assertNull(t.getAvailability(),            "Default availability should be null");
    }

    @Test
    @DisplayName("TC-UNIT-04: Teacher toString() returns teacher name only")
    void testTeacherToString() {
        Teacher t = new Teacher(1, "Mr. Qasim", "Math", "All");
        assertEquals("Mr. Qasim", t.toString(),
            "toString() must return name so JComboBox labels display correctly");
    }

    // ─────────────────────────── SUBJECT ────────────────────────────────────

    @Test
    @DisplayName("TC-UNIT-05: Subject all-args constructor populates all six fields")
    void testSubjectAllArgsConstructor() {
        Subject s = new Subject(3, "Data Structures", "CS201", 3, "CS", "BCS-SP26-1");

        assertEquals(3,               s.getId());
        assertEquals("Data Structures", s.getName());
        assertEquals("CS201",          s.getCode());
        assertEquals(3,               s.getCreditHours());
        assertEquals("CS",            s.getDepartment());
        assertEquals("BCS-SP26-1",    s.getSection());
    }

    @Test
    @DisplayName("TC-UNIT-06: Subject toString() returns 'CODE - Name' format")
    void testSubjectToString() {
        Subject s = new Subject(1, "Operating Systems", "CS202", 3, "CS", "A");
        assertEquals("CS202 - Operating Systems", s.toString(),
            "toString() must match drop-down display format 'CODE - Name'");
    }

    @Test
    @DisplayName("TC-UNIT-07: Subject setters update all fields independently")
    void testSubjectSetters() {
        Subject s = new Subject();
        s.setId(7);
        s.setName("AI");
        s.setCode("CS410");
        s.setCreditHours(4);
        s.setDepartment("SE");
        s.setSection("BSE-FA25-1");

        assertEquals(7,           s.getId());
        assertEquals("AI",        s.getName());
        assertEquals("CS410",     s.getCode());
        assertEquals(4,           s.getCreditHours());
        assertEquals("SE",        s.getDepartment());
        assertEquals("BSE-FA25-1", s.getSection());
    }

    // ─────────────────────────── CLASSROOM ──────────────────────────────────

    @Test
    @DisplayName("TC-UNIT-08: Classroom all-args constructor populates all fields")
    void testClassroomAllArgsConstructor() {
        Classroom c = new Classroom(5, "CS LAB-1", 30, "Laboratory");

        assertEquals(5,           c.getId());
        assertEquals("CS LAB-1", c.getRoomNumber());
        assertEquals(30,          c.getCapacity());
        assertEquals("Laboratory", c.getType());
    }

    @Test
    @DisplayName("TC-UNIT-09: Classroom toString() returns 'RoomNumber (Type)' format")
    void testClassroomToString() {
        Classroom c = new Classroom(2, "MS-6", 60, "Lecture Hall");
        assertEquals("MS-6 (Lecture Hall)", c.toString(),
            "toString() must match display format used in scheduler tables");
    }

    @Test
    @DisplayName("TC-UNIT-10: Classroom setters modify capacity and type independently")
    void testClassroomSetters() {
        Classroom c = new Classroom();
        c.setId(12);
        c.setRoomNumber("SE-2");
        c.setCapacity(50);
        c.setType("Seminar Room");

        assertEquals(12,            c.getId());
        assertEquals("SE-2",        c.getRoomNumber());
        assertEquals(50,            c.getCapacity());
        assertEquals("Seminar Room", c.getType());
    }

    // ─────────────────────────── TIMESLOT ───────────────────────────────────

    @Test
    @DisplayName("TC-UNIT-11: TimeSlot all-args constructor stores day and times correctly")
    void testTimeSlotAllArgsConstructor() {
        TimeSlot ts = new TimeSlot(4, "Thursday", "09:05", "10:10");

        assertEquals(4,          ts.getId());
        assertEquals("Thursday", ts.getDay());
        assertEquals("09:05",    ts.getStartTime());
        assertEquals("10:10",    ts.getEndTime());
    }

    @Test
    @DisplayName("TC-UNIT-12: TimeSlot toString() returns 'Day HH:mm - HH:mm' format")
    void testTimeSlotToString() {
        TimeSlot ts = new TimeSlot(1, "Monday", "08:00", "09:05");
        assertEquals("Monday 08:00 - 09:05", ts.toString(),
            "toString() must match timetable display format");
    }

    @Test
    @DisplayName("TC-UNIT-13: TimeSlot setters update day and time range")
    void testTimeSlotSetters() {
        TimeSlot ts = new TimeSlot();
        ts.setId(9);
        ts.setDay("Friday");
        ts.setStartTime("11:15");
        ts.setEndTime("12:20");

        assertEquals(9,        ts.getId());
        assertEquals("Friday", ts.getDay());
        assertEquals("11:15",  ts.getStartTime());
        assertEquals("12:20",  ts.getEndTime());
    }

    // ─────────────────────── SUBJECT ASSIGNMENT ─────────────────────────────

    @Test
    @DisplayName("TC-UNIT-14: SubjectAssignment all-args constructor sets all six fields")
    void testSubjectAssignmentAllArgs() {
        SubjectAssignment sa = new SubjectAssignment(
            1, 3, 7, "Dr. Salman Iqbal", "Algorithms", "BCS-SP26-2");

        assertEquals(1,                sa.getId());
        assertEquals(3,                sa.getTeacherId());
        assertEquals(7,                sa.getSubjectId());
        assertEquals("Dr. Salman Iqbal", sa.getTeacherName());
        assertEquals("Algorithms",     sa.getSubjectName());
        assertEquals("BCS-SP26-2",     sa.getSubjectSection());
    }

    @Test
    @DisplayName("TC-UNIT-15: SubjectAssignment setters mutate teacher/subject IDs independently")
    void testSubjectAssignmentSetters() {
        SubjectAssignment sa = new SubjectAssignment();
        sa.setId(20);
        sa.setTeacherId(5);
        sa.setSubjectId(11);
        sa.setTeacherName("Ms. Hina Naz");
        sa.setSubjectName("Mathematics");
        sa.setSubjectSection("BSE-FA25-1");

        assertEquals(20,             sa.getId());
        assertEquals(5,              sa.getTeacherId());
        assertEquals(11,             sa.getSubjectId());
        assertEquals("Ms. Hina Naz", sa.getTeacherName());
        assertEquals("Mathematics",  sa.getSubjectName());
        assertEquals("BSE-FA25-1",   sa.getSubjectSection());
    }

    // ─────────────────────── TIMETABLE ENTRY ────────────────────────────────

    @Test
    @DisplayName("TC-UNIT-16: TimetableEntry 6-arg constructor stores all FK fields")
    void testTimetableEntryConstructor() {
        TimetableEntry e = new TimetableEntry(1, "Monday", 2, 5, 3, 4);

        assertEquals(1,        e.getId());
        assertEquals("Monday", e.getDay());
        assertEquals(2,        e.getTimeSlotId());
        assertEquals(5,        e.getSubjectId());
        assertEquals(3,        e.getTeacherId());
        assertEquals(4,        e.getClassroomId());
    }

    @Test
    @DisplayName("TC-UNIT-17: TimetableEntry display fields are null until explicitly set")
    void testTimetableEntryDisplayFieldsDefaultNull() {
        TimetableEntry e = new TimetableEntry(1, "Tuesday", 1, 2, 3, 4);

        assertNull(e.getTimeRange(),    "timeRange must be null before set");
        assertNull(e.getSubjectName(),  "subjectName must be null before set");
        assertNull(e.getTeacherName(),  "teacherName must be null before set");
        assertNull(e.getClassroomName(),"classroomName must be null before set");
        assertNull(e.getSectionName(),  "sectionName must be null before set");
    }

    @Test
    @DisplayName("TC-UNIT-18: TimetableEntry display fields set and retrieved correctly")
    void testTimetableEntryDisplayFieldsSet() {
        TimetableEntry e = new TimetableEntry(2, "Wednesday", 3, 6, 1, 2);
        e.setTimeRange("08:00 - 09:05");
        e.setSubjectName("Operating Systems");
        e.setTeacherName("Dr. Muhammad Mudassar");
        e.setClassroomName("MS-6");
        e.setSectionName("BCS-SP26-1");

        assertEquals("08:00 - 09:05",          e.getTimeRange());
        assertEquals("Operating Systems",       e.getSubjectName());
        assertEquals("Dr. Muhammad Mudassar",   e.getTeacherName());
        assertEquals("MS-6",                    e.getClassroomName());
        assertEquals("BCS-SP26-1",              e.getSectionName());
    }
}
