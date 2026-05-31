package database;

import models.TimeSlot;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotDAO {
    private final Connection conn;

    public TimeSlotDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<TimeSlot> getAll() {
        List<TimeSlot> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM time_slots ORDER BY CASE day " +
                "WHEN 'Monday' THEN 1 WHEN 'Tuesday' THEN 2 WHEN 'Wednesday' THEN 3 " +
                "WHEN 'Thursday' THEN 4 WHEN 'Friday' THEN 5 ELSE 6 END, start_time"
            );
            while (rs.next()) {
                list.add(new TimeSlot(
                    rs.getInt("id"),
                    rs.getString("day"),
                    rs.getString("start_time"),
                    rs.getString("end_time")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(TimeSlot ts) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO time_slots (day, start_time, end_time) VALUES (?, ?, ?)"
            );
            ps.setString(1, ts.getDay());
            ps.setString(2, ts.getStartTime());
            ps.setString(3, ts.getEndTime());
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
            PreparedStatement ps = conn.prepareStatement("DELETE FROM time_slots WHERE id=?");
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
