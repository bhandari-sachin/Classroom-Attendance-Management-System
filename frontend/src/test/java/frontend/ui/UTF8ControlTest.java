package frontend.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class UTF8ControlTest {

    @Test
    void shouldCreateInstance() {
        assertNotNull(new UTF8Control());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void newBundleShouldReturnNullWhenBundleDoesNotExist(boolean reload) throws IOException {
        UTF8Control control = new UTF8Control();

        ResourceBundle bundle = control.newBundle(
                "missing_bundle_for_test",
                Locale.ROOT,
                "java.properties",
                Thread.currentThread().getContextClassLoader(),
                reload
        );

        assertNull(bundle);
    }

    @Test
    void newBundleShouldLoadExistingBaseBundle() throws IOException {
        UTF8Control control = new UTF8Control();

        ResourceBundle bundle = control.newBundle(
                "messages",
                Locale.ROOT,
                "java.properties",
                Thread.currentThread().getContextClassLoader(),
                false
        );

        assertNotNull(bundle);
        assertFalse(bundle.keySet().isEmpty());
    }
}