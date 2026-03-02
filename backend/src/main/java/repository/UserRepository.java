package repository;

import config.DatabaseConnection;
import model.User;
import model.UserRole;

import java.sql.*;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByEmail(String email) {
        String sql = """
            SELECT id, email, password_hash, first_name, last_name, user_type, student_code
            FROM users
            WHERE email = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error checking email existence", e);
        }
    }

    public void save(User user) {
        String sql = """
            INSERT INTO users (email, password_hash, first_name, last_name, user_type, student_code)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getUserType().name());
            ps.setString(6, user.getStudentCode()); // null allowed for non-students

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                UserRole.valueOf(rs.getString("user_type")),
                rs.getString("student_code")
        );
    }

    public void insert(String email, String hash, String firstName, String lastName, String role, String studentCode) {
        String sql = """
        INSERT INTO users (email, password_hash, first_name, last_name, user_type, student_code)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, email);
            ps.setString(2, hash);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, role);
            ps.setString(6, studentCode); // must be NULL for non-students

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting user", e);
        }
    }
    public int countByRole(UserRole role) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting users by role", e);
        }
    }

    public java.util.List<User> findAll() {
        String sql = """
        SELECT id, email, password_hash, first_name, last_name, user_type, student_code
        FROM users
        ORDER BY created_at DESC
    """;

        java.util.List<User> out = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(mapRow(rs));
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all users", e);
        }
    }
    public java.util.List<java.util.Map<String, String>> findAllTeachers() {
        String sql = """
        SELECT first_name, last_name, email
        FROM users
        WHERE user_type = 'TEACHER'
        ORDER BY last_name, first_name
    """;

        java.util.List<java.util.Map<String, String>> out = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String first = rs.getString("first_name");
                String last  = rs.getString("last_name");
                String email = rs.getString("email");

                String fullName = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
                out.add(java.util.Map.of(
                        "teacherName", fullName.isBlank() ? "Teacher" : fullName,
                        "email", email == null ? "" : email
                ));
            }

            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching teachers", e);
        }
    }



}