package config;

import model.Attendance;
import model.AttendanceStatus;
import model.AttendanceView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AttendanceSQL {

    public void save(Attendance attendance) {
        String sql = "INSERT INTO attendance (student_id, session_id, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, attendance.getStudentId());
            stmt.setLong(2, attendance.getSessionId());
            stmt.setString(3, attendance.getStatus().name());

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
                        AttendanceStatus.valueOf(rs.getString("status"))
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
        String sql = "SELECT a.* FROM attendance a JOIN session s ON a.session_id = s.session_id WHERE s.class_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance(
                        rs.getLong("student_id"),
                        rs.getLong("session_id"),
                        AttendanceStatus.valueOf(rs.getString("status"))
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
        SELECT s.student_id,
               u.first_name,
               u.last_name,
               se.date,
               a.status
        FROM attendance a
        JOIN session se ON a.session_id = se.session_id
        JOIN student s ON a.student_id = s.student_id
        JOIN user u ON s.user_id = u.user_id
        WHERE se.class_id = ?
          AND (
                CAST(s.student_id AS CHAR) LIKE ?
             OR LOWER(u.first_name) LIKE ?
             OR LOWER(u.last_name) LIKE ?
             OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE ?
          )
        ORDER BY u.last_name, u.first_name, se.date
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
                        rs.getDate("date").toLocalDate(),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}
