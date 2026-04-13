package config;

import model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudentSQL {

    private static final Logger LOGGER = Logger.getLogger(StudentSQL.class.getName());

    public Student findById(Long studentId) {
        String sql = "SELECT id, first_name, last_name, email, student_code FROM users WHERE id = ? AND user_type = 'STUDENT'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Student(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getLong("student_code")
                );
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find student by id", e);
        }
        return null;
    }

    public List<Student> findByClassId(Long classId) {
        String sql = """
            SELECT DISTINCT u.id, u.first_name, u.last_name, u.email, u.student_code
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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find students by class id", e);
        }

        return students;
    }
}

