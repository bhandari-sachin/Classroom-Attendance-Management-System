package config;

import model.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionSQL {

    public Session findById(Long sessionId) throws SQLException {
        String sql = "SELECT * FROM sessions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Session(
                        rs.getLong("id"),
                        rs.getLong("class_id"),
                        rs.getDate("session_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getString("topic"),
                        rs.getString("qr_token"),
                        rs.getString("status")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Session> findByClassId(Long classId) {
        String sql = """
                    SELECT * FROM sessions
                    WHERE class_id = ?
                    ORDER BY session_date DESC
                """;

        List<Session> sessions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, classId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sessions.add(new Session(
                        rs.getLong("id"),
                        rs.getLong("class_id"),
                        rs.getDate("session_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getString("topic"),
                        rs.getString("qr_token"),
                        rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sessions;
    }

    public void updateQRCode(Long sessionId, String code) {
        String sql = """
                    UPDATE sessions
                    SET qr_token = ?, status = 'SCHEDULED'
                    WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            stmt.setLong(2, sessionId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateStatus(Long sessionId, String status) {
        String sql = """
                    UPDATE sessions
                    SET status = ?
                    WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setLong(2, sessionId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void activateSession(Long sessionId, String qrToken) {
        String sql = """
                    UPDATE sessions
                    SET qr_token = ?,
                        start_time = CURTIME(),
                        status = 'ACTIVE'
                    WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, qrToken);
            stmt.setLong(2, sessionId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void completeSession(Long sessionId) {
        String sql = """
                    UPDATE sessions
                    SET end_time = CURTIME(),
                        status = 'COMPLETED'
                    WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
