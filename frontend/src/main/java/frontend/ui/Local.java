package frontend.ui;

import java.util.Locale;
import java.util.ResourceBundle;

public class Local {

    public static ResourceBundle getBundle(String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        return ResourceBundle.getBundle("messages", locale, new frontend.ui.UTF8Control());
    }
}