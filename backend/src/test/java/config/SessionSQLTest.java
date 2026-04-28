package config;

import model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionSQLTest {

    private SessionSQL sessionSQL;

    @BeforeEach
    void setup() {
        sessionSQL = new SessionSQL(); // ✅ REAL object
    }

    // ========================
    // createSession()
    // ========================

    @Test
    void createSession_returnsGeneratedId() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet keys = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenReturn(ps);

            when(ps.getGeneratedKeys()).thenReturn(keys);

            when(keys.next()).thenReturn(true);
            when(keys.getLong(1)).thenReturn(123L);

            long id = sessionSQL.createSession(
                    1L,
                    LocalDate.now(),
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    "CODE123"
            );

            assertEquals(123L, id);
        }
    }

    @Test
    void createSession_throws_whenNoKeyReturned() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet keys = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
            when(ps.getGeneratedKeys()).thenReturn(keys);

            when(keys.next()).thenReturn(false);

            LocalDate date = LocalDate.now();
            LocalTime start = LocalTime.NOON;
            LocalTime end = LocalTime.MIDNIGHT;

            assertThrows(IllegalStateException.class, () ->
                    sessionSQL.createSession(
                            1L,
                            date,
                            start,
                            end,
                            "FAIL"
                    )
            );
        }
    }

    @Test
    void createSession_throws_onSQLException() {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException("DB down"));

            LocalDate date = LocalDate.now();
            LocalTime start = LocalTime.NOON;
            LocalTime end = LocalTime.MIDNIGHT;

            assertThrows(IllegalStateException.class, () ->
                    sessionSQL.createSession(
                            1L,
                            date,
                            start,
                            end,
                            "ERR"
                    )
            );
        }
    }

    // ========================
    // listForClass()
    // ========================

    @Test
    void listForClass_returnsMappedResults() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getLong("id")).thenReturn(10L);
            when(rs.getLong("class_id")).thenReturn(1L);
            when(rs.getDate("session_date")).thenReturn(Date.valueOf(LocalDate.of(2025, 1, 1)));
            when(rs.getTime("start_time")).thenReturn(Time.valueOf(LocalTime.of(9, 0)));
            when(rs.getTime("end_time")).thenReturn(Time.valueOf(LocalTime.of(10, 0)));
            when(rs.getString("qr_token")).thenReturn("ABC");

            List<Map<String, Object>> result = sessionSQL.listForClass(1L);

            assertEquals(1, result.size());
            assertEquals("ABC", result.get(0).get("code"));
        }
    }

    @Test
    void listForClass_returnsEmpty_onSQLException() {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            List<Map<String, Object>> result = sessionSQL.listForClass(1L);

            assertTrue(result.isEmpty());
        }
    }

    // ========================
    // findById()
    // ========================

    @Test
    void findById_returnsSession() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);

            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getLong("class_id")).thenReturn(2L);
            when(rs.getDate("session_date")).thenReturn(Date.valueOf("2025-01-01"));
            when(rs.getString("qr_token")).thenReturn("TOKEN");

            Session s = sessionSQL.findById(1L);

            assertNotNull(s);
            assertEquals(1L, s.getId());
        }
    }

    @Test
    void findById_returnsNull_whenNotFound() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(false);

            assertNull(sessionSQL.findById(99L));
        }
    }

    // ========================
    // updateQRCode()
    // ========================

    @Test
    void updateQRCode_executesUpdate() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            sessionSQL.updateQRCode(1L, "NEW");

            verify(ps).setString(1, "NEW");
            verify(ps).setLong(2, 1L);
            verify(ps).executeUpdate();
        }
    }

    @Test
    void updateQRCode_throws_onSQLException() {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> sessionSQL.updateQRCode(1L, "FAIL"));
        }
    }
}