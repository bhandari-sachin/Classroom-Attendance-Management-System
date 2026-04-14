package frontend.i18n;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple frontend i18n utility for managing translations.
 */
public final class FrontendI18n {

    private static String currentLanguage = "en";
    private static Map<String, String> translations = new HashMap<>();

    private FrontendI18n() {}

    // ===== Language =====

    public static void setLanguage(String languageCode) {
        if (languageCode != null && !languageCode.isBlank()) {
            currentLanguage = languageCode;
        }
    }

    public static String getLanguage() {
        return currentLanguage;
    }

    // ===== Translations =====

    public static void setTranslations(Map<String, String> values) {
        translations = (values != null) ? new HashMap<>(values) : new HashMap<>();
    }

    /**
     * Translate a key. Returns the key itself if not found.
     */
    public static String t(String key) {
        if (key == null || key.isBlank()) return "";
        return translations.getOrDefault(key, key);
    }

    /**
     * Translate with fallback value.
     */
    public static String t(String key, String fallback) {
        if (key == null || key.isBlank()) return fallback;
        return translations.getOrDefault(key, fallback);
    }

    /**
     * Clear translations (useful when switching language).
     */
    public static void clear() {
        translations.clear();
    }
}