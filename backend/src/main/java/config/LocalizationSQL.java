package config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class LocalizationSQL {
    public static Map<String, String> getLabels(String language) {

        String sql = """
            SELECT `translation_key`, value
            FROM ui_translations
            WHERE language_code = ?
        """;

        Map<String, String> labels = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, language);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                labels.put(rs.getString("translation_key"), rs.getString("value"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return labels;
    }
}
