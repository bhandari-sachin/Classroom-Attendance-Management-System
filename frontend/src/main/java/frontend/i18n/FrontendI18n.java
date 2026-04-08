package frontend.i18n;

import java.util.HashMap;
import java.util.Map;

public final class FrontendI18n {

    private static String currentLanguage = "en";
    private static Map<String, String> translations = new HashMap<>();

    private FrontendI18n() {
    }

    public static void setLanguage(String languageCode) {
        currentLanguage = languageCode;
    }

    public static String getLanguage() {
        return currentLanguage;
    }

    public static void setTranslations(Map<String, String> values) {
        translations = new HashMap<>(values);
    }

    public static String t(String key) {
        return translations.getOrDefault(key, key);
    }
}