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
    void shouldBeRuntimeException() {
        ConfigurationException ex =
                new ConfigurationException("Error");

        assertTrue(ex instanceof RuntimeException);
    }
}