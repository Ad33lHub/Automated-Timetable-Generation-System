package database;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:timetable.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            migrate(); 
            syncDefaultData(); // Self-healing seeder
            seedDefaultAdmin();
        } catch (SQLException e) {
            System.err.println("DB Initialization Error: " + e.getMessage());
        }
    }

    private void migrate() {
        try (Statement st = connection.createStatement()) {
            boolean hasSection = false;
            ResultSet rs = st.executeQuery("PRAGMA table_info(subjects)");
            while (rs.next()) {
                if ("section".equals(rs.getString("name"))) {
                    hasSection = true;
                    break;
                }
            }
            if (!hasSection) {
                st.execute("ALTER TABLE subjects ADD COLUMN section TEXT DEFAULT 'A'");
            }
        } catch (SQLException e) {
            System.err.println("Migration error: " + e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void syncDefaultData() {
        try (Statement st = connection.createStatement()) {
            // 1. Teachers & Classrooms
            ResultSet rsT = st.executeQuery("SELECT COUNT(*) FROM teachers");
            if (rsT.next() && rsT.getInt(1) < 6) {
                System.out.println("Restoring Faculty & Rooms...");
                st.execute("INSERT OR IGNORE INTO teachers (id, name, specialization, availability) VALUES (1, 'Dr. Muhammad Mudassar', 'Computer Science', 'Monday, Tuesday, Wednesday, Thursday, Friday')");
                st.execute("INSERT OR IGNORE INTO teachers (id, name, specialization, availability) VALUES (2, 'Ms. Mariam Fiaz', 'ICT', 'Monday, Wednesday, Friday')");
                st.execute("INSERT OR IGNORE INTO teachers (id, name, specialization, availability) VALUES (3, 'Mr. Qaizar Javed', 'Architecture', 'Monday, Tuesday, Wednesday')");
                st.execute("INSERT OR IGNORE INTO teachers (id, name, specialization, availability) VALUES (4, 'Dr. Salman Iqbal', 'Algorithms', 'Tuesday, Thursday, Friday')");
                st.execute("INSERT OR IGNORE INTO teachers (id, name, specialization, availability) VALUES (5, 'Ms. Hina Naz', 'Mathematics', 'Monday, Wednesday, Thursday')");
                // Teacher 6: limited availability (Monday, Tuesday only) — demonstrates conflict scenario
                // Yeh teacher sirf Monday aur Tuesday ko available hai, aur usse 2 courses BCS-SP26-1 mein padhane hain
                // Yeh section already busy hai in2 dino mein → conflict scenario
                st.execute("INSERT OR IGNORE INTO teachers (id, name, specialization, availability) VALUES (6, 'Mr. Tariq Hassan', 'Database Systems', 'Monday, Tuesday')");

                st.execute("INSERT OR IGNORE INTO classrooms (room_number, capacity, type) VALUES ('MS-6', 60, 'Lecture Hall')");
                st.execute("INSERT OR IGNORE INTO classrooms (room_number, capacity, type) VALUES ('CS-10', 40, 'Lecture Hall')");
                st.execute("INSERT OR IGNORE INTO classrooms (room_number, capacity, type) VALUES ('CS LAB-1', 30, 'Laboratory')");
                st.execute("INSERT OR IGNORE INTO classrooms (room_number, capacity, type) VALUES ('SE-2', 50, 'Lecture Hall')");
            }

            // 2. Time Slots
            ResultSet rsTS = st.executeQuery("SELECT COUNT(*) FROM time_slots");
            if (rsTS.next() && rsTS.getInt(1) < 10) {
                System.out.println("Restoring Time Slots...");
                String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
                String[][] times = {{"08:00", "09:05"}, {"09:05", "10:10"}, {"10:10", "11:15"}, {"11:15", "12:20"}};
                for (String d : days) {
                    for (String[] t : times) {
                        st.execute(String.format("INSERT OR IGNORE INTO time_slots (day, start_time, end_time) VALUES ('%s', '%s', '%s')", d, t[0], t[1]));
                    }
                }
            }

            // 3. Subjects & Assignments
            ResultSet rsS = st.executeQuery("SELECT COUNT(*) FROM subjects");
            if (rsS.next() && rsS.getInt(1) < 5) {
                System.out.println("Restoring Subjects & Assignments...");
                String[][] subs = {
                    {"Operating Systems",        "CS202", "3", "CS", "BCS-SP26-1"},
                    {"Computer Networks",         "CS305", "3", "CS", "BCS-SP26-1"},
                    {"Artificial Intelligence",   "CS410", "4", "CS", "BCS-SP26-2"},
                    {"Software Design",           "SE301", "3", "SE", "BSE-FA25-1"},
                    {"Data Structures",           "CS201", "3", "CS", "BCS-SP26-1"},
                    {"Cyber Security",            "CS450", "3", "CS", "BCS-SP26-2"},
                    // Database subjects assigned to Mr. Tariq Hassan (teacher 6, Mon-Tue only)
                    // → demonstrates availability conflict with BCS-SP26-1 which already has many Mon/Tue classes
                    {"Database Systems",          "CS310", "3", "CS", "BCS-SP26-1"},
                    {"Advanced Database Concepts","CS411", "3", "CS", "BCS-SP26-1"}
                };
                for (int i = 0; i < subs.length; i++) {
                    st.execute(String.format("INSERT OR IGNORE INTO subjects (name, code, credit_hours, department, section) VALUES ('%s','%s',%s,'%s','%s')",
                        subs[i][0], subs[i][1], subs[i][2], subs[i][3], subs[i][4]));
                    // First 6 subjects → teachers 1-5 (round-robin), last 2 → teacher 6
                    int teacherId = (i < 6) ? (i % 5) + 1 : 6;
                    st.execute(String.format("INSERT OR IGNORE INTO subject_assignments (teacher_id, subject_id) SELECT %d, id FROM subjects WHERE code='%s'", teacherId, subs[i][1]));
                }
            }

            // 4. Students (Ensure Demo accounts exist even if others are present)
            ResultSet rsStud = st.executeQuery("SELECT COUNT(*) FROM students WHERE student_id='S101'");
            if (rsStud.next() && rsStud.getInt(1) == 0) {
                st.execute("INSERT OR IGNORE INTO students (student_id, name, password, department, section) VALUES ('S101', 'John Doe', 'stud123', 'CS', 'BCS-SP26-1')");
                st.execute("INSERT OR IGNORE INTO students (student_id, name, password, department, section) VALUES ('S102', 'Jane Smith', 'stud123', 'CS', 'BCS-SP26-2')");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, password TEXT NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS teachers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, specialization TEXT, availability TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS subjects (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, code TEXT NOT NULL UNIQUE, credit_hours INTEGER NOT NULL, department TEXT, section TEXT DEFAULT 'A')");
            stmt.execute("CREATE TABLE IF NOT EXISTS classrooms (id INTEGER PRIMARY KEY AUTOINCREMENT, room_number TEXT NOT NULL UNIQUE, capacity INTEGER NOT NULL, type TEXT NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS time_slots (id INTEGER PRIMARY KEY AUTOINCREMENT, day TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, UNIQUE(day, start_time, end_time))");
            stmt.execute("CREATE TABLE IF NOT EXISTS subject_assignments (id INTEGER PRIMARY KEY AUTOINCREMENT, teacher_id INTEGER NOT NULL, subject_id INTEGER NOT NULL, FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE, FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE, UNIQUE(teacher_id, subject_id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS timetable_entries (id INTEGER PRIMARY KEY AUTOINCREMENT, day TEXT NOT NULL, time_slot_id INTEGER NOT NULL, subject_id INTEGER NOT NULL, teacher_id INTEGER NOT NULL, classroom_id INTEGER NOT NULL, FOREIGN KEY (time_slot_id) REFERENCES time_slots(id) ON DELETE CASCADE, FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE, FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE, FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id TEXT NOT NULL UNIQUE, name TEXT NOT NULL, password TEXT NOT NULL, department TEXT, section TEXT)");
        }
    }

    public void clearAll() {
        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM teachers");
            st.execute("DELETE FROM subjects");
            st.execute("DELETE FROM classrooms");
            st.execute("DELETE FROM time_slots");
            st.execute("DELETE FROM students");
            st.execute("DELETE FROM subject_assignments");
            st.execute("DELETE FROM timetable_entries");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearTimetable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM timetable_entries");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedDefaultAdmin() throws SQLException {
        try (PreparedStatement check = connection.prepareStatement("SELECT COUNT(*) FROM admins WHERE username = 'admin'")) {
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insert = connection.prepareStatement("INSERT INTO admins (username, password) VALUES ('admin', 'admin123')")) {
                    insert.executeUpdate();
                }
            }
        }
    }

    public boolean authenticate(String username, String password) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM admins WHERE username = ? AND password = ?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        try { if (connection != null && !connection.isClosed()) connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}
