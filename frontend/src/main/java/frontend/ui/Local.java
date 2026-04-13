package frontend.ui;

import frontend.UTF8Control;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for loading localized ResourceBundle files.
 *
 * <p>Use this only if the UI still relies on .properties files.
 * For database/API-based translations, prefer FrontendI18n instead.</p>
 */
public final class Local {

    private Local() {
        // Utility class
    }

    public static ResourceBundle getBundle(String languageCode) {
        String normalizedLanguage = (languageCode == null || languageCode.isBlank())
                ? "en"
                : languageCode.trim();

        Locale locale = Locale.forLanguageTag(normalizedLanguage);

        return ResourceBundle.getBundle(
                "messages",
                locale,
                new UTF8Control()
        );
    }
}