package frontend.ui;

import javafx.scene.Scene;

import java.util.prefs.Preferences;

/**
 * Manages UI preferences such as theme and language.
 */
public final class UiPreferences {

    private static final Preferences PREFS =
            Preferences.userRoot().node("attendance-app");

    private static final String KEY_THEME = "theme";
    private static final String KEY_LANG = "lang";

    private static final String DEFAULT_LANG = "en";
    private static final Theme DEFAULT_THEME = Theme.LIGHT;

    private UiPreferences() {
        // Utility class
    }

    public enum Theme {
        LIGHT, DARK
    }

    // ===== Theme =====

    public static Theme getTheme() {
        String value = PREFS.get(KEY_THEME, DEFAULT_THEME.name());
        try {
            return Theme.valueOf(value);
        } catch (Exception e) {
            return DEFAULT_THEME;
        }
    }

    public static void setTheme(Theme theme) {
        if (theme != null) {
            PREFS.put(KEY_THEME, theme.name());
        }
    }

    // ===== Language =====

    public static String getLanguage() {
        String lang = PREFS.get(KEY_LANG, DEFAULT_LANG);
        return (lang == null || lang.isBlank()) ? DEFAULT_LANG : lang;
    }

    public static void setLanguage(String lang) {
        if (lang != null && !lang.isBlank()) {
            PREFS.put(KEY_LANG, lang);
        }
    }

    // ===== Apply Theme =====

    /**
     * Applies the selected theme to the scene root.
     */
    public static void applyTheme(Scene scene) {
        if (scene == null || scene.getRoot() == null) return;
        var root = scene.getRoot();

        if (!root.getStyleClass().contains("app-root")) {
            root.getStyleClass().add("app-root");
        }

        // Remove old theme classes
        root.getStyleClass().removeAll("theme-light", "theme-dark");

        // Apply new theme
        if (getTheme() == Theme.DARK) {
            root.getStyleClass().add("theme-dark");
        } else {
            root.getStyleClass().add("theme-light");
        }
    }
}