package frontend.auth;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceExceptionTest {

    @Test
    void constructorWithMessageShouldSetMessageCorrectly() {
        String message = "Authentication failed";

        AuthServiceException exception = new AuthServiceException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithMessageAndCauseShouldSetBothCorrectly() {
        String message = "Authentication failed";
        Throwable cause = new RuntimeException("Root cause");

        AuthServiceException exception = new AuthServiceException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldBeInstanceOfIOException() {
        AuthServiceException exception = new AuthServiceException("Error");

        assertInstanceOf(IOException.class, exception);
    }

    @Test
    void shouldThrowAndBeCaughtAsAuthServiceException() {
        String message = "Auth error";

        AuthServiceException thrown = assertThrows(
                AuthServiceException.class,
                () -> {
                    throw new AuthServiceException(message);
                }
        );

        assertEquals(message, thrown.getMessage());
    }
}