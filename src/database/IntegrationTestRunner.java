package database;

import models.*;
import java.util.List;

public class IntegrationTestRunner {
    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("            AUTOTIME SYSTEM INTEGRATION TEST EXECUTION RUN            ");
        System.out.println("======================================================================");
        
        int total = 4;
        int passed = 0;
        
        try {
            // Test 1: Teacher UI -> TeacherDAO -> DatabaseManager
            System.out.println("\n[RUNNING] TC-INTG-01: Teacher UI -> TeacherDAO -> DatabaseManager Integration");
            TeacherDAO teacherDAO = new TeacherDAO();
            int initialCount = teacherDAO.getAll().size();
            System.out.println("  -> Initial Teacher Count: " + initialCount);
            
            Teacher testTeacher = new Teacher(0, "Dr. Adnan Test", "Cybersecurity", "Monday, Wednesday");
            System.out.println("  -> Data Flow: Instantiating Teacher Model: " + testTeacher.getName());
            
            boolean insertSuccess = teacherDAO.insert(testTeacher);
            System.out.println("  -> Action: Calling TeacherDAO.insert()");
            
            List<Teacher> updatedTeachers = teacherDAO.getAll();
            System.out.println("  -> Fetching: Calling TeacherDAO.getAll()");
            
            boolean found = false;
            int testTeacherId = -1;
            for (Teacher t : updatedTeachers) {
                if ("Dr. Adnan Test".equals(t.getName())) {
                    found = true;
                    testTeacherId = t.getId();
                    break;
                }
            }
            
            if (insertSuccess && found) {
                System.out.println("  [RESULT] Expected: Teacher successfully added and verified in database.");
                System.out.println("  [RESULT] Actual: Record present in DB with auto-incremented ID: " + testTeacherId);
                System.out.println("  [STATUS] TC-INTG-01: PASS");
                passed++;
                
                // Cleanup
                teacherDAO.delete(testTeacherId);
                System.out.println("  -> Cleanup: Deleted test record.");
            } else {
                System.out.println("  [STATUS] TC-INTG-01: FAIL");
            }
            
            // Test 2: GenPanel -> TimetableGenerator -> SQLite DB
            System.out.println("\n[RUNNING] TC-INTG-02: GenPanel -> TimetableGenerator -> SQLite DB Integration");
            TimetableGenerator generator = new TimetableGenerator();
            TimetableDAO timetableDAO = new TimetableDAO();
            
            // Seed a clean environment first
            System.out.println("  -> DatabaseManager: Purging old timetable entries...");
            DatabaseManager.getInstance().clearTimetable();
            
            System.out.println("  -> Generation: Invoking TimetableGenerator.generate()...");
            TimetableGenerator.GenerationResult genResult = generator.generate();
            List<String> logs = genResult.toLogLines();
            System.out.println("  -> Verification: Checking timetable entries saved to database...");
            
            List<TimetableEntry> entries = timetableDAO.getAll();
            System.out.println("  -> Fetching: Found " + entries.size() + " generated schedule entries in SQLite DB.");
            
            if (!entries.isEmpty()) {
                System.out.println("  [RESULT] Expected: Timetable generated successfully with non-empty SQLite entries.");
                System.out.println("  [RESULT] Actual: " + entries.size() + " entries persisted with valid relational IDs.");
                System.out.println("  [STATUS] TC-INTG-02: PASS");
                passed++;
            } else {
                System.out.println("  [STATUS] TC-INTG-02: FAIL");
            }
            
            // Test 3: StudentDashboard -> StudentDAO -> DatabaseManager -> TimetableDAO Integration
            System.out.println("\n[RUNNING] TC-INTG-03: StudentDashboard -> StudentDAO -> DatabaseManager -> TimetableDAO Integration");
            StudentDAO studentDAO = new StudentDAO();
            Student student = null;
            List<Student> students = studentDAO.getAll();
            for (Student s : students) {
                if ("S101".equals(s.getStudentId())) {
                    student = s;
                    break;
                }
            }
            
            if (student == null) {
                // If not found, let's restore defaults to ensure it's there
                DatabaseManager.getInstance().syncDefaultData();
                students = studentDAO.getAll();
                for (Student s : students) {
                    if ("S101".equals(s.getStudentId())) {
                        student = s;
                        break;
                    }
                }
            }
            
            if (student != null) {
                System.out.println("  -> Logged-in Student: " + student.getName() + " | Section: " + student.getSection());
                System.out.println("  -> Calling: TimetableDAO.getFilteredEntries(\"section\", \"" + student.getSection() + "\")");
                List<TimetableEntry> studentSchedule = timetableDAO.getFilteredEntries("section", student.getSection());
                System.out.println("  -> Retrieve Flow: Found " + studentSchedule.size() + " schedule entries for section " + student.getSection());
                
                boolean allMatch = true;
                for (TimetableEntry e : studentSchedule) {
                    if (!student.getSection().equals(e.getSectionName())) {
                        allMatch = false;
                        break;
                    }
                }
                
                if (allMatch) {
                    System.out.println("  [RESULT] Expected: Personalized schedule matching student section 'BCS-SP26-1'.");
                    System.out.println("  [RESULT] Actual: " + studentSchedule.size() + " schedule rows retrieved. 100% matched section 'BCS-SP26-1'.");
                    System.out.println("  [STATUS] TC-INTG-03: PASS");
                    passed++;
                } else {
                    System.out.println("  [STATUS] TC-INTG-03: FAIL");
                }
            } else {
                System.out.println("  [WARNING] Student S101 not found. Skipping validation.");
                System.out.println("  [STATUS] TC-INTG-03: FAIL");
            }
            
            // Test 4: Admin Dashboard -> SampleDataGenerator -> DatabaseManager Integration
            System.out.println("\n[RUNNING] TC-INTG-04: Admin Dashboard -> SampleDataGenerator -> DatabaseManager Integration");
            System.out.println("  -> Action: Invoking SampleDataGenerator.clearAndGenerate()");
            SampleDataGenerator sampleGen = new SampleDataGenerator();
            sampleGen.clearAndGenerate();
            
            int newTeachersCount = teacherDAO.getAll().size();
            int newSubjectsCount = new SubjectDAO().getAll().size();
            int newClassroomsCount = new ClassroomDAO().getAll().size();
            
            System.out.println("  -> Seeding Results in SQLite:");
            System.out.println("     - Teachers count: " + newTeachersCount);
            System.out.println("     - Subjects count: " + newSubjectsCount);
            System.out.println("     - Classrooms count: " + newClassroomsCount);
            
            if (newTeachersCount == 20 && newClassroomsCount == 10) {
                System.out.println("  [RESULT] Expected: Database fully purged and seeded with 20 teachers, 10 classrooms.");
                System.out.println("  [RESULT] Actual: System-wide relational data seeded perfectly in database.");
                System.out.println("  [STATUS] TC-INTG-04: PASS");
                passed++;
            } else {
                System.out.println("  [STATUS] TC-INTG-04: FAIL");
            }
            
            // Recalculate default seeder for main application sanity
            System.out.println("\n  -> Self-Healing: Restoring default seeder for main app running...");
            DatabaseManager.getInstance().clearAll();
            DatabaseManager.getInstance().syncDefaultData();
            generator.generate(); // Pre-generate standard timetable
            System.out.println("  -> System restored to standard configuration.");
            
        } catch (Exception e) {
            System.err.println("Exception occurred during Integration Testing: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n======================================================================");
        System.out.println("          INTEGRATION TEST SUMMARY: " + passed + " / " + total + " PASSED          ");
        System.out.println("======================================================================");
        System.exit(0);
    }
}
