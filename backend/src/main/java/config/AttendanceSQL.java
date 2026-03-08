package config;

import dto.*;
import model.Attendance;
import model.AttendanceStatus;
import model.MarkedBy;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttendanceSQL {

    public boolean exists(Long studentId, Long sessionId) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND session_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setLong(2, sessionId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (Exception e) {
            throw new RuntimeException("Failed to check attendance", e);
        }
    }

    public void save(Attendance attendance) {
<<<<<<< HEAD
        String sql = "INSERT INTO attendance (student_id, session_id, status, marked_by, remarks) VALUES (?, ?, ?, ?, ?)";
=======
        String sql = "INSERT INTO attendance (student_id, session_id, status, marked_by) VALUES (?, ?, ?, ?)";

>>>>>>> origin/admin-api
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, attendance.getStudentId());
            stmt.setLong(2, attendance.getSessionId());
            stmt.setString(3, attendance.getStatus().name());
            stmt.setString(4, attendance.getMarkedBy().name());
            stmt.setString(5, attendance.getRemarks());

            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to save attendance", e);
        }
    }

<<<<<<< HEAD
    public void updateStatus(
            Long studentId,
            Long sessionId,
            AttendanceStatus status,
            MarkedBy markedBy,
            String remarks) {

        String sql = """
        UPDATE attendance
        SET status = ?, marked_by = ?, remarks = ?
        WHERE student_id = ? AND session_id = ?
    """;
=======
    public void updateStatus(Long studentId, Long sessionId, AttendanceStatus status, MarkedBy markedBy) {
        String sql = "UPDATE attendance SET status = ?, marked_by = ?, marked_at = CURRENT_TIMESTAMP WHERE student_id = ? AND session_id = ?";
>>>>>>> origin/admin-api

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, markedBy.name());
            stmt.setString(3, remarks);
            stmt.setLong(4, studentId);
            stmt.setLong(5, sessionId);

            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update attendance status", e);
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

    public AttendanceStats getStatsForStudentByDate(Long studentId, LocalDate start, LocalDate end) {

        String sql = """
    SELECT
        SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count,
        SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count,
        SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) AS excused_count,
        COUNT(*) AS total_records
    FROM attendance a
    JOIN sessions s ON a.session_id = s.id
    WHERE a.student_id = ?
      AND s.session_date BETWEEN ? AND ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setDate(2, Date.valueOf(start));
            stmt.setDate(3, Date.valueOf(end));

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

    public AttendanceStats getStatsForStudentInClass(Long studentId, Long classId) {

        String sql = """
        SELECT
            SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count,
            SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count,
            SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) AS excused_count,
            COUNT(*) AS total_records
        FROM attendance a
        JOIN sessions s ON a.session_id = s.id
        WHERE a.student_id = ?
          AND s.class_id = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setLong(2, classId);

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

    public AttendanceStats getStatsForStudentInClassByDate(Long studentId, Long classId, LocalDate start, LocalDate end) {

        String sql = """
        SELECT
            SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count,
            SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count,
            SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) AS excused_count,
            COUNT(*) AS total_records
        FROM attendance a
        JOIN sessions s ON a.session_id = s.id
        WHERE a.student_id = ?
          AND s.class_id = ?
            AND s.session_date BETWEEN ? AND ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setLong(2, classId);
            stmt.setDate(3, Date.valueOf(start));
            stmt.setDate(4, Date.valueOf(end));

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

    // exporting stats
    public List<StudentClassReportRow> getStudentYearlyReport(Long studentId, int year) {

        String sql = """
        SELECT c.name,
               SUM(CASE WHEN a.status='PRESENT' THEN 1 ELSE 0 END),
               SUM(CASE WHEN a.status='ABSENT' THEN 1 ELSE 0 END),
               SUM(CASE WHEN a.status='EXCUSED' THEN 1 ELSE 0 END),
               COUNT(*)
        FROM attendance a
        JOIN sessions s ON a.session_id = s.id
        JOIN classes c ON s.class_id = c.id
        WHERE a.student_id = ?
          AND YEAR(s.session_date) = ?
        GROUP BY c.name
    """;

        List<StudentClassReportRow> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setInt(2, year);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int present = rs.getInt(2);
                int total = rs.getInt(5);

                list.add(new StudentClassReportRow(
                        rs.getString(1),
                        present,
                        rs.getInt(3),
                        rs.getInt(4),
                        total == 0 ? 0 : (present * 100.0 / total)
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<TeacherStudentReportRow> getTeacherClassReport(Long teacherId, Long classId) {

        String sql = """
        SELECT CONCAT(u.first_name,' ',u.last_name),
               SUM(CASE WHEN a.status='PRESENT' THEN 1 ELSE 0 END),
               SUM(CASE WHEN a.status='ABSENT' THEN 1 ELSE 0 END),
               SUM(CASE WHEN a.status='EXCUSED' THEN 1 ELSE 0 END),
               COUNT(*)
        FROM attendance a
        JOIN users u ON a.student_id = u.id
        JOIN sessions s ON a.session_id = s.id
        JOIN classes c ON s.class_id = c.id
        WHERE c.teacher_id = ?
          AND c.id = ?
        GROUP BY u.id
    """;

        List<TeacherStudentReportRow> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, teacherId);
            stmt.setLong(2, classId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int present = rs.getInt(2);
                int total = rs.getInt(5);

                list.add(new TeacherStudentReportRow(
                        rs.getString(1),
                        present,
                        rs.getInt(3),
                        rs.getInt(4),
                        total == 0 ? 0 : (present * 100.0 / total)
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<AttendanceReportRow> getAllStudentsStats() {

        String sql = """
        SELECT u.id,
               u.first_name,
               u.last_name,
               SUM(CASE WHEN a.status='PRESENT' THEN 1 ELSE 0 END) present,
               SUM(CASE WHEN a.status='ABSENT' THEN 1 ELSE 0 END) absent,
               SUM(CASE WHEN a.status='EXCUSED' THEN 1 ELSE 0 END) excused,
               COUNT(*) total
        FROM users u
        LEFT JOIN attendance a ON u.id = a.student_id
        WHERE u.user_type = 'STUDENT'
        GROUP BY u.id
    """;

        List<AttendanceReportRow> list = new ArrayList<>();
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                list.add(new AttendanceReportRow(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("present"),
                        rs.getInt("absent"),
                        rs.getInt("excused"),
                        rs.getInt("total")
                ));
            }
        } catch (Exception e){ e.printStackTrace(); }

        return list;
    }

    // Sessions
    public String getSessionCode(Long sessionId) {
        String sql = "SELECT qr_token FROM sessions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("qr_token");
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get session code", e);
        }
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
<<<<<<< HEAD

    public AttendanceStats getStatsForClassByDateRange(Long classId, LocalDate start, LocalDate end) {
        String sql = """
        SELECT
            SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count,
            SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count,
            SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) AS excused_count,
            COUNT(*) AS total_records
        FROM attendance a
        JOIN sessions s ON a.session_id = s.id
        WHERE s.class_id = ?
          AND s.session_date BETWEEN ? AND ?
        """;
=======
    public dto.AttendanceStats getStudentStats(Long studentId) {
        String sql = """
        SELECT
            COALESCE(SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END), 0) AS presentCount,
            COALESCE(SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END), 0) AS absentCount,
            COALESCE(SUM(CASE WHEN status = 'EXCUSED' THEN 1 ELSE 0 END), 0) AS excusedCount,
            COUNT(*) AS totalDays
        FROM attendance
        WHERE student_id = ?
    """;
>>>>>>> origin/admin-api

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

<<<<<<< HEAD
            stmt.setLong(1, classId);
            stmt.setDate(2, Date.valueOf(start));
            stmt.setDate(3, Date.valueOf(end));
=======
            stmt.setLong(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int present = rs.getInt("presentCount");
                    int absent = rs.getInt("absentCount");
                    int excused = rs.getInt("excusedCount");
                    int total = rs.getInt("totalDays");

                    return new dto.AttendanceStats(present, absent, excused, total);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to get student attendance stats", e);
        }

        return new dto.AttendanceStats(0, 0, 0, 0);
    }
    public Long findSessionIdByCode(String code) {
        String sql = "SELECT id FROM sessions WHERE qr_token = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("id");
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to find session by code", e);
        }
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
    public List<Map<String, Object>> reportByClass(long classId) {
        String sql = """
        SELECT
          s.id AS session_id,
          s.session_date,
          s.qr_token,
          SUM(a.status = 'PRESENT') AS present,
          SUM(a.status = 'ABSENT')  AS absent,
          SUM(a.status = 'EXCUSED') AS excused,
          COUNT(a.student_id)       AS total_marked
        FROM sessions s
        LEFT JOIN attendance a ON a.session_id = s.id
        WHERE s.class_id = ?
        GROUP BY s.id, s.session_date, s.qr_token
        ORDER BY s.session_date DESC
    """;

        List<Map<String, Object>> out = new ArrayList<>();

        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, classId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(Map.of(
                            "sessionId", rs.getLong("session_id"),
                            "date", rs.getDate("session_date").toLocalDate().toString(),
                            "code", rs.getString("qr_token"),
                            "present", rs.getInt("present"),
                            "absent", rs.getInt("absent"),
                            "excused", rs.getInt("excused"),
                            "totalMarked", rs.getInt("total_marked")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }
    public java.util.Map<String, Object> getSessionReport(long sessionId) {

        String statsSql = """
        SELECT
            SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS presentCount,
            SUM(CASE WHEN status = 'ABSENT'  THEN 1 ELSE 0 END) AS absentCount,
            SUM(CASE WHEN status = 'EXCUSED' THEN 1 ELSE 0 END) AS excusedCount,
            COUNT(*) AS totalRecords
        FROM attendance
        WHERE session_id = ?
    """;

        String rowsSql = """
        SELECT
            u.id AS studentId,
            u.first_name,
            u.last_name,
            u.email,
            a.status
        FROM attendance a
        JOIN users u ON u.id = a.student_id
        WHERE a.session_id = ?
        ORDER BY u.last_name, u.first_name
    """;

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();

        int present = 0, absent = 0, excused = 0, total = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(statsSql)) {
                ps.setLong(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        present = rs.getInt("presentCount");
                        absent  = rs.getInt("absentCount");
                        excused = rs.getInt("excusedCount");
                        total   = rs.getInt("totalRecords");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(rowsSql)) {
                ps.setLong(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(java.util.Map.of(
                                "studentId", rs.getLong("studentId"),
                                "firstName", rs.getString("first_name"),
                                "lastName", rs.getString("last_name"),
                                "email", rs.getString("email"),
                                "status", rs.getString("status")
                        ));
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("getSessionReport failed: " + e.getMessage(), e);
        }

        double rate = (total == 0) ? 0.0 : (present * 100.0) / total;

        result.put("stats", java.util.Map.of(
                "present", present,
                "absent", absent,
                "excused", excused,
                "total", total,
                "rate", rate
        ));
        result.put("rows", rows);

        return result;
    }
    public int countTodayForTeacher(long teacherId, String status) {

        String sql = """
        SELECT COUNT(*)
        FROM attendance a
        JOIN sessions s ON s.id = a.session_id
        JOIN classes  c ON c.id = s.class_id
        WHERE c.teacher_id = ?
          AND s.session_date = CURRENT_DATE
          AND a.status = ?
    """;

        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, teacherId);
            ps.setString(2, status);

            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    private String buildPeriodCondition(String period) {
        if (period == null || period.equalsIgnoreCase("ALL")) {
            return "";
        }

        return switch (period) {
            case "THIS_MONTH" -> " AND YEAR(s.session_date) = YEAR(CURDATE()) AND MONTH(s.session_date) = MONTH(CURDATE()) ";
            case "LAST_MONTH" -> " AND YEAR(s.session_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) AND MONTH(s.session_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) ";
            case "THIS_YEAR" -> " AND YEAR(s.session_date) = YEAR(CURDATE()) ";
            default -> "";
        };
    }
    public dto.AttendanceStats getStudentStats(Long studentId, Long classId, String period) {
        StringBuilder sql = new StringBuilder("""
        SELECT
            SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count,
            SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count,
            SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) AS excused_count,
            COUNT(*) AS total_days
        FROM attendance a
        JOIN sessions s ON a.session_id = s.id
        WHERE a.student_id = ?
        """);

        if (classId != null) {
            sql.append(" AND s.class_id = ? ");
        }

        sql.append(buildPeriodCondition(period));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int i = 1;
            stmt.setLong(i++, studentId);
            if (classId != null) {
                stmt.setLong(i++, classId);
            }
>>>>>>> origin/admin-api

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int present = rs.getInt("present_count");
                int absent = rs.getInt("absent_count");
                int excused = rs.getInt("excused_count");
<<<<<<< HEAD
                int total = rs.getInt("total_records");

                return new AttendanceStats(present, absent, excused, total);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new AttendanceStats(0, 0, 0, 0);
    }
}
=======
                int total = rs.getInt("total_days");
                double rate = total == 0 ? 0 : (present * 100.0 / total);

                return new dto.AttendanceStats(present, absent, excused, total);
            }

            return new dto.AttendanceStats(0, 0, 0, 0);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get filtered student stats", e);
        }
    }
    public List<dto.AttendanceView> getStudentAttendanceViews(Long studentId, Long classId, String period) {
        StringBuilder sql = new StringBuilder("""
        SELECT
            s.session_date,
            a.status
        FROM attendance a
        JOIN sessions s ON a.session_id = s.id
        WHERE a.student_id = ?
        """);

        if (classId != null) {
            sql.append(" AND s.class_id = ? ");
        }

        sql.append(buildPeriodCondition(period));
        sql.append(" ORDER BY s.session_date DESC ");

        List<dto.AttendanceView> list = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int i = 1;
            stmt.setLong(i++, studentId);
            if (classId != null) {
                stmt.setLong(i++, classId);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dto.AttendanceView v = new dto.AttendanceView();
                v.setSessionDate(rs.getDate("session_date").toLocalDate());
                v.setStatus(rs.getString("status"));
                list.add(v);
            }

            return list;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get filtered attendance records", e);
        }
    }
    public List<dto.AttendanceView> getAdminAttendanceReport(Long classId, String period, String searchTerm) {
        List<dto.AttendanceView> results = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
        SELECT
            u.id AS student_id,
            u.first_name,
            u.last_name,
            se.session_date,
            a.status
        FROM attendance a
        JOIN sessions se ON a.session_id = se.id
        JOIN users u ON a.student_id = u.id
        WHERE se.class_id = ?
        """);

        sql.append(buildPeriodConditionForSessionAlias(period, "se"));

        sql.append("""
          AND (
                CAST(u.id AS CHAR) LIKE ?
             OR LOWER(u.first_name) LIKE ?
             OR LOWER(u.last_name) LIKE ?
             OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE ?
          )
        ORDER BY u.last_name, u.first_name, se.session_date DESC
        """);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            String safeSearch = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
            String likeText = "%" + safeSearch + "%";

            stmt.setLong(1, classId);
            stmt.setString(2, "%" + safeSearch + "%");
            stmt.setString(3, likeText);
            stmt.setString(4, likeText);
            stmt.setString(5, likeText);

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
            throw new RuntimeException("Failed to get admin attendance report", e);
        }

        return results;
    }
    private String buildPeriodConditionForSessionAlias(String period, String alias) {
        if (period == null || period.equalsIgnoreCase("ALL")) {
            return "";
        }

        return switch (period) {
            case "THIS_MONTH" ->
                    " AND YEAR(" + alias + ".session_date) = YEAR(CURDATE()) AND MONTH(" + alias + ".session_date) = MONTH(CURDATE()) ";
            case "LAST_MONTH" ->
                    " AND YEAR(" + alias + ".session_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) " +
                            " AND MONTH(" + alias + ".session_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) ";
            case "THIS_YEAR" ->
                    " AND YEAR(" + alias + ".session_date) = YEAR(CURDATE()) ";
            default -> "";
        };
    }

}
>>>>>>> origin/admin-api
