package config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassSQL {

    private static final Logger LOGGER = Logger.getLogger(ClassSQL.class.getName());
    private static final String COL_ID = "id";
    private static final String COL_CLASS_CODE = "class_code";
    private static final String COL_NAME = "name";
    private static final String COL_TEACHER_EMAIL = "teacher_email";
    private static final String COL_SEMESTER = "semester";
    private static final String COL_ACADEMIC_YEAR = "academic_year";
    private static final String COL_STUDENTS_COUNT = "students_count";
    private static final String COL_EMAIL = "email";
    private static final String COL_STUDENT_CODE = "student_code";

    public record ClassView(long id, String classCode, String name, String teacherEmail, String semester,
                            String academicYear, int studentsCount) {
    }

    public List<ClassView> listAllForAdmin() {
        String sql = """
            SELECT
                c.id,
                c.class_code,
                c.name,
                u.email AS teacher_email,
                c.semester,
                c.academic_year,
                (SELECT COUNT(*) FROM enrollments e WHERE e.class_id = c.id) AS students_count
            FROM classes c
            JOIN users u ON u.id = c.teacher_id
            ORDER BY c.created_at DESC
        """;

        List<ClassView> out = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                out.add(new ClassView(
                        rs.getLong(COL_ID),
                        rs.getString(COL_CLASS_CODE),
                        rs.getString(COL_NAME),
                        rs.getString(COL_TEACHER_EMAIL),
                        rs.getString(COL_SEMESTER),
                        rs.getString(COL_ACADEMIC_YEAR),
                        rs.getInt(COL_STUDENTS_COUNT)
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to list classes for admin", e);
        }

        return out;
    }

    // ================== NEW: helpers for POST / create class ==================

    public Long findTeacherIdByEmail(String teacherEmail) {
        String sql = "SELECT id FROM users WHERE " + COL_EMAIL + " = ? AND user_type = 'TEACHER' LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, teacherEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(COL_ID);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find teacher by email: " + e.getMessage(), e);
        }
        return null;
    }

    public long createClass(
            String classCode,
            String name,
            long teacherId,
            String semester,
            String academicYear,
            Integer maxCapacity
    ) {
        String sql = """
            INSERT INTO classes (class_code, name, teacher_id, semester, academic_year, max_capacity)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, classCode);
            ps.setString(2, name);
            ps.setLong(3, teacherId);

            // allow NULLs if empty
            ps.setString(4, (semester == null || semester.isBlank()) ? null : semester);
            ps.setString(5, (academicYear == null || academicYear.isBlank()) ? null : academicYear);

            if (maxCapacity == null || maxCapacity <= 0) {
                ps.setObject(6, null);
            } else {
                ps.setInt(6, maxCapacity);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create class: " + e.getMessage(), e);
        }

        throw new IllegalStateException("Failed to create class: no id returned");
    }
    // ===== TEACHER: list my classes =====
    public List<java.util.Map<String, Object>> listForTeacher(long teacherId) {
        String sql = """
        SELECT c.id, c.class_code, c.name
        FROM classes c
        WHERE c.teacher_id = ?
        ORDER BY c.created_at DESC
    """;

        List<java.util.Map<String, Object>> out = new ArrayList<>();

        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, teacherId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(java.util.Map.of(
                            "id", rs.getLong(COL_ID),
                            "classCode", rs.getString(COL_CLASS_CODE),
                            "name", rs.getString(COL_NAME)
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to list classes for teacher", e);
        }
        return out;
    }

    // ===== TEACHER: verify ownership =====
    public boolean isClassOwnedByTeacher(long classId, long teacherId) {
        String sql = "SELECT COUNT(*) FROM classes WHERE id = ? AND teacher_id = ?";
        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            ps.setLong(2, teacherId);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to verify class ownership", e);
            return false;
        }
    }
    public java.util.List<java.util.Map<String, Object>> listStudentsForClass(long classId) {
        String sql = """
        SELECT
            u.id,
            u.first_name,
            u.last_name,
            u.email,
            u.student_code
        FROM enrollments e
        JOIN users u ON u.id = e.student_id
        WHERE e.class_id = ?
          AND u.user_type = 'STUDENT'
        ORDER BY u.last_name, u.first_name
    """;

        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, classId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(java.util.Map.of(
                            "id", rs.getLong(COL_ID),
                            "firstName", rs.getString("first_name"),
                            "lastName", rs.getString("last_name"),
                            COL_EMAIL, rs.getString(COL_EMAIL),
                            "studentCode", rs.getString(COL_STUDENT_CODE)
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to list students for class", e);
        }

        return out;
    }
    public int countForTeacher(long teacherId) {
        String sql = "SELECT COUNT(*) FROM classes WHERE teacher_id = ?";
        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to count classes for teacher", e);
            return 0;
        }
    }

    public int countStudentsForTeacher(long teacherId) {
        // counts distinct enrolled students across teacher's classes
        String sql = """
        SELECT COUNT(DISTINCT e.student_id)
        FROM enrollments e
        JOIN classes c ON c.id = e.class_id
        WHERE c.teacher_id = ?
    """;
        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to count students for teacher", e);
            return 0;
        }
    }
    public List<Map<String, Object>> listClassesForStudent(Long studentId) {
        String sql = """
        SELECT c.id, c.class_code, c.name
        FROM classes c
        JOIN enrollments e ON e.class_id = c.id
        WHERE e.student_id = ? AND e.status = 'ACTIVE'
        ORDER BY c.name
        """;

        List<Map<String, Object>> result = new java.util.ArrayList<>();

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(Map.of(
                            "id", rs.getLong(COL_ID),
                            "classCode", rs.getString(COL_CLASS_CODE),
                            "name", rs.getString(COL_NAME)
                    ));
                }
            }

            return result;

        } catch (SQLException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to load classes for student", e);
        }
    }
    public List<Map<String, Object>> listStudentsNotEnrolledInClass(String classCode) {
        String sql = """
        SELECT
            u.id,
            u.first_name,
            u.last_name,
            u.email,
            u.student_code
        FROM users u
        WHERE u.user_type = 'STUDENT'
          AND u.id NOT IN (
              SELECT e.student_id
              FROM enrollments e
              JOIN classes c ON c.id = e.class_id
              WHERE c.class_code = ?
          )
        ORDER BY u.last_name, u.first_name
    """;

        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, classCode);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(Map.of(
                            "id", rs.getLong(COL_ID),
                            "firstName", rs.getString("first_name"),
                            "lastName", rs.getString("last_name"),
                            COL_EMAIL, rs.getString(COL_EMAIL),
                            "studentCode", rs.getString(COL_STUDENT_CODE) == null ? "" : rs.getString(COL_STUDENT_CODE)
                    ));
                }
            }
        } catch (SQLException | RuntimeException e) {
            throw new IllegalStateException("Failed to load available students", e);
        }

        return out;
    }

    public Long findClassIdByCode(String classCode) {
        String sql = """
        SELECT id
        FROM classes
        WHERE class_code = ?
        LIMIT 1
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, classCode);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(COL_ID);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find class by code", e);
        }

        return null;
    }

    public Long findStudentIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE " + COL_EMAIL + " = ? AND user_type = 'STUDENT' LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(COL_ID);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find student by email", e);
        }

        return null;
    }

    public boolean enrollmentExists(long classId, long studentId) {
        String sql = """
        SELECT COUNT(*)
        FROM enrollments
        WHERE class_id = ?
          AND student_id = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, classId);
            ps.setLong(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to check enrollment existence", e);
        }

        return false;
    }

    public void enrollStudent(long classId, long studentId) {
        String sql = """
        INSERT INTO enrollments (class_id, student_id, status)
        VALUES (?, ?, 'ACTIVE')
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, classId);
            ps.setLong(2, studentId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to enroll student", e);
        }
    }

    public void enrollStudentsByEmails(String classCode, List<String> studentEmails) {
        Long classId = findClassIdByCode(classCode);
        if (classId == null) {
            throw new IllegalArgumentException("Class not found: " + classCode);
        }

        for (String email : studentEmails) {
            if (email == null || email.isBlank()) continue;

            Long studentId = findStudentIdByEmail(email.trim());
            if (studentId == null) {
                throw new IllegalArgumentException("Student not found: " + email);
            }

            if (!enrollmentExists(classId, studentId)) {
                enrollStudent(classId, studentId);
            }
        }
    }


}