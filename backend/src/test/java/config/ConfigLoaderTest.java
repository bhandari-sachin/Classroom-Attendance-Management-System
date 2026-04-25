package config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    @Test
    void testGetExistingProperty() {
        String value = ConfigLoader.get("api.admin.classes");

        assertEquals("/api/admin/classes", value);
    }

    @Test
    void testGetNonExistingProperty() {
        String value = ConfigLoader.get("non.existing.key");

        assertNull(value);
    }

    @Test
    void testRepeatedAccessReturnsSameValue() {
        String first = ConfigLoader.get("api.admin.classes");
        String second = ConfigLoader.get("api.admin.classes");

        assertEquals(first, second);
    }

    @Test
    void testPrivateConstructorCoverage() throws Exception {
        Constructor<ConfigLoader> constructor =
                ConfigLoader.class.getDeclaredConstructor();

        constructor.setAccessible(true);
        ConfigLoader instance = constructor.newInstance();

        assertNotNull(instance);
    }
}