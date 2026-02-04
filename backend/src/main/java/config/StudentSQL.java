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
        String sql = "SELECT * FROM student WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Student(
                        rs.getLong("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Student> findByClassId(Long classId) {
        String sql = """
            SELECT DISTINCT s.student_id, s.user_id
            FROM student s
            JOIN attendance a ON s.student_id = a.student_id
            JOIN session se ON a.session_id = se.session_id
            WHERE se.class_id = ?
        """;

        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                students.add(new Student(
                        rs.getLong("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return students;
    }
}

