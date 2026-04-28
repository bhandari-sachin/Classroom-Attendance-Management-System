package repository;

import config.DatabaseConnection;
import model.User;
import model.UserRole;
import backend.exception.DatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryTest {

    private UserRepository repo;

    @BeforeEach
    void setup() {
        repo = new UserRepository();
    }

    // ========================
    // findByEmail()
    // ========================

    @Test
    void findByEmail_returnsUser_whenExists() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);

            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getString("email")).thenReturn("test@test.com");
            when(rs.getString("password_hash")).thenReturn("hash");
            when(rs.getString("first_name")).thenReturn("A");
            when(rs.getString("last_name")).thenReturn("B");
            when(rs.getString("user_type")).thenReturn("STUDENT");
            when(rs.getString("student_code")).thenReturn("1001");

            Optional<User> result = repo.findByEmail("test@test.com");

            assertTrue(result.isPresent());
            assertEquals("test@test.com", result.get().getEmail());
        }
    }

    @Test
    void findByEmail_returnsEmpty_whenNotFound() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(false);

            Optional<User> result = repo.findByEmail("missing@test.com");

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void findByEmail_throws_onSQLException() {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(DatabaseException.class,
                    () -> repo.findByEmail("err@test.com"));
        }
    }

    @Test
    void findByEmail_shouldThrowDatabaseException_onSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            assertThrows(DatabaseException.class,
                    () -> repo.findByEmail("test@test.com"));
        }
    }

    // ========================
    // existsByEmail()
    // ========================

    @Test
    void existsByEmail_true_whenExists() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(1);

            assertTrue(repo.existsByEmail("a@test.com"));
        }
    }

    @Test
    void existsByEmail_false_whenNotExists() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(0);

            assertFalse(repo.existsByEmail("b@test.com"));
        }
    }

    @Test
    void existsByEmail_shouldThrowDatabaseException_onSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            assertThrows(DatabaseException.class,
                    () -> repo.existsByEmail("test@test.com"));
        }
    }

    // ========================
    // countByRole()
    // ========================

    @Test
    void countByRole_returnsValue() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(5);

            int count = repo.countByRole(UserRole.TEACHER);

            assertEquals(5, count);
        }
    }

    @Test
    void countByRole_shouldReturnZero() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            assertEquals(0, repo.countByRole(UserRole.STUDENT));
        }
    }

    @Test
    void countByRole_shouldThrowDatabaseException() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            assertThrows(DatabaseException.class,
                    () -> repo.countByRole(UserRole.STUDENT));
        }
    }

    // ========================
    // findAll()
    // ========================

    @Test
    void findAll_returnsUsers() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getString("email")).thenReturn("x@test.com");
            when(rs.getString("password_hash")).thenReturn("h");
            when(rs.getString("first_name")).thenReturn("A");
            when(rs.getString("last_name")).thenReturn("B");
            when(rs.getString("user_type")).thenReturn("STUDENT");
            when(rs.getString("student_code")).thenReturn("123");

            List<User> list = repo.findAll();

            assertEquals(1, list.size());
        }
    }

    @Test
    void findAll_shouldReturnEmptyList() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            assertTrue(repo.findAll().isEmpty());
        }
    }

    @Test
    void findAll_shouldMapMultipleRows() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("email")).thenReturn("a", "b");
        when(rs.getString("password_hash")).thenReturn("p", "p");
        when(rs.getString("first_name")).thenReturn("f1", "f2");
        when(rs.getString("last_name")).thenReturn("l1", "l2");
        when(rs.getString("user_type")).thenReturn("STUDENT", "STUDENT");
        when(rs.getString("student_code")).thenReturn("s1", "s2");

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            assertEquals(2, repo.findAll().size());
        }
    }

    @Test
    void findAll_shouldThrowDatabaseException_onSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            assertThrows(DatabaseException.class, repo::findAll);
        }
    }

    // ========================
    // findAllTeachers()
    // ========================

    @Test
    void findAllTeachers_formatsNamesCorrectly() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getString("first_name")).thenReturn("John");
            when(rs.getString("last_name")).thenReturn("Doe");
            when(rs.getString("email")).thenReturn("john@test.com");

            List<java.util.Map<String, String>> list = repo.findAllTeachers();

            assertEquals("John Doe", list.get(0).get("teacherName"));
        }
    }

    @Test
    void findAllTeachers_shouldHandleNullNamesAndEmail() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getString("first_name")).thenReturn(null);
        when(rs.getString("last_name")).thenReturn(null);
        when(rs.getString("email")).thenReturn(null);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            var result = repo.findAllTeachers();

            assertEquals("Teacher", result.get(0).get("teacherName"));
            assertEquals("", result.get(0).get("email"));
        }
    }

    @Test
    void findAllTeachers_shouldThrowDatabaseException_onSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            assertThrows(DatabaseException.class, repo::findAllTeachers);
        }
    }

    @Test
    void save_shouldThrowDatabaseException_onSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString(), anyInt()))
                .thenThrow(new SQLException());

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            User user = mock(User.class);
            when(user.getEmail()).thenReturn("a");
            when(user.getPasswordHash()).thenReturn("b");
            when(user.getFirstName()).thenReturn("c");
            when(user.getLastName()).thenReturn("d");
            when(user.getUserType()).thenReturn(UserRole.STUDENT);

            assertThrows(DatabaseException.class, () -> repo.save(user));
        }
    }

    @Test
    void insert_shouldThrowDatabaseException_onSQLException() throws Exception {
        Connection conn = mock(Connection.class);

        when(conn.prepareStatement(anyString(), anyInt()))
                .thenThrow(new SQLException());

        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {
            db.when(DatabaseConnection::getConnection).thenReturn(conn);

            assertThrows(DatabaseException.class, () ->
                    repo.insert("e", "h", "f", "l", "STUDENT", null)
            );
        }
    }
}