package repository;

import config.DatabaseConnection;
import model.User;
import model.UserRole;

import java.sql.*;
import java.util.*;

/**
 * Repository for accessing and managing user data in the database.
 */
public class UserRepository {

    /**
     * Finds a user by email.
     * @param email the user's email address
     * @return Optional containing the user if found, otherwise empty
     */
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
                return Optional.of(mapUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
    }

    /**
     * Checks if a user exists by email.
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
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

    /**
     * Saves a new user to the database.
     * @param user the user to save
     */
    public void save(User user) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = prepareInsertStatement(conn, user)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    /**
     * Prepares a SQL insert statement for a user.
     */
    private PreparedStatement prepareInsertStatement(Connection conn, User user) throws SQLException {
        String sql = """
            INSERT INTO users (email, password_hash, first_name, last_name, user_type, student_code)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, user.getEmail());
        ps.setString(2, user.getPasswordHash());
        ps.setString(3, user.getFirstName());
        ps.setString(4, user.getLastName());
        ps.setString(5, user.getUserType().name());
        ps.setString(6, user.getStudentCode());
        return ps;
    }

    /**
     * Maps a ResultSet row to a User object.
     */
    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
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

    /**
     * Counts users by role.
     * @param role the user role
     * @return number of users with that role
     */
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

    /**
     * Finds all users ordered by creation date.
     * @return list of all users
     */
    public List<User> findAll() {
        String sql = """
            SELECT id, email, password_hash, first_name, last_name, user_type, student_code
            FROM users
            ORDER BY created_at DESC
        """;

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUserFromResultSet(rs));
            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all users", e);
        }
    }

    /**
     * Finds all teachers with name and email.
     * @return list of maps with teacherName and email
     */
    public List<Map<String, String>> findAllTeachers() {
        String sql = """
            SELECT first_name, last_name, email
            FROM users
            WHERE user_type = 'TEACHER'
            ORDER BY last_name, first_name
        """;

        List<Map<String, String>> teacherList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String first = rs.getString("first_name");
                String last  = rs.getString("last_name");
                String email = rs.getString("email");

                String fullName = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
                teacherList.add(Map.of(
                        "teacherName", fullName.isBlank() ? "Teacher" : fullName,
                        "email", email == null ? "" : email
                ));
            }

            return teacherList;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching teachers", e);
        }
    }
}
