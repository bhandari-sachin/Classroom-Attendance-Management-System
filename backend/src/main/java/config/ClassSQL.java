package config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
}