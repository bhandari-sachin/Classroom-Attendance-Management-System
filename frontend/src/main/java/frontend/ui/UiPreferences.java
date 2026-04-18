package frontend.ui;

import javafx.application.Platform;
import javafx.scene.Scene;

import java.util.prefs.Preferences;

public class UiPreferences {

    private static final Preferences prefs = Preferences.userRoot().node("attendance-app");

    private static final String KEY_THEME = "theme";
    private static final String KEY_LANG = "lang";

    public enum Theme {
        LIGHT, DARK
    }

    public static Theme getTheme() {
        String t = prefs.get(KEY_THEME, "LIGHT");
        try {
            return Theme.valueOf(t);
        } catch (Exception e) {
            return Theme.LIGHT;
        }
    }

    public static void setTheme(Theme theme) {
        prefs.put(KEY_THEME, theme.name());
    }

    public static String getLanguage() {
        return prefs.get(KEY_LANG, "en");
    }

    public static void setLanguage(String lang) {
        prefs.put(KEY_LANG, lang);
    }

    public static void applyTheme(Scene scene) {
        if (scene == null || scene.getRoot() == null) return;

        Platform.runLater(() -> {
            scene.getRoot().getStyleClass().removeAll("theme-light", "theme-dark");
            if (getTheme() == Theme.DARK) {
                scene.getRoot().getStyleClass().add("theme-dark");
            } else {
                scene.getRoot().getStyleClass().add("theme-light");
            }
        });
    }
}
