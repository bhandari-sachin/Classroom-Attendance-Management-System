package config;

import dto.AttendanceReportRow;
import dto.AttendanceStats;
import dto.AttendanceView;
import model.Attendance;
import model.AttendanceStatus;
import model.MarkedBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceSQLTest {

    private AttendanceSQL sql;

    @BeforeEach
    void setup() {
        sql = new AttendanceSQL();
    }

    // =========================
    // updateStatus()
    // =========================
    @Test
    void updateStatus_executesUpdate() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            sql.updateStatus(1L, 2L, AttendanceStatus.ABSENT, MarkedBy.TEACHER);

            verify(ps).setString(1, "ABSENT");
            verify(ps).setString(2, "TEACHER");
            verify(ps).setLong(3, 1L);
            verify(ps).setLong(4, 2L);
            verify(ps).executeUpdate();
        }
    }

    @Test
    void updateStatus_throws_whenDBFails() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sql.updateStatus(1L, 1L, AttendanceStatus.PRESENT, MarkedBy.QR));
        }
    }

    // =========================
    // findByClassId()
    // =========================
    @Test
    void findByClassId_returnsList() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("student_id")).thenReturn(1L);
            when(rs.getLong("session_id")).thenReturn(2L);
            when(rs.getString("status")).thenReturn("PRESENT");
            when(rs.getString("marked_by")).thenReturn("QR");

            List<Attendance> list = sql.findByClassId(1L);

            assertEquals(1, list.size());
        }
    }

    // =========================
    // filterAttendanceByStudent()
    // =========================
    @Test
    void filterAttendance_returnsResults() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("student_id")).thenReturn(1L);
            when(rs.getString("first_name")).thenReturn("John");
            when(rs.getString("last_name")).thenReturn("Doe");
            when(rs.getDate("session_date")).thenReturn(Date.valueOf(LocalDate.now()));
            when(rs.getString("status")).thenReturn("PRESENT");

            List<AttendanceView> result =
                    sql.filterAttendanceByStudent(1L, "john", "en");

            assertEquals(1, result.size());
        }
    }

    @Test
    void filterAttendance_returnsEmpty_onSQLException() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            List<AttendanceView> result =
                    sql.filterAttendanceByStudent(1L, "x", "en");

            assertTrue(result.isEmpty());
        }
    }

    // =========================
    // getSessionCode()
    // =========================
    @Test
    void getSessionCode_returnsCode() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getString("qr_token")).thenReturn("ABC");

            assertEquals("ABC", sql.getSessionCode(1L));
        }
    }

    @Test
    void getSessionCode_returnsNull_whenNotFound() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(false);

            assertNull(sql.getSessionCode(1L));
        }
    }

    @Test
    void getSessionCode_throws_onSQLException() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sql.getSessionCode(1L));
        }
    }

    // =========================
    // findSessionIdByCode()
    // =========================
    @Test
    void findSessionIdByCode_returnsId() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getLong("id")).thenReturn(99L);

            assertEquals(99L, sql.findSessionIdByCode("ABC"));
        }
    }

    @Test
    void findSessionIdByCode_returnsNull_whenMissing() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(false);

            assertNull(sql.findSessionIdByCode("NONE"));
        }
    }

    @Test
    void findSessionIdByCode_throws_onSQLException() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sql.findSessionIdByCode("X"));
        }
    }

    // =========================
    // reportByClass()
    // =========================
    @Test
    void reportByClass_returnsMappedData() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("session_id")).thenReturn(1L);
            when(rs.getDate("session_date")).thenReturn(Date.valueOf(LocalDate.now()));
            when(rs.getString("qr_token")).thenReturn("CODE");
            when(rs.getInt("present")).thenReturn(5);
            when(rs.getInt("absent")).thenReturn(2);
            when(rs.getInt("excused")).thenReturn(1);
            when(rs.getInt("total_marked")).thenReturn(8);

            List<Map<String, Object>> result = sql.reportByClass(1L);

            assertEquals(1, result.size());
            assertEquals(5, result.get(0).get("present"));
        }
    }

    // =========================
    // countTodayForTeacher()
    // =========================
    @Test
    void countTodayForTeacher_returnsCount() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(7);

            assertEquals(7, sql.countTodayForTeacher(1L, "PRESENT"));
        }
    }

    @Test
    void countTodayForTeacher_returnsZero_onError() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertEquals(0, sql.countTodayForTeacher(1L, "PRESENT"));
        }
    }

    // =========================
    // getSessionReport()
    // =========================
    @Test
    void getSessionReport_returnsStatsAndRows() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement statsPs = mock(PreparedStatement.class);
            PreparedStatement rowsPs = mock(PreparedStatement.class);

            ResultSet statsRs = mock(ResultSet.class);
            ResultSet rowsRs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString()))
                    .thenReturn(statsPs)
                    .thenReturn(rowsPs);

            when(statsPs.executeQuery()).thenReturn(statsRs);
            when(rowsPs.executeQuery()).thenReturn(rowsRs);

            when(statsRs.next()).thenReturn(true);
            when(statsRs.getInt("presentCount")).thenReturn(3);
            when(statsRs.getInt("absentCount")).thenReturn(1);
            when(statsRs.getInt("excusedCount")).thenReturn(1);
            when(statsRs.getInt("totalRecords")).thenReturn(5);

            when(rowsRs.next()).thenReturn(false);

            Map<String, Object> result =
                    sql.getSessionReport(1L, "en");

            assertNotNull(result.get("stats"));
            assertNotNull(result.get("rows"));
        }
    }

    @Test
    void getSessionReport_throws_onSQLException() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sql.getSessionReport(1L, "en"));
        }
    }

    // ========================
    // exists()
    // ========================

    @Test
    void exists_true() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(1);

            assertTrue(sql.exists(1L, 2L));
        }
    }

    @Test
    void exists_false() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(0);

            assertFalse(sql.exists(1L, 2L));
        }
    }

    @Test
    void exists_throws_onError() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sql.exists(1L, 2L));
        }
    }

    // ========================
    // save()
    // ========================

    @Test
    void save_executesInsert() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Attendance a = mock(Attendance.class);
            when(a.getStudentId()).thenReturn(1L);
            when(a.getSessionId()).thenReturn(2L);
            when(a.getStatus()).thenReturn(AttendanceStatus.PRESENT);
            when(a.getMarkedBy()).thenReturn(MarkedBy.TEACHER);

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            sql.save(a);

            verify(ps).executeUpdate();
        }
    }

    @Test
    void save_throws_onError() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sql.save(mock(Attendance.class)));
        }
    }

    // ========================
    // findByStudentId()
    // ========================

    @Test
    void findByStudentId_returnsList() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("student_id")).thenReturn(1L);
            when(rs.getLong("session_id")).thenReturn(2L);
            when(rs.getString("status")).thenReturn("PRESENT");
            when(rs.getString("marked_by")).thenReturn("TEACHER");

            List<Attendance> result = sql.findByStudentId(1L);

            assertEquals(1, result.size());
        }
    }

    // ========================
    // getOverallStats()
    // ========================

    @Test
    void getOverallStats_returnsData() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt("presentCount")).thenReturn(5);
            when(rs.getInt("absentCount")).thenReturn(2);
            when(rs.getInt("excusedCount")).thenReturn(1);
            when(rs.getInt("totalRecords")).thenReturn(8);

            AttendanceStats stats = sql.getOverallStats();

            assertEquals(5, stats.getPresentCount());
        }
    }

    @Test
    void getOverallStats_returnsDefault_onError() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            AttendanceStats stats = sql.getOverallStats();

            assertEquals(0, stats.getTotalDays());
        }
    }

    // ========================
    // getStudentStats()
    // ========================

    @Test
    void getStudentStats_returnsData() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(anyString())).thenReturn(3);

            AttendanceStats stats = sql.getStudentStats(1L);

            assertEquals(3, stats.getPresentCount());
        }
    }

    @Test
    void getStudentStats_throws_onError() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sql.getStudentStats(1L));
        }
    }

    // ========================
    // getStudentAttendanceViews()
    // ========================

    @Test
    void getStudentAttendanceViews_returnsList() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong(anyString())).thenReturn(1L);
            when(rs.getString(anyString())).thenReturn("John");
            when(rs.getDate(anyString())).thenReturn(Date.valueOf(LocalDate.now()));

            List<AttendanceView> list = sql.getStudentAttendanceViews(1L, "en");

            assertEquals(1, list.size());
        }
    }

    @Test
    void getStudentAttendanceViews_filtered_throws_onSQLException() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sql.getStudentAttendanceViews(1L, 2L, "MONTH", "en"));
        }
    }

    // ========================
    // FILTERED (covers bindPeriodParameters)
    // ========================

    @Test
    void getStudentStats_withFilters() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(anyString())).thenReturn(1);

            AttendanceStats stats = sql.getStudentStats(1L, 2L, "MONTH");

            assertEquals(1, stats.getPresentCount());
        }
    }

    @Test
    void getStudentAttendanceViews_filtered() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getDate(anyString())).thenReturn(Date.valueOf(LocalDate.now()));
            when(rs.getString(anyString())).thenReturn("PRESENT");

            List<AttendanceView> list =
                    sql.getStudentAttendanceViews(1L, 2L, "MONTH", "en");

            assertEquals(1, list.size());
        }
    }

    @Test
    void getStudentStats_filtered_throws_onSQLException() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sql.getStudentStats(1L, 2L, "MONTH"));
        }
    }

    // ========================
    // REPORTS
    // ========================

    @Test
    void getAllStudentsStats_returnsList() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getString(anyString())).thenReturn("John");
            when(rs.getInt(anyString())).thenReturn(2);

            List<AttendanceReportRow> list = sql.getAllStudentsStats();

            assertEquals(1, list.size());
        }
    }

    @Test
    void getStudentYearlyReport_returnsList() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getString(1)).thenReturn("Math");
            when(rs.getString(2)).thenReturn("S1");
            when(rs.getInt(3)).thenReturn(2);
            when(rs.getInt(4)).thenReturn(1);
            when(rs.getInt(5)).thenReturn(1);
            when(rs.getInt(6)).thenReturn(4);

            var list = sql.getStudentYearlyReport(1L, 2024);

            assertEquals(1, list.size());
        }
    }

    @Test
    void getTeacherClassReport_returnsList() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getString(1)).thenReturn("Math");
            when(rs.getString(2)).thenReturn("Teacher A");
            when(rs.getString(3)).thenReturn("Student B");
            when(rs.getInt(4)).thenReturn(3);
            when(rs.getInt(5)).thenReturn(1);
            when(rs.getInt(6)).thenReturn(0);
            when(rs.getInt(7)).thenReturn(4);

            var list = sql.getTeacherClassReport(1L, 2L, 2024);

            assertEquals(1, list.size());
        }
    }

    @Test
    void getAdminAttendanceReport_throws_onSQLException() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sql.getAdminAttendanceReport(1L, "MONTH", "john", "en"));
        }
    }
}