package config;

import dto.AttendanceReportRow;
import dto.StudentClassReportRow;
import dto.TeacherStudentReportRow;
import model.Attendance;
import model.AttendanceStatus;
import dto.AttendanceView;
import model.MarkedBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttendanceSQL {

    private static final Logger LOGGER = Logger.getLogger(AttendanceSQL.class.getName());
    private static final String COL_STUDENT_ID = "student_id";
    private static final String COL_SESSION_ID = "session_id";
    private static final String COL_STATUS = "status";
    private static final String COL_MARKED_BY = "marked_by";
    private static final String COL_FIRST_NAME = "first_name";
    private static final String COL_LAST_NAME = "last_name";
    private static final String COL_SESSION_DATE = "session_date";
    private static final String COL_QR_TOKEN = "qr_token";
    private static final String COL_PRESENT_COUNT = "presentCount";
    private static final String COL_ABSENT_COUNT = "absentCount";
    private static final String COL_EXCUSED_COUNT = "excusedCount";
    private static final String COL_TOTAL_RECORDS = "totalRecords";
    private static final String COL_PRESENT_COUNT_ALT = "present_count";
    private static final String COL_ABSENT_COUNT_ALT = "absent_count";
    private static final String COL_EXCUSED_COUNT_ALT = "excused_count";
    private static final String COL_TOTAL_DAYS = "total_days";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PRESENT = "present";
    private static final String KEY_ABSENT = "absent";
    private static final String KEY_EXCUSED = "excused";
    private static final String KEY_TOTAL_MARKED = "totalMarked";


    private static final String PERIOD_FILTER_SESSION_DATE =
            "AND (? IS NULL OR ? = 'ALL' "
                    + "OR (? = 'THIS_MONTH' AND YEAR(s.session_date) = YEAR(CURDATE()) AND MONTH(s.session_date) = MONTH(CURDATE())) "
                    + "OR (? = 'LAST_MONTH' AND YEAR(s.session_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) AND MONTH(s.session_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))) "
                    + "OR (? = 'THIS_YEAR' AND YEAR(s.session_date) = YEAR(CURDATE())) "
                    + "OR (? NOT IN ('ALL', 'THIS_MONTH', 'LAST_MONTH'))) ";

    private static final String PERIOD_FILTER_SE_SESSION_DATE =
            "AND (? IS NULL OR ? = 'ALL' "
                    + "OR (? = 'THIS_MONTH' AND YEAR(se.session_date) = YEAR(CURDATE()) AND MONTH(se.session_date) = MONTH(CURDATE())) "
                    + "OR (? = 'LAST_MONTH' AND YEAR(se.session_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) AND MONTH(se.session_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))) "
                    + "OR (? = 'THIS_YEAR' AND YEAR(se.session_date) = YEAR(CURDATE())) "
                    + "OR (? NOT IN ('ALL', 'THIS_MONTH', 'LAST_MONTH'))) ";

    // getStudentStats — without classId filter
    private static final String SQL_STUDENT_STATS =
            "SELECT "
                    + "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count, "
                    + "SUM(CASE WHEN a.status = 'ABSENT'  THEN 1 ELSE 0 END) AS absent_count, "
                    + "SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) AS excused_count, "
                    + "COUNT(*) AS total_days "
                    + "FROM attendance a "
                    + "JOIN sessions s ON a.session_id = s.id "
                    + "WHERE a.student_id = ? "
                    + PERIOD_FILTER_SESSION_DATE;

    // getStudentStats — with classId filter
    private static final String SQL_STUDENT_STATS_BY_CLASS =
            "SELECT "
                    + "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count, "
                    + "SUM(CASE WHEN a.status = 'ABSENT'  THEN 1 ELSE 0 END) AS absent_count, "
                    + "SUM(CASE WHEN a.status = 'EXCUSED' THEN 1 ELSE 0 END) AS excused_count, "
                    + "COUNT(*) AS total_days "
                    + "FROM attendance a "
                    + "JOIN sessions s ON a.session_id = s.id "
                    + "WHERE a.student_id = ? AND s.class_id = ? "
                    + PERIOD_FILTER_SESSION_DATE;

    // getStudentAttendanceViews — without classId filter
    private static final String SQL_STUDENT_ATTENDANCE_VIEWS =
            "SELECT s.session_date, ast.label AS status "
                    + "FROM attendance a "
                    + "JOIN sessions s ON a.session_id = s.id "
                    + "JOIN attendance_status_translation ast ON a.status = ast.status_code "
                    + "WHERE a.student_id = ? AND ast.language_code = ? "
                    + PERIOD_FILTER_SESSION_DATE
                    + "ORDER BY s.session_date DESC";

    // getStudentAttendanceViews — with classId filter
    private static final String SQL_STUDENT_ATTENDANCE_VIEWS_BY_CLASS =
            "SELECT s.session_date, ast.label AS status "
                    + "FROM attendance a "
                    + "JOIN sessions s ON a.session_id = s.id "
                    + "JOIN attendance_status_translation ast ON a.status = ast.status_code "
                    + "WHERE a.student_id = ? AND ast.language_code = ? AND s.class_id = ? "
                    + PERIOD_FILTER_SESSION_DATE
                    + "ORDER BY s.session_date DESC";

    // getAdminAttendanceReport
    private static final String SQL_ADMIN_ATTENDANCE_REPORT =
            "SELECT u.id AS student_id, u.first_name, u.last_name, se.session_date, ast.label AS status "
                    + "FROM attendance a "
                    + "JOIN sessions se ON a.session_id = se.id "
                    + "JOIN users u ON a.student_id = u.id "
                    + "JOIN attendance_status_translation ast ON a.status = ast.status_code "
                    + "WHERE se.class_id = ? AND ast.language_code = ? "
                    + PERIOD_FILTER_SE_SESSION_DATE
                    + "AND (CAST(u.id AS CHAR) LIKE ? OR LOWER(u.first_name) LIKE ? "
                    + "OR LOWER(u.last_name) LIKE ? OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE ?) "
                    + "ORDER BY u.last_name, u.first_name, se.session_date DESC";

    // filterAttendanceByStudent
    private static final String SQL_FILTER_ATTENDANCE_BY_STUDENT =
            "SELECT u.id AS student_id, u.first_name, u.last_name, se.session_date, ast.label AS status "
                    + "FROM attendance a "
                    + "JOIN sessions se ON a.session_id = se.id "
                    + "JOIN users u ON a.student_id = u.id "
                    + "JOIN attendance_status_translation ast ON a.status = ast.status_code "
                    + "WHERE se.class_id = ? AND ast.language_code = ? "
                    + "AND (CAST(u.id AS CHAR) LIKE ? OR LOWER(u.first_name) LIKE ? "
                    + "OR LOWER(u.last_name) LIKE ? OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE ?) "
                    + "ORDER BY u.last_name, u.first_name, se.session_date";

    private static void bindPeriodParameters(PreparedStatement stmt, int startIndex, String period) throws SQLException {
        for (int i = 0; i < 6; i++) {
            stmt.setString(startIndex + i, period);
        }
    }

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

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check attendance", e);
        }
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

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save attendance", e);
        }
    }

    public void updateStatus(Long studentId, Long sessionId, AttendanceStatus status, MarkedBy markedBy) {
        String sql = "UPDATE attendance SET status = ?, marked_by = ?, marked_at = CURRENT_TIMESTAMP WHERE student_id = ? AND session_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, markedBy.name());
            stmt.setLong(3, studentId);
            stmt.setLong(4, sessionId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update attendance status", e);
        }
    }


    // Fetch attendance records for a student
    public List<Attendance> findByStudentId(Long studentId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT student_id, session_id, status, marked_by FROM attendance WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance(
                        rs.getLong(COL_STUDENT_ID),
                        rs.getLong(COL_SESSION_ID),
                        AttendanceStatus.valueOf(rs.getString(COL_STATUS)),
                        MarkedBy.valueOf(rs.getString(COL_MARKED_BY))
                );
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load attendance by student", e);
        }
        return attendanceList;
    }

    // Fetch attendance records for a class
    public List<Attendance> findByClassId(Long classId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT a.student_id, a.session_id, a.status, a.marked_by FROM attendance a JOIN sessions s ON a.session_id = s.id WHERE s.class_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance(
                        rs.getLong(COL_STUDENT_ID),
                        rs.getLong(COL_SESSION_ID),
                        AttendanceStatus.valueOf(rs.getString(COL_STATUS)),
                        MarkedBy.valueOf(rs.getString(COL_MARKED_BY))
                );
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load attendance by class", e);
        }
        return attendanceList;
    }

    // Filter attendance records by student details
    public List<AttendanceView> filterAttendanceByStudent(
            Long classId,
            String searchTerm,
            String languageCode
    ) {

        List<AttendanceView> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FILTER_ATTENDANCE_BY_STUDENT)) {

            String term = "%" + searchTerm.toLowerCase() + "%";

            stmt.setLong(1, classId);
            stmt.setString(2, languageCode);
            stmt.setString(3, "%" + searchTerm + "%"); // student ID
            stmt.setString(4, term);                   // first name
            stmt.setString(5, term);                   // last name
            stmt.setString(6, term);                   // full name

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(new AttendanceView(
                        rs.getLong(COL_STUDENT_ID),
                        rs.getString(COL_FIRST_NAME),
                        rs.getString(COL_LAST_NAME),
                        rs.getDate(COL_SESSION_DATE).toLocalDate(),
                        rs.getString(COL_STATUS)
                ));
            }

        } catch (SQLException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Failed to filter attendance by student", e);
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
                return rs.getString(COL_QR_TOKEN);
            }
            return null;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get session code", e);
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
                int present = rs.getInt(COL_PRESENT_COUNT);
                int absent = rs.getInt(COL_ABSENT_COUNT);
                int excused = rs.getInt(COL_EXCUSED_COUNT);
                int total = rs.getInt(COL_TOTAL_RECORDS);
                return new dto.AttendanceStats(present, absent, excused, total);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load overall attendance stats", e);
        }
        return new dto.AttendanceStats(0,0,0,0);
    }
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int present = rs.getInt(COL_PRESENT_COUNT_ALT);
                    int absent = rs.getInt(COL_ABSENT_COUNT_ALT);
                    int excused = rs.getInt(COL_EXCUSED_COUNT_ALT);
                    int total = rs.getInt(COL_TOTAL_DAYS);

                    return new dto.AttendanceStats(present, absent, excused, total);
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get student attendance stats", e);
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

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find session by code", e);
        }
    }

    public List<dto.AttendanceView> getStudentAttendanceViews(Long studentId, String languageCode) {
        List<dto.AttendanceView> results = new ArrayList<>();

        String sql = """
        SELECT
            u.id AS student_id,
            u.first_name,
            u.last_name,
            se.session_date,
            ast.label AS status
        FROM attendance a
        JOIN sessions se ON a.session_id = se.id
        JOIN users u ON a.student_id = u.id
        JOIN attendance_status_translation ast ON a.status = ast.status_code
        WHERE a.student_id = ?
        AND ast.language_code = ?
        ORDER BY se.session_date DESC
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setString(2, languageCode);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new dto.AttendanceView(
                            rs.getLong(COL_STUDENT_ID),
                            rs.getString(COL_FIRST_NAME),
                            rs.getString(COL_LAST_NAME),
                            rs.getDate(COL_SESSION_DATE).toLocalDate(),
                            rs.getString(COL_STATUS)
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load attendance views for student", e);
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
                            "sessionId", rs.getLong(COL_SESSION_ID),
                            "date", rs.getDate(COL_SESSION_DATE).toLocalDate().toString(),
                            "code", rs.getString(COL_QR_TOKEN),
                            KEY_PRESENT, rs.getInt(KEY_PRESENT),
                            KEY_ABSENT, rs.getInt(KEY_ABSENT),
                            KEY_EXCUSED, rs.getInt(KEY_EXCUSED),
                            KEY_TOTAL_MARKED, rs.getInt("total_marked")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to build class attendance report", e);
        }
        return out;
    }
    public java.util.Map<String, Object> getSessionReport(long sessionId, String languageCode) {

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
            ast.label AS status
        FROM attendance a
        JOIN users u ON u.id = a.student_id
        JOIN attendance_status_translation ast ON a.status = ast.status_code
        WHERE a.session_id = ?
        AND ast.language_code = ?
        ORDER BY u.last_name, u.first_name
    """;

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();

        int present = 0;
        int absent = 0;
        int excused = 0;
        int total = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(statsSql)) {
                ps.setLong(1, sessionId);
                ps.setString(2, languageCode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        present = rs.getInt(COL_PRESENT_COUNT);
                        absent  = rs.getInt(COL_ABSENT_COUNT);
                        excused = rs.getInt(COL_EXCUSED_COUNT);
                        total   = rs.getInt(COL_TOTAL_RECORDS);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(rowsSql)) {
                ps.setLong(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(java.util.Map.of(
                                "studentId", rs.getLong("studentId"),
                                "firstName", rs.getString(COL_FIRST_NAME),
                                "lastName", rs.getString(COL_LAST_NAME),
                                "email", rs.getString("email"),
                                KEY_STATUS, rs.getString(COL_STATUS)
                        ));
                    }
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("getSessionReport failed: " + e.getMessage(), e);
        }

        double rate = (total == 0) ? 0.0 : (present * 100.0) / total;

        result.put("stats", java.util.Map.of(
                KEY_PRESENT, present,
                KEY_ABSENT, absent,
                KEY_EXCUSED, excused,
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

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to count today's attendance for teacher", e);
            return 0;
        }
    }
    public dto.AttendanceStats getStudentStats(Long studentId, Long classId, String period) {
        final String sql = classId != null ? SQL_STUDENT_STATS_BY_CLASS : SQL_STUDENT_STATS;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int parameterIndex = 1;
            stmt.setLong(parameterIndex++, studentId);
            if (classId != null) {
                stmt.setLong(parameterIndex++, classId);
            }
            bindPeriodParameters(stmt, parameterIndex, period);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int present = rs.getInt(COL_PRESENT_COUNT_ALT);
                int absent = rs.getInt(COL_ABSENT_COUNT_ALT);
                int excused = rs.getInt(COL_EXCUSED_COUNT_ALT);
                int total = rs.getInt(COL_TOTAL_DAYS);

                return new dto.AttendanceStats(present, absent, excused, total);
            }

            return new dto.AttendanceStats(0, 0, 0, 0);

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get filtered student stats", e);
        }
    }
    public List<dto.AttendanceView> getStudentAttendanceViews(Long studentId, Long classId, String period, String languageCode) {
        final String sql = classId != null ? SQL_STUDENT_ATTENDANCE_VIEWS_BY_CLASS : SQL_STUDENT_ATTENDANCE_VIEWS;

        List<dto.AttendanceView> list = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int parameterIndex = 1;
            stmt.setLong(parameterIndex++, studentId);
            stmt.setString(parameterIndex++, languageCode);
            if (classId != null) {
                stmt.setLong(parameterIndex++, classId);
            }
            bindPeriodParameters(stmt, parameterIndex, period);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dto.AttendanceView v = new dto.AttendanceView();
                v.setSessionDate(rs.getDate(COL_SESSION_DATE).toLocalDate());
                v.setStatus(rs.getString(COL_STATUS));
                list.add(v);
            }

            return list;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get filtered attendance records", e);
        }
    }
    public List<dto.AttendanceView> getAdminAttendanceReport(Long classId, String period, String searchTerm, String languageCode) {
        List<dto.AttendanceView> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ADMIN_ATTENDANCE_REPORT)) {

            String safeSearch = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
            String likeText = "%" + safeSearch + "%";

            int parameterIndex = 1;
            stmt.setLong(parameterIndex++, classId);
            stmt.setString(parameterIndex++, languageCode);
            bindPeriodParameters(stmt, parameterIndex, period);
            parameterIndex += 6;
            stmt.setString(parameterIndex++, "%" + safeSearch + "%");
            stmt.setString(parameterIndex++, likeText);
            stmt.setString(parameterIndex++, likeText);
            stmt.setString(parameterIndex, likeText);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new dto.AttendanceView(
                            rs.getLong(COL_STUDENT_ID),
                            rs.getString(COL_FIRST_NAME),
                            rs.getString(COL_LAST_NAME),
                            rs.getDate(COL_SESSION_DATE).toLocalDate(),
                            rs.getString(COL_STATUS)
                    ));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get admin attendance report", e);
        }

        return results;
    }

    // exporting stats
    @SuppressWarnings("java:S1172")
    public List<StudentClassReportRow> getStudentYearlyReport(Long studentId, int year) { // NOSONAR

        String sql = """
        SELECT c.name,
               u.student_code,
               SUM(CASE WHEN a.status='PRESENT' THEN 1 ELSE 0 END),
               SUM(CASE WHEN a.status='ABSENT' THEN 1 ELSE 0 END),
               SUM(CASE WHEN a.status='EXCUSED' THEN 1 ELSE 0 END),
               COUNT(*)
        FROM attendance a
        JOIN users u ON a.student_id = u.id
        JOIN sessions s ON a.session_id = s.id
        JOIN classes c ON s.class_id = c.id
        WHERE a.student_id = ?
          AND YEAR(s.session_date) = ?
        GROUP BY c.name, u.student_code
    """;

        List<StudentClassReportRow> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setInt(2, year);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String className = rs.getString(1);
                String studentCode = rs.getString(2);
                int present = rs.getInt(3);
                int absent = rs.getInt(4);
                int excused = rs.getInt(5);
                int total = rs.getInt(6);

                list.add(new StudentClassReportRow(
                        className,
                        studentCode,
                        present,
                        absent,
                        excused,
                        total == 0 ? 0 : (present * 100.0 / total)
                ));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load student yearly report", e);
        }

        return list;
    }

    public List<TeacherStudentReportRow> getTeacherClassReport(Long teacherId, Long classId, int year) {

        if (year < 0) {
            LOGGER.log(Level.FINE, "Ignoring negative year parameter: {0}", year);
        }

        String sql = """
        SELECT c.name,
               CONCAT(t.first_name,' ',t.last_name),
               CONCAT(u.first_name,' ',u.last_name),
               SUM(CASE WHEN a.status='PRESENT' THEN 1 ELSE 0 END),
               SUM(CASE WHEN a.status='ABSENT' THEN 1 ELSE 0 END),
               SUM(CASE WHEN a.status='EXCUSED' THEN 1 ELSE 0 END),
               COUNT(*)
        FROM attendance a
        JOIN users u ON a.student_id = u.id
        JOIN sessions s ON a.session_id = s.id
        JOIN classes c ON s.class_id = c.id
        JOIN users t ON c.teacher_id = t.id
        WHERE c.teacher_id = ?
          AND c.id = ?
        GROUP BY c.name, t.first_name, t.last_name, u.id
    """;

        List<TeacherStudentReportRow> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, teacherId);
            stmt.setLong(2, classId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String className = rs.getString(1);
                String teacherName = rs.getString(2);
                String studentName = rs.getString(3);
                int present = rs.getInt(4);
                int absent = rs.getInt(5);
                int excused = rs.getInt(6);
                int total = rs.getInt(7);

                list.add(new TeacherStudentReportRow(
                        className,
                        teacherName,
                        studentName,
                        present,
                        absent,
                        excused,
                        total == 0 ? 0 : (present * 100.0 / total)
                ));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load teacher class report", e);
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
                        rs.getString(COL_FIRST_NAME),
                        rs.getString(COL_LAST_NAME),
                        rs.getInt(KEY_PRESENT),
                        rs.getInt(KEY_ABSENT),
                        rs.getInt(KEY_EXCUSED),
                        rs.getInt("total")
                ));
            }
        } catch (SQLException e){ LOGGER.log(Level.SEVERE, "Failed to load all-students stats", e); }

        return list;
    }

}