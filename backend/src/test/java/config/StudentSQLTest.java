package config;

import model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentSQLTest {

    private StudentSQL studentSQL;

    @BeforeEach
    void setup() {
        studentSQL = new StudentSQL();
    }

    // ========================
    // findById()
    // ========================

    @Test
    void findById_returnsStudent_whenFound() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);

            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getString("first_name")).thenReturn("John");
            when(rs.getString("last_name")).thenReturn("Doe");
            when(rs.getString("email")).thenReturn("john@test.com");
            when(rs.getLong("student_code")).thenReturn(12345L);

            Student result = studentSQL.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getStudentId());
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

            Student result = studentSQL.findById(999L);

            assertNull(result);
        }
    }

    @Test
    void findById_returnsNull_onSQLException() {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            Student result = studentSQL.findById(1L);

            assertNull(result);
        }
    }

    // ========================
    // findByClassId()
    // ========================

    @Test
    void findByClassId_returnsStudents() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, true, false);

            when(rs.getLong("id")).thenReturn(1L, 2L);
            when(rs.getString("first_name")).thenReturn("A", "B");
            when(rs.getString("last_name")).thenReturn("One", "Two");
            when(rs.getString("email")).thenReturn("a@test.com", "b@test.com");
            when(rs.getLong("student_code")).thenReturn(111L, 222L);

            List<Student> list = studentSQL.findByClassId(10L);

            assertEquals(2, list.size());
        }
    }

    @Test
    void findByClassId_returnsEmpty_whenNoResults() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(false);

            List<Student> list = studentSQL.findByClassId(10L);

            assertTrue(list.isEmpty());
        }
    }

    @Test
    void findByClassId_returnsEmpty_onSQLException() {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            List<Student> list = studentSQL.findByClassId(10L);

            assertTrue(list.isEmpty());
        }
    }
}