package frontend.ui;

public class HelperClass {

    public String getMessage(String key) {
        return Local.getBundle(UiPreferences.getLanguage()).getString(key);
    }

    public void setLocale(String lang) {
        UiPreferences.setLanguage(lang);
    }
}