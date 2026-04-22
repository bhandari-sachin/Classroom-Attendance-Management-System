package frontend.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

class UiPreferencesTest {

    private static final Preferences PREFS =
            Preferences.userRoot().node("attendance-app");

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit failed to start");
    }

    @Test
    void constructorShouldBePrivate() throws Exception {
        Constructor<UiPreferences> constructor = UiPreferences.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        assertDoesNotThrow((org.junit.jupiter.api.function.Executable) constructor::newInstance);
    }

    @Test
    void getThemeShouldReturnDefaultWhenPreferenceMissing() {
        PREFS.remove("theme");

        assertEquals(UiPreferences.Theme.LIGHT, UiPreferences.getTheme());
    }

    @Test
    void getThemeShouldReturnStoredLightTheme() {
        PREFS.put("theme", "LIGHT");

        assertEquals(UiPreferences.Theme.LIGHT, UiPreferences.getTheme());
    }

    @Test
    void getThemeShouldReturnStoredDarkTheme() {
        PREFS.put("theme", "DARK");

        assertEquals(UiPreferences.Theme.DARK, UiPreferences.getTheme());
    }

    @Test
    void getThemeShouldReturnDefaultWhenStoredValueIsInvalid() {
        PREFS.put("theme", "INVALID_THEME");

        assertEquals(UiPreferences.Theme.LIGHT, UiPreferences.getTheme());
    }

    @Test
    void setThemeShouldStoreThemeWhenNotNull() {
        UiPreferences.setTheme(UiPreferences.Theme.DARK);

        assertEquals("DARK", PREFS.get("theme", null));
    }

    @Test
    void setThemeShouldIgnoreNull() {
        PREFS.put("theme", "LIGHT");

        UiPreferences.setTheme(null);

        assertEquals("LIGHT", PREFS.get("theme", null));
    }

    @Test
    void getLanguageShouldReturnDefaultWhenPreferenceMissing() {
        PREFS.remove("lang");

        assertEquals("en", UiPreferences.getLanguage());
    }

    @Test
    void getLanguageShouldReturnStoredLanguage() {
        PREFS.put("lang", "fi");

        assertEquals("fi", UiPreferences.getLanguage());
    }

    @Test
    void getLanguageShouldReturnDefaultWhenStoredLanguageIsBlank() {
        PREFS.put("lang", "   ");

        assertEquals("en", UiPreferences.getLanguage());
    }

    @Test
    void setLanguageShouldStoreLanguageWhenValid() {
        UiPreferences.setLanguage("ar");

        assertEquals("ar", PREFS.get("lang", null));
    }

    @Test
    void setLanguageShouldIgnoreNull() {
        PREFS.put("lang", "en");

        UiPreferences.setLanguage(null);

        assertEquals("en", PREFS.get("lang", null));
    }

    @Test
    void setLanguageShouldIgnoreBlank() {
        PREFS.put("lang", "en");

        UiPreferences.setLanguage("   ");

        assertEquals("en", PREFS.get("lang", null));
    }

    @Test
    void applyThemeShouldDoNothingWhenSceneIsNull() {
        assertDoesNotThrow(() -> UiPreferences.applyTheme(null));
    }

    @Test
    void applyThemeShouldApplyDarkThemeClass() throws Exception {
        PREFS.put("theme", "DARK");

        StackPane root = new StackPane();
        root.getStyleClass().addAll("theme-light", "other-class");
        Scene scene = new Scene(root);

        UiPreferences.applyTheme(scene);
        waitForFxEvents();

        assertFalse(root.getStyleClass().contains("theme-light"));
        assertTrue(root.getStyleClass().contains("theme-dark"));
        assertTrue(root.getStyleClass().contains("other-class"));
    }

    @Test
    void applyThemeShouldApplyLightThemeClass() throws Exception {
        PREFS.put("theme", "LIGHT");

        StackPane root = new StackPane();
        root.getStyleClass().addAll("theme-dark", "other-class");
        Scene scene = new Scene(root);

        UiPreferences.applyTheme(scene);
        waitForFxEvents();

        assertFalse(root.getStyleClass().contains("theme-dark"));
        assertTrue(root.getStyleClass().contains("theme-light"));
        assertTrue(root.getStyleClass().contains("other-class"));
    }

    @Test
    void applyThemeShouldFallbackToLightThemeWhenStoredThemeIsInvalid() throws Exception {
        PREFS.put("theme", "BROKEN");

        StackPane root = new StackPane();
        root.getStyleClass().add("theme-dark");
        Scene scene = new Scene(root);

        UiPreferences.applyTheme(scene);
        waitForFxEvents();

        assertFalse(root.getStyleClass().contains("theme-dark"));
        assertTrue(root.getStyleClass().contains("theme-light"));
    }

    private static void waitForFxEvents() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for FX events");
    }
}