package config;

import model.Attendance;
import model.AttendanceStatus;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class AttendanceSQL {

    public void save(Attendance attendance) {
        String sql = "INSERT INTO attendance (student_id, session_id, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(sql)) {

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
             var stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            var rs = stmt.executeQuery();

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
             var stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            var rs = stmt.executeQuery();

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
