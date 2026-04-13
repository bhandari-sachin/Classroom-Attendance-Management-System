package config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalizationSQL {

    private static final Logger LOGGER = Logger.getLogger(LocalizationSQL.class.getName());

    public static Map<String, String> getLabels(String language) {
        String sql = """
            SELECT translation_key, value
            FROM ui_translations
            WHERE language_code = ?
        """;

        Map<String, String> labels = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, language);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    labels.put(rs.getString("translation_key"), rs.getString("value"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load UI labels", e);
        }

        return labels;
    }

    public List<LanguageItem> getActiveLanguages() {
        String sql = """
            SELECT code, name, is_default, is_active
            FROM languages
            WHERE is_active = TRUE
            ORDER BY is_default DESC, name ASC
        """;

        List<LanguageItem> languages = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                languages.add(new LanguageItem(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getBoolean("is_default"),
                        rs.getBoolean("is_active")
                ));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load active languages", e);
        }

        return languages;
    }

    public String getUserTypeLabel(String code, String languageCode) {
        String sql = """
            SELECT label
            FROM user_type_translation
            WHERE type_code = ? AND language_code = ?
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            stmt.setString(2, languageCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("label");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load user type label", e);
        }

        return code;
    }

    public String getAttendanceStatusLabel(String code, String languageCode) {
        String sql = """
            SELECT label
            FROM attendance_status_translation
            WHERE status_code = ? AND language_code = ?
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            stmt.setString(2, languageCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("label");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load attendance status label", e);
        }

        return code;
    }

    public record LanguageItem(String code, String name, boolean isDefault, boolean isActive) {
    }
}