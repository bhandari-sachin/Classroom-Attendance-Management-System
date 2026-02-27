package config;

import model.User;
import model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserSQL {
    public List<User> findAll() {

        String sql = "SELECT * FROM users";

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        UserRole.valueOf(rs.getString("user_type")),
                        // read student_code as String
                        rs.getString("student_code")
                        ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public List<User> findByRole(UserRole role) {

        String sql = "SELECT * FROM users WHERE user_type = ?";

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.name());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        UserRole.valueOf(rs.getString("user_type")),
                        // read student_code as String
                        rs.getString("student_code")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public void update(User user) {

        String sql = """
        UPDATE users
        SET email = ?, first_name = ?, last_name = ?, user_type = ?
        WHERE id = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getRole().name());
            stmt.setLong(5, user.getId());

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(Long userId) {

        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // search by name or email
    public List<User> search(String keyword) {

        String sql = """
        SELECT *
        FROM users
        WHERE LOWER(first_name) LIKE LOWER(?)
           OR LOWER(last_name) LIKE LOWER(?)
           OR LOWER(email) LIKE LOWER(?)
    """;

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        UserRole.valueOf(rs.getString("user_type")),
                        // read student_code as String
                        rs.getString("student_code")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public int countUserClasses(Long userId) {
        String sql = """
        SELECT COUNT(e.class_id) AS enrolled_classes
        FROM users u
            LEFT JOIN enrollments e ON u.id = e.student_id
        WHERE u.id = ?
        GROUP BY u.id
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return rs.getInt("enrolled_classes");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


}
