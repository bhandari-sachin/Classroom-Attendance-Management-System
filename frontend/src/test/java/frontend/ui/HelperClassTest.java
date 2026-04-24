package frontend.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelperClassTest {

    @Test
    void getMessageShouldReturnValueForExistingKey() {
        HelperClass helper = new HelperClass();

        String result = helper.getMessage("common.app.title");

        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void getMessageShouldChangeWithLanguage() {
        HelperClass helper = new HelperClass();

        UiPreferences.setLanguage("en");
        String english = helper.getMessage("common.app.title");

        UiPreferences.setLanguage("fi");
        String finnish = helper.getMessage("common.app.title");

        assertNotNull(english);
        assertNotNull(finnish);

    }

    @Test
    void getMessageShouldThrowForMissingKey() {
        HelperClass helper = new HelperClass();

        assertThrows(Exception.class, () ->
                helper.getMessage("non.existing.key")
        );
    }
}