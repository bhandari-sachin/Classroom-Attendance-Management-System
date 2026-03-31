package util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class I18n {

    private static Locale locale = new Locale("ar", "MA");
    private static ResourceBundle bundle = loadBundle(locale);

    private I18n() {}

    public static void setLocale(String languageTag) {
        locale = Locale.forLanguageTag(languageTag);
        bundle = loadBundle(locale);
    }

    public static String t(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    public static Locale getLocale() {
        return locale;
    }

    public static boolean isRTL() {
        return "ar".equalsIgnoreCase(locale.getLanguage());
    }
    private static ResourceBundle loadBundle(Locale locale) {
        try {
            String baseName = "MessagesBundle";
            String bundleName = baseName + "_" + locale.toString();
            String resourceName = bundleName + ".properties";

            InputStream stream = I18n.class.getClassLoader().getResourceAsStream(resourceName);

            if (stream == null) {
                // fallback to default
                stream = I18n.class.getClassLoader().getResourceAsStream(baseName + ".properties");
            }

            if (stream == null) {
                throw new RuntimeException("Bundle not found");
            }

            return new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new RuntimeException("Failed to load i18n bundle", e);
        }
    }
}