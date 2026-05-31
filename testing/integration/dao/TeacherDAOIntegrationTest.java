package integration.dao;

import database.DatabaseManager;
import database.TeacherDAO;
import models.Teacher;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests — TeacherDAO ↔ DatabaseManager ↔ SQLite
 * These tests verify the full data pipeline:
 *   TeacherDAO (DAO layer) → DatabaseManager (connection layer) → timetable.db (SQLite)
 *
 * Inserted test records are always cleaned up in @AfterEach to preserve DB state.
 *
 * Test IDs: TC-INTG-01 through TC-INTG-04
 */
@DisplayName("Integration Tests: TeacherDAO ↔ SQLite Database")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeacherDAOIntegrationTest {

    private TeacherDAO dao;
    private int insertedId = -1;

    @BeforeEach
    void setUp() {
        dao = new TeacherDAO();
    }

    @AfterEach
    void tearDown() {
        // Clean up any test teacher inserted during a test
        if (insertedId > 0) {
            dao.delete(insertedId);
            insertedId = -1;
        }
    }

    @Test
    @Order(1)
    @DisplayName("TC-INTG-01: TeacherDAO.insert() persists record to SQLite and getAll() retrieves it")
    void testInsertAndRetrieve() {
        Teacher newTeacher = new Teacher(0, "Dr. Test Integration", "Testing", "Monday, Friday");

        boolean result = dao.insert(newTeacher);
        assertTrue(result, "insert() must return true on success");

        List<Teacher> all = dao.getAll();
        Teacher found = all.stream()
            .filter(t -> "Dr. Test Integration".equals(t.getName()))
            .findFirst().orElse(null);

        assertNotNull(found, "Inserted teacher must be retrievable via getAll()");
        assertEquals("Testing",          found.getSpecialization(), "Specialization must match");
        assertEquals("Monday, Friday",   found.getAvailability(),   "Availability must match");
        assertTrue(found.getId() > 0,     "DB must assign auto-incremented ID > 0");

        insertedId = found.getId(); // for cleanup
    }

    @Test
    @Order(2)
    @DisplayName("TC-INTG-02: TeacherDAO.update() modifies existing record and DB reflects change")
    void testUpdatePersistsToDatabase() {
        // Setup: insert a fresh teacher
        Teacher t = new Teacher(0, "Prof. Update Test", "Old Spec", "Monday");
        dao.insert(t);
        insertedId = dao.getAll().stream()
            .filter(x -> "Prof. Update Test".equals(x.getName()))
            .mapToInt(Teacher::getId).findFirst().orElse(-1);
        assertTrue(insertedId > 0, "Setup insert must succeed before update test");

        // Act: update the teacher
        Teacher updated = new Teacher(insertedId, "Prof. Update Test", "New Specialization", "Monday, Wednesday");
        boolean updateResult = dao.update(updated);
        assertTrue(updateResult, "update() must return true on success");

        // Assert: re-query the DB to confirm change
        Teacher fromDb = dao.getAll().stream()
            .filter(x -> x.getId() == insertedId)
            .findFirst().orElse(null);

        assertNotNull(fromDb,                    "Updated teacher must still exist in DB");
        assertEquals("New Specialization",       fromDb.getSpecialization(), "DB specialization must reflect update");
        assertEquals("Monday, Wednesday",        fromDb.getAvailability(),   "DB availability must reflect update");
    }

    @Test
    @Order(3)
    @DisplayName("TC-INTG-03: TeacherDAO.delete() removes record; subsequent getAll() excludes it")
    void testDeleteRemovesFromDatabase() {
        // Setup: insert a teacher to delete
        Teacher t = new Teacher(0, "Dr. Delete Me", "Temporary", "Friday");
        dao.insert(t);
        int idToDelete = dao.getAll().stream()
            .filter(x -> "Dr. Delete Me".equals(x.getName()))
            .mapToInt(Teacher::getId).findFirst().orElse(-1);
        assertTrue(idToDelete > 0, "Setup insert must succeed before delete test");

        // Act
        boolean deleteResult = dao.delete(idToDelete);
        assertTrue(deleteResult, "delete() must return true on success");

        // Assert
        boolean stillExists = dao.getAll().stream().anyMatch(x -> x.getId() == idToDelete);
        assertFalse(stillExists, "Deleted teacher must NOT appear in getAll()");
        insertedId = -1; // already deleted, no cleanup needed
    }

    @Test
    @Order(4)
    @DisplayName("TC-INTG-04: TeacherDAO.getAll() returns non-empty list (default seeded data present)")
    void testGetAllReturnsSeededData() {
        // DatabaseManager.syncDefaultData() seeds at least 6 teachers on startup
        List<Teacher> all = dao.getAll();
        assertFalse(all.isEmpty(),           "getAll() must return at least one teacher");
        assertTrue(all.size() >= 6,          "Default seed should produce at least 6 teachers");

        // Verify expected demo teachers are present
        boolean hasMudassar = all.stream().anyMatch(t -> t.getName().contains("Mudassar"));
        assertTrue(hasMudassar, "Seeded teacher 'Dr. Muhammad Mudassar' must exist in DB");
    }

    @Test
    @Order(5)
    @DisplayName("TC-INTG-05: DatabaseManager singleton — getInstance() returns same connection each call")
    void testDatabaseManagerSingleton() {
        DatabaseManager db1 = DatabaseManager.getInstance();
        DatabaseManager db2 = DatabaseManager.getInstance();

        assertSame(db1, db2,
            "DatabaseManager must be a singleton; both references must point to the same object");
        assertNotNull(db1.getConnection(), "Connection must not be null");
    }
}
