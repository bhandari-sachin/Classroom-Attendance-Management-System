package config;

import model.Student;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StudentSQLTest {

    private final StudentSQL studentSQL = new StudentSQL();

    // Track inserted ids to cleanup safely
    private final List<Long> userIds = new ArrayList<>();
    private final List<Long> classIds = new ArrayList<>();
    private final List<Long> enrollmentIds = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        // delete in reverse FK order: enrollments -> classes -> users
        try (Connection conn = DatabaseConnection.getConnection()) {

            if (!enrollmentIds.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM enrollments WHERE id = ?")) {
                    for (Long id : enrollmentIds) {
                        ps.setLong(1, id);
                        ps.executeUpdate();
                    }
                }
            }

            if (!classIds.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM classes WHERE id = ?")) {
                    for (Long id : classIds) {
                        ps.setLong(1, id);
                        ps.executeUpdate();
                    }
                }
            }

            if (!userIds.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    for (Long id : userIds) {
                        ps.setLong(1, id);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }



    @Test
    void findById_returnsNull_whenUserIsNotStudent() throws Exception {
        long teacherId = insertUser(
                uniqueEmail("teacher"),
                "$2a$10$95DbuhjYv6biZ9Amv4xUvO07Dw90GfhHkgC3v44YpIlgKHKg2AVkq",
                "Tom",
                "Teacher",
                "TEACHER",
                null
        );

        Student s = studentSQL.findById(teacherId);

        assertNull(s);
    }

    @Test
    void findById_returnsNull_whenNotFound() {
        Student s = studentSQL.findById(99999999L);
        assertNull(s);
    }



    @Test
    void findByClassId_returnsEmpty_whenNoEnrollments() throws Exception {
        long teacherId = insertUser(
                uniqueEmail("teacher"),
                "$2a$10$95DbuhjYv6biZ9Amv4xUvO07Dw90GfhHkgC3v44YpIlgKHKg2AVkq",
                "Tina",
                "Teach",
                "TEACHER",
                null
        );

        long classId = insertClass("CS-" + UUID.randomUUID().toString().substring(0, 6), "EmptyClass", teacherId);

        List<Student> students = studentSQL.findByClassId(classId);
        assertNotNull(students);
        assertTrue(students.isEmpty());
    }

    // ---------------- helpers: insert rows ----------------

    private long insertUser(String email, String hash, String first, String last, String role, String studentCode) throws Exception {
        String sql = """
            INSERT INTO users (email, password_hash, first_name, last_name, user_type, student_code)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, email);
            ps.setString(2, hash);
            ps.setString(3, first);
            ps.setString(4, last);
            ps.setString(5, role);
            ps.setString(6, studentCode); // VARCHAR in DB

            ps.executeUpdate();

            try (var keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next(), "No generated key for users insert");
                long id = keys.getLong(1);
                userIds.add(id);
                return id;
            }
        }
    }

    private long insertClass(String code, String name, long teacherId) throws Exception {
        String sql = """
            INSERT INTO classes (class_code, name, teacher_id, semester, academic_year, max_capacity)
            VALUES (?, ?, ?, 'SPRING', '2025/2026', 30)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, code);
            ps.setString(2, name);
            ps.setLong(3, teacherId);

            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next(), "No generated key for classes insert");
                long id = keys.getLong(1);
                classIds.add(id);
                return id;
            }
        }
    }

    private long insertEnrollment(long studentId, long classId, String status) throws Exception {
        String sql = """
            INSERT INTO enrollments (student_id, class_id, status)
            VALUES (?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, studentId);
            ps.setLong(2, classId);
            ps.setString(3, status);

            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next(), "No generated key for enrollments insert");
                long id = keys.getLong(1);
                enrollmentIds.add(id);
                return id;
            }
        }
    }

    private static String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
    }
}