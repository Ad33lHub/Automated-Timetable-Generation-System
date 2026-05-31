package database;

import models.SubjectAssignment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectAssignmentDAO {
    private final Connection conn;

    public SubjectAssignmentDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<SubjectAssignment> getAll() {
        List<SubjectAssignment> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("""
                SELECT sa.id, sa.teacher_id, sa.subject_id,
                       t.name AS teacher_name,
                       s.code || ' - ' || s.name AS subject_name,
                       s.section
                FROM subject_assignments sa
                JOIN teachers t ON sa.teacher_id = t.id
                JOIN subjects s ON sa.subject_id = s.id
                ORDER BY t.name, s.code
            """);
            while (rs.next()) {
                list.add(new SubjectAssignment(
                    rs.getInt("id"),
                    rs.getInt("teacher_id"),
                    rs.getInt("subject_id"),
                    rs.getString("teacher_name"),
                    rs.getString("subject_name"),
                    rs.getString("section")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(SubjectAssignment sa) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO subject_assignments (teacher_id, subject_id) VALUES (?, ?)"
            );
            ps.setInt(1, sa.getTeacherId());
            ps.setInt(2, sa.getSubjectId());
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM subject_assignments WHERE id=?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
