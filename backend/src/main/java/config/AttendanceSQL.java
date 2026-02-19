package config;

import dto.AttendanceStats;
import model.Attendance;
import model.AttendanceStatus;
import dto.AttendanceView;
import model.MarkedBy;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
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

    //Statistics
    public AttendanceStats getOverallStats(){
        String sql = """
        SELECT
            SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present_count,
            SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absent_count,
            SUM(CASE WHEN status = 'EXCUSED' THEN 1 ELSE 0 END) as excused_count,
            COUNT(*) as total_records
        FROM attendance
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int present = rs.getInt("present_count");
                int absent = rs.getInt("absent_count");
                int excused = rs.getInt("excused_count");
                int total = rs.getInt("total_records");

                return new AttendanceStats(present, absent, excused, total);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new AttendanceStats(0, 0, 0, 0);
    }

    public AttendanceStats getStatsForClass(Long classId) {
        String sql = """
        SELECT
            SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present_count,
            SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absent_count,
            SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) as excused_count,
            COUNT(*) as total_records
        FROM attendance a
        JOIN sessions s ON a.session_id = s.id
        WHERE s.class_id = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int present = rs.getInt("present_count");
                int absent = rs.getInt("absent_count");
                int excused = rs.getInt("excused_count");
                int total = rs.getInt("total_records");

                return new AttendanceStats(present, absent, excused, total);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new AttendanceStats(0, 0, 0, 0);
    }

    public AttendanceStats getStatsForStudent(Long studentId) {
        String sql = """
        SELECT
            SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present_count,
            SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absent_count,
            SUM(CASE WHEN status = 'EXCUSED' THEN 1 ELSE 0 END) as excused_count,
            COUNT(*) as total_records
        FROM attendance
        WHERE student_id = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int present = rs.getInt("present_count");
                int absent = rs.getInt("absent_count");
                int excused = rs.getInt("excused_count");
                int total = rs.getInt("total_records");

                return new AttendanceStats(present, absent, excused, total);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new AttendanceStats(0, 0, 0, 0);
    }

    public AttendanceStats getStatsByDateRange(LocalDate start, LocalDate end) {

        String sql = """
        SELECT
            SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count,
            SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count,
            SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) AS excused_count,
            COUNT(*) AS total_records
        FROM attendance a
        JOIN sessions s ON a.session_id = s.id
        WHERE s.session_date BETWEEN ? AND ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new AttendanceStats(
                        rs.getInt("present_count"),
                        rs.getInt("absent_count"),
                        rs.getInt("excused_count"),
                        rs.getInt("total_records")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new AttendanceStats(0,0,0,0);
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
}
