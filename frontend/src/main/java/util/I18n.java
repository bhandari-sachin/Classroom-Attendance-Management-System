package util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class I18n {

    private static Locale locale = Locale.ENGLISH;
    private static ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", locale);

    private I18n() {
    }

    public static String t(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static Locale getLocale() {
        return locale;
    }

    public static boolean isArabic() {
        return "ar".equalsIgnoreCase(locale.getLanguage());
    }

    public static boolean isRtl() {
        return isArabic();
    }

    public static void setLocale(Locale newLocale) {
        locale = newLocale;
        bundle = ResourceBundle.getBundle("MessagesBundle", locale);
    }

    public static void setEnglish() {
        setLocale(Locale.ENGLISH);
    }

    public static void setArabic() {
        setLocale(Locale.forLanguageTag("ar-MA"));
    }

    public static String currentLanguageCode() {
        return isArabic() ? "العربية" : "English";
    }
}