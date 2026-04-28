package frontend.ui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void getBundleShouldUseDefaultLanguageForNullOrBlankInput(String languageCode) {
        assertBundleLoadsWithCommonAppTitle(languageCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "fi", "ar", "ar-MA"})
    void getBundleShouldLoadSupportedBundles(String languageCode) {
        assertBundleLoadsWithCommonAppTitle(languageCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"  en  ", "  fi  ", "  ar  "})
    void getBundleShouldTrimLanguageCode(String languageCode) {
        assertBundleLoadsWithCommonAppTitle(languageCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"xx", "zz-ZZ"})
    void getBundleShouldFallbackWhenLanguageIsUnknown(String languageCode) {
        assertBundleLoadsWithCommonAppTitle(languageCode);
    }

    private void assertBundleLoadsWithCommonAppTitle(String languageCode) {
        ResourceBundle bundle = Local.getBundle(languageCode);

        assertNotNull(bundle);
        assertTrue(bundle.containsKey("common.app.title"));
    }
}