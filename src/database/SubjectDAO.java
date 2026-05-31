package database;

import models.Subject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {
    private final Connection conn;

    public SubjectDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<Subject> getAll() {
        List<Subject> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM subjects ORDER BY code");
            while (rs.next()) {
                list.add(new Subject(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("code"),
                    rs.getInt("credit_hours"),
                    rs.getString("department"),
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

    public boolean insert(Subject s) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO subjects (name, code, credit_hours, department, section) VALUES (?, ?, ?, ?, ?)"
            );
            ps.setString(1, s.getName());
            ps.setString(2, s.getCode());
            ps.setInt(3, s.getCreditHours());
            ps.setString(4, s.getDepartment());
            ps.setString(5, s.getSection());
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Subject s) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE subjects SET name=?, code=?, credit_hours=?, department=?, section=? WHERE id=?"
            );
            ps.setString(1, s.getName());
            ps.setString(2, s.getCode());
            ps.setInt(3, s.getCreditHours());
            ps.setString(4, s.getDepartment());
            ps.setString(5, s.getSection());
            ps.setInt(6, s.getId());
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
            PreparedStatement ps = conn.prepareStatement("DELETE FROM subjects WHERE id=?");
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
