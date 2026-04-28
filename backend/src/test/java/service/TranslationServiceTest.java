package service;

import config.LocalizationSQL;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslationServiceTest {

    @Test
    void getUiTranslationsShouldReturnLabels() {

        try (MockedStatic<LocalizationSQL> mocked = mockStatic(LocalizationSQL.class)) {
            Map<String, String> mockMap = Map.of("hello", "Hello");
            mocked.when(() -> LocalizationSQL.getLabels("en"))
                    .thenReturn(mockMap);
            TranslationService service = new TranslationService();
            Map<String, String> result = service.getUiTranslations("en");
            assertEquals("Hello", result.get("hello"));
        }
    }

    @Test
    void getActiveLanguagesShouldReturnList() {

        try (MockedConstruction<LocalizationSQL> mocked =
                     mockConstruction(LocalizationSQL.class,
                             (mock, context) -> {
                                 when(mock.getActiveLanguages())
                                         .thenReturn(List.of());
                             })) {

            TranslationService service = new TranslationService();
            List<LocalizationSQL.LanguageItem> result =
                    service.getActiveLanguages();
            assertNotNull(result);
        }
    }

    @Test
    void getUserTypeLabelShouldReturnValue() {

        try (MockedConstruction<LocalizationSQL> mocked =
                     mockConstruction(LocalizationSQL.class,
                             (mock, context) -> {
                                 when(mock.getUserTypeLabel("ADMIN", "en"))
                                         .thenReturn("Admin");
                             })) {

            TranslationService service = new TranslationService();
            String result = service.getUserTypeLabel("ADMIN", "en");
            assertEquals("Admin", result);
        }
    }

    @Test
    void getAttendanceStatusLabelShouldReturnValue() {

        try (MockedConstruction<LocalizationSQL> mocked =
                     mockConstruction(LocalizationSQL.class,
                             (mock, context) -> {
                                 when(mock.getAttendanceStatusLabel("PRESENT", "en"))
                                         .thenReturn("Present");
                             })) {

            TranslationService service = new TranslationService();
            String result = service.getAttendanceStatusLabel("PRESENT", "en");
            assertEquals("Present", result);
        }
    }
}