package frontend.ui;
/**
 * Helper class for UI translations and language management.
 */
public class HelperClass {
    /**
     * Get translated message by key.
     */
    public String getMessage(String key) {
        return Local.getBundle(UiPreferences.getLanguage()).getString(key);
    }

}