package frontend.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FrontendI18nTest {

    @BeforeEach
    void resetState() {
        FrontendI18n.setLanguage("en");
        FrontendI18n.setTranslations(null);
        FrontendI18n.clear();
    }

    // ===== Language =====

    @Test
    void setLanguage_shouldUpdateLanguage() {
        FrontendI18n.setLanguage("fi");

        assertEquals("fi", FrontendI18n.getLanguage());
    }

    @Test
    void setLanguage_shouldIgnoreNull() {
        FrontendI18n.setLanguage(null);

        assertEquals("en", FrontendI18n.getLanguage());
    }

    @Test
    void setLanguage_shouldIgnoreBlank() {
        FrontendI18n.setLanguage("   ");

        assertEquals("en", FrontendI18n.getLanguage());
    }

    // ===== Translations =====

    @Test
    void setTranslations_shouldStoreValues() {
        FrontendI18n.setTranslations(Map.of("hello", "Bonjour"));

        assertEquals("Bonjour", FrontendI18n.t("hello"));
    }

    @Test
    void setTranslations_shouldHandleNull() {
        FrontendI18n.setTranslations(null);

        assertEquals("hello", FrontendI18n.t("hello"));
    }

    @Test
    void t_shouldReturnTranslatedValue() {
        FrontendI18n.setTranslations(Map.of("key", "value"));

        assertEquals("value", FrontendI18n.t("key"));
    }

    @Test
    void t_shouldReturnKeyWhenMissing() {
        FrontendI18n.setTranslations(Map.of());

        assertEquals("missing", FrontendI18n.t("missing"));
    }

    @Test
    void t_shouldReturnEmptyWhenKeyNullOrBlank() {
        assertEquals("", FrontendI18n.t(null));
        assertEquals("", FrontendI18n.t("   "));
    }

    // ===== With fallback =====

    @Test
    void t_withFallback_shouldReturnTranslatedValue() {
        FrontendI18n.setTranslations(Map.of("key", "value"));

        assertEquals("value", FrontendI18n.t("key", "fallback"));
    }

    @Test
    void t_withFallback_shouldReturnFallbackWhenMissing() {
        FrontendI18n.setTranslations(Map.of());

        assertEquals("fallback", FrontendI18n.t("missing", "fallback"));
    }

    @Test
    void t_withFallback_shouldReturnFallbackWhenKeyNullOrBlank() {
        assertEquals("fallback", FrontendI18n.t(null, "fallback"));
        assertEquals("fallback", FrontendI18n.t("   ", "fallback"));
    }

    // ===== Clear =====

    @Test
    void clear_shouldRemoveTranslations() {
        FrontendI18n.setTranslations(Map.of("key", "value"));

        FrontendI18n.clear();

        assertEquals("key", FrontendI18n.t("key"));
    }
}