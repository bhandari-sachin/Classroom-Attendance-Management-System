package config;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalizationSQLTest {

    @Test
    void getLabels_shouldReturnMap() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("translation_key")).thenReturn("key1", "key2");
        when(rs.getString("value")).thenReturn("val1", "val2");

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

            Map<String, String> result = LocalizationSQL.getLabels("en");

            assertEquals(2, result.size());
            assertEquals("val1", result.get("key1"));
            assertEquals("val2", result.get("key2"));
        }
    }

    @Test
    void getLabels_shouldHandleException() throws Exception {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException("fail"));

            Map<String, String> result = LocalizationSQL.getLabels("en");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getActiveLanguages_shouldReturnList() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, false);
        when(rs.getString("code")).thenReturn("en");
        when(rs.getString("name")).thenReturn("English");
        when(rs.getBoolean("is_default")).thenReturn(true);
        when(rs.getBoolean("is_active")).thenReturn(true);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

            LocalizationSQL sql = new LocalizationSQL();
            List<LocalizationSQL.LanguageItem> result = sql.getActiveLanguages();

            assertEquals(1, result.size());
            assertEquals("en", result.get(0).code());
        }
    }

    @Test
    void getUserTypeLabel_shouldReturnLabel() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getString("label")).thenReturn("Admin");

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

            LocalizationSQL sql = new LocalizationSQL();
            String result = sql.getUserTypeLabel("ADMIN", "en");

            assertEquals("Admin", result);
        }
    }

    @Test
    void getUserTypeLabel_shouldFallbackToCode() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(false);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

            LocalizationSQL sql = new LocalizationSQL();
            String result = sql.getUserTypeLabel("ADMIN", "en");

            assertEquals("ADMIN", result);
        }
    }

    @Test
    void getAttendanceStatusLabel_shouldReturnLabel() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getString("label")).thenReturn("Present");

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

            LocalizationSQL sql = new LocalizationSQL();
            String result = sql.getAttendanceStatusLabel("PRESENT", "en");

            assertEquals("Present", result);
        }
    }

    @Test
    void getAttendanceStatusLabel_shouldFallbackToCode() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(false);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

            LocalizationSQL sql = new LocalizationSQL();
            String result = sql.getAttendanceStatusLabel("ABSENT", "en");

            assertEquals("ABSENT", result);
        }
    }
}