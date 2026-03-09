package config;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SessionSQL {

    public long createSession(long classId,
                              java.time.LocalDate date,
                              java.time.LocalTime start,
                              java.time.LocalTime end,
                              String code) {

        String sql = """
        INSERT INTO sessions
        (class_id, session_date, start_time, end_time, qr_token)
        VALUES (?, ?, ?, ?, ?)
    """;

        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, classId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setTime(3, java.sql.Time.valueOf(start));
            ps.setTime(4, java.sql.Time.valueOf(end));
            ps.setString(5, code);

            ps.executeUpdate();

            try (var keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }

            throw new RuntimeException("No session id returned");

        } catch (Exception e) {
            throw new RuntimeException("Create session failed: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> listForClass(long classId) {
        String sql = """
            SELECT id, session_date, start_time, end_time, qr_token
            FROM sessions
            WHERE class_id = ?
            ORDER BY session_date DESC, start_time DESC
        """;

        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, classId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(Map.of(
                            "id", rs.getLong("id"),
                            "date", rs.getDate("session_date").toLocalDate().toString(),
                            "startTime", rs.getTime("start_time").toLocalTime().toString(),
                            "endTime", rs.getTime("end_time").toLocalTime().toString(),
                            "code", rs.getString("qr_token")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }

    public model.Session findById(Long sessionId) throws SQLException {
        String sql = "SELECT * FROM sessions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new model.Session(
                        rs.getLong("id"),
                        rs.getLong("class_id"),
                        rs.getDate("session_date").toLocalDate(),
                        rs.getString("qr_token")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void updateQRCode(Long sessionId, String code) {
        String sql = "UPDATE sessions SET qr_token = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            stmt.setLong(2, sessionId);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update session QR code", e);
        }
    }

    public String getQRCode(Long sessionId) {
        String sql = "SELECT qr_token FROM sessions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("qr_token");
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get session QR code", e);
        }
    }
}