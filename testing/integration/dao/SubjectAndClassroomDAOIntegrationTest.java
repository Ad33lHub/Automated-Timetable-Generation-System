package integration.dao;

import database.ClassroomDAO;
import database.SubjectDAO;
import models.Classroom;
import models.Subject;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests — SubjectDAO and ClassroomDAO ↔ SQLite
 * Verifies that both DAOs correctly persist, read, update, and delete
 * via the shared DatabaseManager connection.
 *
 * Test IDs: TC-INTG-06 through TC-INTG-12
 */
@DisplayName("Integration Tests: SubjectDAO and ClassroomDAO ↔ SQLite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubjectAndClassroomDAOIntegrationTest {

    private SubjectDAO  subjectDAO;
    private ClassroomDAO classroomDAO;
    private int insertedSubjectId   = -1;
    private int insertedClassroomId = -1;

    @BeforeEach
    void setUp() {
        subjectDAO   = new SubjectDAO();
        classroomDAO = new ClassroomDAO();
    }

    @AfterEach
    void tearDown() {
        if (insertedSubjectId > 0) {
            subjectDAO.delete(insertedSubjectId);
            insertedSubjectId = -1;
        }
        if (insertedClassroomId > 0) {
            classroomDAO.delete(insertedClassroomId);
            insertedClassroomId = -1;
        }
    }

    // ── SubjectDAO Tests ─────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("TC-INTG-06: SubjectDAO.insert() persists subject with all fields to SQLite")
    void testSubjectInsert() {
        Subject s = new Subject(0, "Test Subject 101", "TST999", 3, "TST", "TEST-SEC");
        boolean ok = subjectDAO.insert(s);
        assertTrue(ok, "insert() must return true");

        Subject found = subjectDAO.getAll().stream()
            .filter(x -> "TST999".equals(x.getCode()))
            .findFirst().orElse(null);

        assertNotNull(found,            "Inserted subject must be found via getAll()");
        assertEquals("Test Subject 101", found.getName(),        "Name must persist");
        assertEquals(3,                  found.getCreditHours(), "Credit hours must persist");
        assertEquals("TST",              found.getDepartment(),  "Department must persist");
        assertEquals("TEST-SEC",         found.getSection(),     "Section must persist");

        insertedSubjectId = found.getId();
    }

    @Test
    @Order(2)
    @DisplayName("TC-INTG-07: SubjectDAO.update() modifies subject record in SQLite")
    void testSubjectUpdate() {
        Subject s = new Subject(0, "Update Me Subject", "UPD001", 2, "CS", "SEC-A");
        subjectDAO.insert(s);
        insertedSubjectId = subjectDAO.getAll().stream()
            .filter(x -> "UPD001".equals(x.getCode()))
            .mapToInt(Subject::getId).findFirst().orElse(-1);

        Subject updated = new Subject(insertedSubjectId, "Updated Subject Name", "UPD001", 4, "SE", "SEC-B");
        boolean ok = subjectDAO.update(updated);
        assertTrue(ok, "update() must return true");

        Subject fromDb = subjectDAO.getAll().stream()
            .filter(x -> x.getId() == insertedSubjectId)
            .findFirst().orElse(null);

        assertNotNull(fromDb,                  "Updated subject must still exist in DB");
        assertEquals("Updated Subject Name",   fromDb.getName(),        "Name update must persist");
        assertEquals(4,                        fromDb.getCreditHours(), "Credit hours update must persist");
        assertEquals("SE",                     fromDb.getDepartment(),  "Department update must persist");
        assertEquals("SEC-B",                  fromDb.getSection(),     "Section update must persist");
    }

    @Test
    @Order(3)
    @DisplayName("TC-INTG-08: SubjectDAO.delete() removes subject; getAll() excludes deleted record")
    void testSubjectDelete() {
        Subject s = new Subject(0, "Delete Me Subject", "DEL002", 3, "CS", "DEL-SEC");
        subjectDAO.insert(s);
        int idToDelete = subjectDAO.getAll().stream()
            .filter(x -> "DEL002".equals(x.getCode()))
            .mapToInt(Subject::getId).findFirst().orElse(-1);

        assertTrue(idToDelete > 0, "Setup insert must produce valid ID");
        assertTrue(subjectDAO.delete(idToDelete), "delete() must return true");

        boolean exists = subjectDAO.getAll().stream().anyMatch(x -> x.getId() == idToDelete);
        assertFalse(exists, "Deleted subject must NOT appear in subsequent getAll()");
        insertedSubjectId = -1;
    }

    @Test
    @Order(4)
    @DisplayName("TC-INTG-09: SubjectDAO.getAll() returns seeded subjects (at least 5 from syncDefaultData)")
    void testSubjectGetAllReturnsSeededData() {
        List<Subject> all = subjectDAO.getAll();
        assertFalse(all.isEmpty(), "getAll() must return at least 1 subject");
        assertTrue(all.size() >= 5, "Default seed should have at least 5 subjects");

        boolean hasOS = all.stream().anyMatch(s -> "CS202".equals(s.getCode()));
        assertTrue(hasOS, "Seeded subject 'Operating Systems (CS202)' must be present");
    }

    // ── ClassroomDAO Tests ───────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("TC-INTG-10: ClassroomDAO.insert() persists classroom to SQLite")
    void testClassroomInsert() {
        Classroom c = new Classroom(0, "TEST-ROOM-99", 25, "Seminar Room");
        boolean ok = classroomDAO.insert(c);
        assertTrue(ok, "ClassroomDAO.insert() must return true");

        Classroom found = classroomDAO.getAll().stream()
            .filter(x -> "TEST-ROOM-99".equals(x.getRoomNumber()))
            .findFirst().orElse(null);

        assertNotNull(found,             "Inserted classroom must be found via getAll()");
        assertEquals(25,                 found.getCapacity(), "Capacity must persist");
        assertEquals("Seminar Room",     found.getType(),     "Type must persist");
        assertTrue(found.getId() > 0,    "DB must assign positive auto-increment ID");

        insertedClassroomId = found.getId();
    }

    @Test
    @Order(6)
    @DisplayName("TC-INTG-11: ClassroomDAO.update() modifies classroom fields in DB")
    void testClassroomUpdate() {
        Classroom c = new Classroom(0, "UPD-ROOM-01", 30, "Laboratory");
        classroomDAO.insert(c);
        insertedClassroomId = classroomDAO.getAll().stream()
            .filter(x -> "UPD-ROOM-01".equals(x.getRoomNumber()))
            .mapToInt(Classroom::getId).findFirst().orElse(-1);

        Classroom updated = new Classroom(insertedClassroomId, "UPD-ROOM-01", 60, "Lecture Hall");
        assertTrue(classroomDAO.update(updated), "update() must return true");

        Classroom fromDb = classroomDAO.getAll().stream()
            .filter(x -> x.getId() == insertedClassroomId)
            .findFirst().orElse(null);

        assertNotNull(fromDb,        "Updated classroom must still exist");
        assertEquals(60,             fromDb.getCapacity(), "Capacity update must persist");
        assertEquals("Lecture Hall", fromDb.getType(),     "Type update must persist");
    }

    @Test
    @Order(7)
    @DisplayName("TC-INTG-12: ClassroomDAO.delete() removes classroom from DB")
    void testClassroomDelete() {
        Classroom c = new Classroom(0, "DEL-ROOM-99", 20, "Seminar Room");
        classroomDAO.insert(c);
        int idToDelete = classroomDAO.getAll().stream()
            .filter(x -> "DEL-ROOM-99".equals(x.getRoomNumber()))
            .mapToInt(Classroom::getId).findFirst().orElse(-1);

        assertTrue(idToDelete > 0, "Setup must produce valid ID");
        assertTrue(classroomDAO.delete(idToDelete), "delete() must return true");

        boolean exists = classroomDAO.getAll().stream().anyMatch(x -> x.getId() == idToDelete);
        assertFalse(exists, "Deleted classroom must NOT exist after delete()");
        insertedClassroomId = -1;
    }

    @Test
    @Order(8)
    @DisplayName("TC-INTG-13: ClassroomDAO.getAll() returns seeded classrooms (at least 4)")
    void testClassroomGetAllSeededData() {
        List<Classroom> all = classroomDAO.getAll();
        assertFalse(all.isEmpty(), "getAll() must not return empty list");
        assertTrue(all.size() >= 4, "Default seed must have at least 4 classrooms");

        boolean hasMS6 = all.stream().anyMatch(c -> "MS-6".equals(c.getRoomNumber()));
        assertTrue(hasMS6, "Seeded classroom 'MS-6' must be present");
    }
}
