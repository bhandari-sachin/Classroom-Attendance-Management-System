package config;

import config.ClassSQL.ClassView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassSQLTest {

    private ClassSQL classSQL;

    @BeforeEach
    void setup() {
        classSQL = new ClassSQL();
    }

    // ========================
    // listAllForAdmin()
    // ========================

    @Test
    void listAllForAdmin_returnsMappedResults() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement stmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getString("class_code")).thenReturn("CLS1");
            when(rs.getString("name")).thenReturn("Math");
            when(rs.getString("teacher_email")).thenReturn("t@test.com");
            when(rs.getString("semester")).thenReturn("SPRING");
            when(rs.getString("academic_year")).thenReturn("2025");
            when(rs.getInt("students_count")).thenReturn(10);

            List<ClassView> result = classSQL.listAllForAdmin();

            assertEquals(1, result.size());
            assertEquals("Math", result.get(0).name());
        }
    }

    @Test
    void listAllForAdmin_returnsEmpty_onSQLException() {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            List<ClassView> result = classSQL.listAllForAdmin();

            assertTrue(result.isEmpty());
        }
    }

    // ========================
    // findTeacherIdByEmail()
    // ========================

    @Test
    void findTeacherIdByEmail_returnsId() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getLong("id")).thenReturn(10L);

            Long id = classSQL.findTeacherIdByEmail("t@test.com");

            assertEquals(10L, id);
        }
    }

    @Test
    void findTeacherIdByEmail_returnsNull_whenNotFound() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(false);

            assertNull(classSQL.findTeacherIdByEmail("x@test.com"));
        }
    }

    // ========================
    // createClass()
    // ========================

    @Test
    void createClass_returnsGeneratedId() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet keys = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
            when(ps.getGeneratedKeys()).thenReturn(keys);

            when(keys.next()).thenReturn(true);
            when(keys.getLong(1)).thenReturn(100L);

            long id = classSQL.createClass("C1","Math",1L,"SPRING","2025",20);

            assertEquals(100L, id);
        }
    }

    @Test
    void createClass_throws_whenNoGeneratedKey() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet keys = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
            when(ps.getGeneratedKeys()).thenReturn(keys);

            when(keys.next()).thenReturn(false);

            assertThrows(IllegalStateException.class, () ->
                    classSQL.createClass("C1","Math",1L,"SPRING","2025",20));
        }
    }

    // ========================
    // isClassOwnedByTeacher()
    // ========================

    @Test
    void isClassOwnedByTeacher_true() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(1);

            assertTrue(classSQL.isClassOwnedByTeacher(1L, 10L));
        }
    }

    @Test
    void isClassOwnedByTeacher_false_onException() {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertFalse(classSQL.isClassOwnedByTeacher(1L, 10L));
        }
    }

    // ========================
    // countForTeacher()
    // ========================

    @Test
    void countForTeacher_returnsCount() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(5);

            assertEquals(5, classSQL.countForTeacher(1L));
        }
    }

    @Test
    void countForTeacher_returnsZero_onError() {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertEquals(0, classSQL.countForTeacher(1L));
        }
    }

    @Test
    void listForTeacher_returnsData() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getString("class_code")).thenReturn("C1");
            when(rs.getString("name")).thenReturn("Math");
            when(rs.getString("semester")).thenReturn("SPRING");
            when(rs.getString("academic_year")).thenReturn("2025");
            when(rs.getInt("students_count")).thenReturn(10);

            List<Map<String, Object>> result = classSQL.listForTeacher(1L);

            assertEquals(1, result.size());
        }
    }

    @Test
    void countStudentsForTeacher_returnsValue() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(20);

            assertEquals(20, classSQL.countStudentsForTeacher(1L));
        }
    }

    // ========================
    // enrollmentExists()
    // ========================

    @Test
    void enrollmentExists_true_whenExists() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(1);

            assertTrue(classSQL.enrollmentExists(1L, 2L));
        }
    }

    // ========================
    // enrollStudent()
    // ========================

    @Test
    void enrollStudent_executesInsert() throws Exception {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            classSQL.enrollStudent(1L, 2L);

            verify(ps).executeUpdate();
        }
    }

    @Test
    void enrollStudentsByEmails_success() {

        ClassSQL spy = spy(classSQL);

        doReturn(1L).when(spy).findClassIdByCode("C1");
        doReturn(2L).when(spy).findStudentIdByEmail("a@test.com");
        doReturn(false).when(spy).enrollmentExists(1L, 2L);
        doNothing().when(spy).enrollStudent(1L, 2L);

        spy.enrollStudentsByEmails("C1", List.of("a@test.com"));

        verify(spy).enrollStudent(1L, 2L);
    }

    @Test
    void enrollStudentsByEmails_throws_whenClassMissing() {

        ClassSQL spy = spy(classSQL);
        doReturn(null).when(spy).findClassIdByCode("C1");

        List<String> emails = List.of("a@test.com");
        assertThrows(IllegalArgumentException.class,
                () -> spy.enrollStudentsByEmails("C1", emails));
    }

    @Test
    void enrollStudentsByEmails_throws_whenStudentMissing() {

        ClassSQL spy = spy(classSQL);

        doReturn(1L).when(spy).findClassIdByCode("C1");
        doReturn(null).when(spy).findStudentIdByEmail("a@test.com");

        List<String> emails = List.of("a@test.com");

        assertThrows(IllegalArgumentException.class,
                () -> spy.enrollStudentsByEmails("C1", emails));
    }

    @Test
    void enrollStudentsByEmails_skipsBlankEmails() {

        ClassSQL spy = spy(classSQL);

        doReturn(1L).when(spy).findClassIdByCode("C1");

        List<String> emails = new ArrayList<>();
        emails.add("");
        emails.add("   ");
        emails.add(null);

        spy.enrollStudentsByEmails("C1", emails);

        verify(spy, never()).enrollStudent(anyLong(), anyLong());
    }

    @Test
    void enrollStudentsByEmails_skipsExistingEnrollment() {

        ClassSQL spy = spy(classSQL);

        doReturn(1L).when(spy).findClassIdByCode("C1");
        doReturn(2L).when(spy).findStudentIdByEmail("a@test.com");
        doReturn(true).when(spy).enrollmentExists(1L, 2L);

        spy.enrollStudentsByEmails("C1", List.of("a@test.com"));

        verify(spy, never()).enrollStudent(anyLong(), anyLong());
    }

    @Test
    void enrollStudent_throws_onFailure() {

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            dbMock.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> classSQL.enrollStudent(1L, 2L));
        }
    }

    @Test
    void listStudentsForClass_returnsStudents() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getString("first_name")).thenReturn("John");
            when(rs.getString("last_name")).thenReturn("Doe");
            when(rs.getString("email")).thenReturn("j@test.com");
            when(rs.getString("student_code")).thenReturn("S1");

            List<Map<String, Object>> result = classSQL.listStudentsForClass(1L);

            assertEquals(1, result.size());
        }
    }

    @Test
    void listClassesForStudent_returnsClasses() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getString("class_code")).thenReturn("C1");
            when(rs.getString("name")).thenReturn("Math");

            List<Map<String, Object>> result = classSQL.listClassesForStudent(1L);

            assertEquals(1, result.size());
        }
    }

    @Test
    void listClassesForStudent_throws_onFailure() {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            db.when(DatabaseConnection::getConnection)
                    .thenThrow(new SQLException());

            assertThrows(IllegalStateException.class,
                    () -> classSQL.listClassesForStudent(1L));
        }
    }

    @Test
    void listStudentsNotEnrolled_returnsList() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getString("first_name")).thenReturn("Jane");
            when(rs.getString("last_name")).thenReturn("Doe");
            when(rs.getString("email")).thenReturn("j@test.com");
            when(rs.getString("student_code")).thenReturn(null);

            List<Map<String, Object>> result =
                    classSQL.listStudentsNotEnrolledInClass("C1");

            assertEquals("", result.get(0).get("studentCode")); // edge case
        }
    }

    @Test
    void findStudentIdByEmail_returnsId() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getLong("id")).thenReturn(7L);

            assertEquals(7L, classSQL.findStudentIdByEmail("s@test.com"));
        }
    }

    @Test
    void findClassIdByCode_returnsId() throws Exception {
        try (MockedStatic<DatabaseConnection> db = mockStatic(DatabaseConnection.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            db.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getLong("id")).thenReturn(5L);

            assertEquals(5L, classSQL.findClassIdByCode("C1"));
        }
    }
}