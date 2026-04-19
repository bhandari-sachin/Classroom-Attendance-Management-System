package service;

import config.LocalizationSQL;

import java.util.List;
import java.util.Map;

public class TranslationService {

    private final LocalizationSQL localizationSQL = new LocalizationSQL();

    public Map<String, String> getUiTranslations(String languageCode) {
        return LocalizationSQL.getLabels(languageCode);
    }

    public List<LocalizationSQL.LanguageItem> getActiveLanguages() {
        return localizationSQL.getActiveLanguages();
    }

    public String getUserTypeLabel(String code, String languageCode) {
        return localizationSQL.getUserTypeLabel(code, languageCode);
    }

    public String getAttendanceStatusLabel(String code, String languageCode) {
        return localizationSQL.getAttendanceStatusLabel(code, languageCode);
    }
}