package database;

import models.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private final Connection conn;

    public StudentDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public Student authenticate(String studentId, String password) {
        String sql = "SELECT * FROM students WHERE student_id = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Student(
                    rs.getInt("id"),
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getString("department"),
                    rs.getString("section")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Student> getAll() {
        List<Student> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students ORDER BY student_id");
            while (rs.next()) {
                list.add(new Student(
                    rs.getInt("id"),
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getString("department"),
                    rs.getString("section")
                ));
            }
        } catch (SQLException e) {
        }
        return list;
    }

    public boolean insert(Student s) {
        String sql = "INSERT INTO students (student_id, name, password, department, section) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getStudentId());
            ps.setString(2, s.getName());
            ps.setString(3, s.getPassword());
            ps.setString(4, s.getDepartment());
            ps.setString(5, s.getSection());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
