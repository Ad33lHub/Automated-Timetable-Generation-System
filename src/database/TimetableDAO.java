package database;

import models.TimetableEntry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {
    private final Connection conn;

    public TimetableDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void insert(TimetableEntry entry) {
        String sql = "INSERT INTO timetable_entries(day, time_slot_id, subject_id, teacher_id, classroom_id) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getDay());
            pstmt.setInt(2, entry.getTimeSlotId());
            pstmt.setInt(3, entry.getSubjectId());
            pstmt.setInt(4, entry.getTeacherId());
            pstmt.setInt(5, entry.getClassroomId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<TimetableEntry> getAll() {
        return getFilteredEntries(null, null);
    }

    public List<TimetableEntry> getFilteredEntries(String type, Object value) {
        List<TimetableEntry> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT te.*, ts.start_time || ' - ' || ts.end_time as time_range, s.name as sub_name, s.department, s.section, t.name as teach_name, c.room_number " +
            "FROM timetable_entries te " +
            "JOIN time_slots ts ON te.time_slot_id = ts.id " +
            "JOIN subjects s ON te.subject_id = s.id " +
            "JOIN teachers t ON te.teacher_id = t.id " +
            "JOIN classrooms c ON te.classroom_id = c.id "
        );

        if (type != null) {
            if (type.equals("teacher")) sql.append(" WHERE te.teacher_id = ?");
            else if (type.equals("classroom")) sql.append(" WHERE te.classroom_id = ?");
            else if (type.equals("department")) sql.append(" WHERE s.department = ?");
            else if (type.equals("section")) sql.append(" WHERE s.section = ?");
        }
        
        sql.append(" ORDER BY te.day, ts.start_time");

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            if (type != null) {
                if (value instanceof Integer) pstmt.setInt(1, (Integer) value);
                else pstmt.setString(1, (String) value);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TimetableEntry e = new TimetableEntry(
                    rs.getInt("id"), rs.getString("day"), rs.getInt("time_slot_id"),
                    rs.getInt("subject_id"), rs.getInt("teacher_id"), rs.getInt("classroom_id")
                );
                e.setTimeRange(rs.getString("time_range"));
                e.setSubjectName(rs.getString("sub_name"));
                e.setTeacherName(rs.getString("teach_name"));
                e.setClassroomName(rs.getString("room_number"));
                e.setSectionName(rs.getString("section"));
                list.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
