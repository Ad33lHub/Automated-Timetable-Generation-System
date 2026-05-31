package database;

import models.Teacher;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO {
    private final Connection conn;

    public TeacherDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<Teacher> getAll() {
        List<Teacher> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM teachers ORDER BY name");
            while (rs.next()) {
                list.add(new Teacher(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("availability")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Teacher t) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO teachers (name, specialization, availability) VALUES (?, ?, ?)"
            );
            ps.setString(1, t.getName());
            ps.setString(2, t.getSpecialization());
            ps.setString(3, t.getAvailability());
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Teacher t) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE teachers SET name=?, specialization=?, availability=? WHERE id=?"
            );
            ps.setString(1, t.getName());
            ps.setString(2, t.getSpecialization());
            ps.setString(3, t.getAvailability());
            ps.setInt(4, t.getId());
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
            PreparedStatement ps = conn.prepareStatement("DELETE FROM teachers WHERE id=?");
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
