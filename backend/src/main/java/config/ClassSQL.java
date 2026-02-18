package config;

import model.Class;
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
    public void createClass(Class c) {
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
    public void updateClass(Class c) {
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

    public List<Class> findAll() {

        List<Class> classes = new ArrayList<>();

        String sql = """
                SELECT *
                FROM classes
                ORDER BY academic_year DESC, semester DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                classes.add(new Class(
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

    public List<Class> findByTeacherId(Long teacherId) {

        List<Class> classes = new ArrayList<>();

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
                classes.add(new Class(
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
                        rs.getLong("student_code")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return students;
    }
}
