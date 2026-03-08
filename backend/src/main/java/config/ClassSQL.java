package config;

<<<<<<< HEAD
import model.CourseClass;
import model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClassSQL {
    // validation methods
    public boolean isStudentEnrolled(Long studentId, Long classId) {
        String sql = """
            SELECT COUNT(*)
            FROM enrollments
            WHERE student_id = ?
            AND class_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setLong(2, classId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getEnrollmentCount(Long classId) {
        String sql = """
            SELECT COUNT(*)
            FROM enrollments
            WHERE class_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getClassCapacity(Long classId) {
        String sql = """
            SELECT max_capacity
            FROM classes
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("max_capacity");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Admin features
    public void createClass(CourseClass c) {
        String sql = """
                INSERT INTO classes (class_code, name, teacher_id, semester, academic_year, max_capacity)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getClassCode());
            stmt.setString(2, c.getName());
            stmt.setLong(3, c.getTeacherId());
            stmt.setString(4, c.getSemester());
            stmt.setString(5, c.getAcademicYear());
            stmt.setInt(6, c.getMaxCapacity());

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateClass(CourseClass c) {
        String sql = """
                UPDATE classes
                SET class_code = ?, name = ?, teacher_id = ?, semester = ?, academic_year = ?, max_capacity = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getClassCode());
            stmt.setString(2, c.getName());
            stmt.setLong(3, c.getTeacherId());
            stmt.setString(4, c.getSemester());
            stmt.setString(5, c.getAcademicYear());
            stmt.setInt(6, c.getMaxCapacity());
            stmt.setLong(7, c.getId());

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void deleteClass(Long classId) {
        String sql = "DELETE FROM classes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void enrollStudent(Long classId, Long studentId) {
        String sql = """
            INSERT INTO enrollments (student_id, class_id, status)
            VALUES (?, ?, 'ACTIVE')
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setLong(2, classId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void assignTeacher(Long classId, Long teacherId) {
        String sql = """
            UPDATE classes
            SET teacher_id = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, teacherId);
            stmt.setLong(2, classId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CourseClass> findAll() {

        List<CourseClass> classes = new ArrayList<>();

        String sql = """
                SELECT c.*, u.first_name AS teacher_first_name, u.last_name AS teacher_last_name, u.email AS teacher_email
                FROM classes c
                LEFT JOIN users u ON c.teacher_id = u.id
                ORDER BY academic_year DESC, semester DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CourseClass cls = new CourseClass(
                        rs.getLong("id"),
                        rs.getString("class_code"),
                        rs.getString("name"),
                        rs.getLong("teacher_id"),
                        rs.getString("semester"),
                        rs.getString("academic_year"),
                        rs.getInt("max_capacity")
                );
                cls.setTeacherName(
                        rs.getString("teacher_first_name") + " " + rs.getString("teacher_last_name")
                );
                cls.setTeacherEmail(rs.getString("teacher_email"));
                classes.add(cls);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }

    public List<CourseClass> findByTeacherId(Long teacherId) {

        List<CourseClass> classes = new ArrayList<>();

        String sql = """
                SELECT *
                FROM classes
                WHERE teacher_id = ?
                ORDER BY academic_year DESC, semester DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, teacherId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                classes.add(new CourseClass(
                        rs.getLong("id"),
                        rs.getString("class_code"),
                        rs.getString("name"),
                        rs.getLong("teacher_id"),
                        rs.getString("semester"),
                        rs.getString("academic_year"),
                        rs.getInt("max_capacity")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }

    public List<Student> getStudentsInClass(Long classId) {
        String sql = """
            SELECT u.*
            FROM users u
            JOIN enrollments e ON u.id = e.student_id
            WHERE e.class_id = ?
            AND u.user_type = 'STUDENT'
        """;

        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                students.add(new Student(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("student_code")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return students;
    }

    public List<CourseClass> findByStudentId(Long studentId) {
        List<CourseClass> classes = new ArrayList<>();

        String sql = """
                SELECT c.*
                FROM classes c
                JOIN enrollments e ON c.id = e.class_id
                WHERE e.student_id = ?
                ORDER BY academic_year DESC, semester DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                classes.add(new CourseClass(
                        rs.getLong("id"),
                        rs.getString("class_code"),
                        rs.getString("name"),
                        rs.getLong("teacher_id"),
                        rs.getString("semester"),
                        rs.getString("academic_year"),
                        rs.getInt("max_capacity")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }
}
=======
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassSQL {

    public static class ClassView {
        public long id;
        public String classCode;
        public String name;
        public String teacherEmail;
        public String semester;
        public String academicYear;
        public int studentsCount;

        public ClassView(long id, String classCode, String name,
                         String teacherEmail, String semester, String academicYear, int studentsCount) {
            this.id = id;
            this.classCode = classCode;
            this.name = name;
            this.teacherEmail = teacherEmail;
            this.semester = semester;
            this.academicYear = academicYear;
            this.studentsCount = studentsCount;
        }
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
                        rs.getLong("id"),
                        rs.getString("class_code"),
                        rs.getString("name"),
                        rs.getString("teacher_email"),
                        rs.getString("semester"),
                        rs.getString("academic_year"),
                        rs.getInt("students_count")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }

    // ================== NEW: helpers for POST / create class ==================

    public Long findTeacherIdByEmail(String teacherEmail) {
        String sql = """
            SELECT id
            FROM users
            WHERE email = ?
              AND user_type = 'TEACHER'
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, teacherEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
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

        } catch (Exception e) {
            throw new RuntimeException("Failed to create class: " + e.getMessage(), e);
        }

        throw new RuntimeException("Failed to create class: no id returned");
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
                            "id", rs.getLong("id"),
                            "classCode", rs.getString("class_code"),
                            "name", rs.getString("name")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
                            "id", rs.getLong("id"),
                            "firstName", rs.getString("first_name"),
                            "lastName", rs.getString("last_name"),
                            "email", rs.getString("email"),
                            "studentCode", rs.getString("student_code")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
                            "id", rs.getLong("id"),
                            "classCode", rs.getString("class_code"),
                            "name", rs.getString("name")
                    ));
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load classes for student", e);
        }
    }


}
>>>>>>> origin/admin-api
