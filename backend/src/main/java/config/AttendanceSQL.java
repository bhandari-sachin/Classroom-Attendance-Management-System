package config;

import model.Attendance;
import model.AttendanceStatus;
import dto.AttendanceView;
import model.MarkedBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AttendanceSQL {

    public boolean exists(Long studentId, Long sessionId) {

        String sql = """
        SELECT COUNT(*) FROM attendance
        WHERE student_id = ? AND session_id = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setLong(2, sessionId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void save(Attendance attendance) {
        String sql = "INSERT INTO attendance (student_id, session_id, status, marked_by) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, attendance.getStudentId());
            stmt.setLong(2, attendance.getSessionId());
            stmt.setString(3, attendance.getStatus().name());
            stmt.setString(4, attendance.getMarkedBy().name());

            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateStatus(
            Long studentId,
            Long sessionId,
            AttendanceStatus status,
            MarkedBy markedBy
    ) {

        String sql = """
        UPDATE attendance
        SET status = ?, marked_by = ?
        WHERE student_id = ? AND session_id = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, markedBy.name());
            stmt.setLong(3, studentId);
            stmt.setLong(4, sessionId);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Fetch attendance records for a student
    public List<Attendance> findByStudentId(Long studentId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance(
                        rs.getLong("student_id"),
                        rs.getLong("session_id"),
                        AttendanceStatus.valueOf(rs.getString("status")),
                        MarkedBy.valueOf(rs.getString("marked_by"))
                );
                attendanceList.add(attendance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    // Fetch attendance records for a class
    public List<Attendance> findByClassId(Long classId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT a.* FROM attendance a JOIN sessions s ON a.session_id = s.id WHERE s.class_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance(
                        rs.getLong("student_id"),
                        rs.getLong("session_id"),
                        AttendanceStatus.valueOf(rs.getString("status")),
                        MarkedBy.valueOf(rs.getString("marked_by"))
                );
                attendanceList.add(attendance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    // Filter attendance records by student details
    public List<AttendanceView> filterAttendanceByStudent(
            Long classId,
            String searchTerm
    ) {

        List<AttendanceView> results = new ArrayList<>();

        String sql = """
        SELECT u.id AS student_id,
               u.first_name,
               u.last_name,
               se.session_date,
               a.status
        FROM attendance a
        JOIN sessions se ON a.session_id = se.id
        JOIN users u ON a.student_id = u.id  
        WHERE se.class_id = ?
          AND (
                CAST(u.id AS CHAR) LIKE ?
             OR LOWER(u.first_name) LIKE ?
             OR LOWER(u.last_name) LIKE ?
             OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE ?
          )
        ORDER BY u.last_name, u.first_name, se.session_date
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String term = "%" + searchTerm.toLowerCase() + "%";

            stmt.setLong(1, classId);
            stmt.setString(2, "%" + searchTerm + "%"); // student ID
            stmt.setString(3, term);                   // first name
            stmt.setString(4, term);                   // last name
            stmt.setString(5, term);                   // full name

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(new AttendanceView(
                        rs.getLong("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getDate("session_date").toLocalDate(),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public String getSessionCode(Long sessionId) {
        String sql = "SELECT qr_token FROM sessions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("qr_token");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public dto.AttendanceStats getOverallStats() {
        String sql = """
        SELECT
          SUM(status = 'PRESENT') AS presentCount,
          SUM(status = 'ABSENT')  AS absentCount,
          SUM(status = 'EXCUSED') AS excusedCount,
          COUNT(*)                AS totalRecords
        FROM attendance
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int present = rs.getInt("presentCount");
                int absent = rs.getInt("absentCount");
                int excused = rs.getInt("excusedCount");
                int total = rs.getInt("totalRecords");
                return new dto.AttendanceStats(present, absent, excused, total);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new dto.AttendanceStats(0,0,0,0);
    }
    public dto.AttendanceStats getStudentStats(Long studentId) {
        String sql = """
        SELECT
          SUM(status = 'PRESENT') AS presentCount,
          SUM(status = 'ABSENT')  AS absentCount,
          SUM(status = 'EXCUSED') AS excusedCount,
          COUNT(*)                AS totalRecords
        FROM attendance
        WHERE student_id = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int present = rs.getInt("presentCount");
                    int absent = rs.getInt("absentCount");
                    int excused = rs.getInt("excusedCount");
                    int total = rs.getInt("totalRecords");
                    return new dto.AttendanceStats(present, absent, excused, total);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new dto.AttendanceStats(0, 0, 0, 0);
    }
    public Long findSessionIdByCode(String code) {
        String sql = "SELECT id FROM sessions WHERE qr_token = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getLong("id");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<dto.AttendanceView> getStudentAttendanceViews(Long studentId) {
        List<dto.AttendanceView> results = new ArrayList<>();

        String sql = """
        SELECT
            u.id AS student_id,
            u.first_name,
            u.last_name,
            se.session_date,
            a.status
        FROM attendance a
        JOIN sessions se ON a.session_id = se.id
        JOIN users u ON a.student_id = u.id
        WHERE a.student_id = ?
        ORDER BY se.session_date DESC
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new dto.AttendanceView(
                            rs.getLong("student_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getDate("session_date").toLocalDate(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}
