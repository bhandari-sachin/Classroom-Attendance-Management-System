package config;

import config.DatabaseConnection;
import model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudentSQL {

    public Student findById(Long studentId) {
        String sql = "SELECT * FROM users WHERE id = ? AND user_type = 'STUDENT'";
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Student> findByClassId(Long classId) {
        String sql = """
            SELECT DISTINCT u.id
            FROM users u
            JOIN attendance a ON u.id = a.student_id
            JOIN sessions se ON a.session_id = se.id
            WHERE se.class_id = ?
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

