package database;

import models.Classroom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassroomDAO {
    private final Connection conn;

    public ClassroomDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<Classroom> getAll() {
        List<Classroom> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM classrooms ORDER BY room_number");
            while (rs.next()) {
                list.add(new Classroom(
                    rs.getInt("id"),
                    rs.getString("room_number"),
                    rs.getInt("capacity"),
                    rs.getString("type")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Classroom c) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO classrooms (room_number, capacity, type) VALUES (?, ?, ?)"
            );
            ps.setString(1, c.getRoomNumber());
            ps.setInt(2, c.getCapacity());
            ps.setString(3, c.getType());
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Classroom c) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE classrooms SET room_number=?, capacity=?, type=? WHERE id=?"
            );
            ps.setString(1, c.getRoomNumber());
            ps.setInt(2, c.getCapacity());
            ps.setString(3, c.getType());
            ps.setInt(4, c.getId());
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
            PreparedStatement ps = conn.prepareStatement("DELETE FROM classrooms WHERE id=?");
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
