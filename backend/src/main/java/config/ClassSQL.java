package config;

import model.Class;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClassSQL {

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
}
