package database;

import models.*;
import java.util.*;

public class SampleDataGenerator {
    private final TeacherDAO tDAO = new TeacherDAO();
    private final SubjectDAO sDAO = new SubjectDAO();
    private final ClassroomDAO cDAO = new ClassroomDAO();
    private final TimeSlotDAO tsDAO = new TimeSlotDAO();
    private final StudentDAO stDAO = new StudentDAO();
    private final SubjectAssignmentDAO saDAO = new SubjectAssignmentDAO();

    public void clearAndGenerate() {
        try {
            // 1. Clear everything
            DatabaseManager.getInstance().clearTimetable();
            java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
            java.sql.Statement s = conn.createStatement();
            s.execute("DELETE FROM subject_assignments");
            s.execute("DELETE FROM subjects");
            s.execute("DELETE FROM teachers");
            s.execute("DELETE FROM classrooms");
            s.execute("DELETE FROM time_slots");
            s.execute("DELETE FROM students");
            s.close();

            // 2. Generate Teachers (20 Professional Teachers)
            String[] teacherNames = {
                "Dr. Ahmed Ali", "Prof. Sarah Khan", "Dr. Usman Farooq", "Ms. Fatima Zahra", 
                "Prof. Salman Khan", "Dr. Zunaira Aziz", "Mr. Ali Raza", "Dr. Maria B",
                "Dr. Waqas Ahmed", "Prof. Amna Shah", "Mr. Bilal Hassan", "Dr. Sana Malik",
                "Ms. Rabia Faisal", "Prof. Kamran Shafi", "Dr. Noreen Anwar", "Mr. Zubair Khan",
                "Dr. Faisal Shahzad", "Ms. Sadia Pervez", "Dr. Arshad Mehmood", "Prof. Tehmina"
            };
            String[] specs = {"Computer Science", "Mathematics", "Humanities", "Data Science", "Software Engineering", "AI & Robotics"};
            for (String name : teacherNames) {
                Teacher t = new Teacher(0, name, specs[new Random().nextInt(specs.length)], "Monday, Tuesday, Wednesday, Thursday, Friday");
                tDAO.insert(t);
            }
            List<Teacher> teachers = tDAO.getAll();

            // 3. Generate Classrooms (Diverse set)
            String[] rooms = {"CS-101", "CS-102", "CS-201", "SE-101", "MS-01", "Lab-A", "Lab-B", "Lab-C", "Lab-D", "Seminar Hall"};
            for (String rName : rooms) {
                cDAO.insert(new Classroom(0, rName, rName.contains("Lab") ? 30 : 60, rName.contains("Lab") ? "Laboratory" : "Lecture Hall"));
            }

            // 4. Generate Time Slots (Standard 6 periods)
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
            String[][] times = {{"08:00", "09:05"}, {"09:05", "10:10"}, {"10:10", "11:15"}, {"11:15", "12:20"}, {"12:20", "13:25"}, {"13:25", "14:30"}};
            for (String day : days) {
                for (String[] time : times) {
                    tsDAO.insert(new TimeSlot(0, day, time[0], time[1]));
                }
            }

            // 5. Generate Subjects for 6 Sections (Diverse Departments)
            String[] sections = {"BCS-SP26-1", "BCS-SP26-2", "BSE-FA25-1", "BSE-FA24-4B", "MCS-FA24-1", "BSE-FA25-2A"};
            String[][] allSubjects = {
                {"Programming Fundamentals", "CS101"}, {"Database Systems", "CS302"}, 
                {"Machine Learning", "CS405"}, {"Calculus", "MT101"}, 
                {"Operating Systems", "CS202"}, {"Computer Networks", "CS305"}, 
                {"Software Quality Assurance", "SE401"}, {"Artificial Intelligence", "CS410"},
                {"Software Design", "SE301"}, {"Data Structures", "CS201"},
                {"Web Engineering", "CS311"}, {"Digital Logic Design", "EE102"},
                {"Human Computer Interaction", "CS403"}, {"Numerical Computing", "MT302"},
                {"Cyber Security", "CS450"}
            };
            
            Random rnd = new Random();
            int codeIdx = 100;
            for (String sect : sections) {
                // Select 5-8 random subjects for each section
                int subCount = 5 + rnd.nextInt(4); 
                List<Integer> selectedIndices = new ArrayList<>();
                while (selectedIndices.size() < subCount) {
                    int idx = rnd.nextInt(allSubjects.length);
                    if (!selectedIndices.contains(idx)) selectedIndices.add(idx);
                }

                for (int idx : selectedIndices) {
                    String[] info = allSubjects[idx];
                    Subject sj = new Subject(0, info[0], info[1] + "-" + (codeIdx++), 3, sect.contains("BCS") ? "CS" : "SE", sect);
                    sDAO.insert(sj);
                }
                
                // 6. Generate 10 Students for each section
                for (int i = 1; i <= 10; i++) {
                    String sId = sect.replace("-", "").toLowerCase() + i;
                    stDAO.insert(new Student(0, sId, "Student " + i + " (" + sect + ")", "pass123", sect.split("-")[0], sect));
                }
            }
            List<Subject> subjects = sDAO.getAll();

            // 6. Random Assignments
            Random r = new Random();
            for (Subject sub : subjects) {
                Teacher randT = teachers.get(r.nextInt(teachers.size()));
                saDAO.insert(new SubjectAssignment(0, randT.getId(), sub.getId(), "", "", ""));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
