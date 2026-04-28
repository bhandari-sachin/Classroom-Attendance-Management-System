package backend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationExceptionTest {

    @Test
    void constructorShouldSetMessage() {
        ConfigurationException ex =
                new ConfigurationException("Config error");

        assertEquals("Config error", ex.getMessage());
    }

    @Test
    void constructorShouldSetMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");

        ConfigurationException ex =
                new ConfigurationException("Config failed", cause);

        assertEquals("Config failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void shouldBeThrown() {
        ConfigurationException ex = assertThrows(
                ConfigurationException.class,
                () -> { throw new ConfigurationException("Error"); }
        );

        assertEquals("Error", ex.getMessage());
    }
}