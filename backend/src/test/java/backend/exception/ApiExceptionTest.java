package backend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionTest {

    @Test
    void constructorShouldSetMessageAndStatus() {
        ApiException ex = new ApiException(400, "Bad request");

        assertEquals(400, ex.getStatus());
        assertEquals("Bad request", ex.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        ApiException ex = new ApiException(401, "Unauthorized");

        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void shouldPreserveStatusCode() {
        ApiException ex = new ApiException(403, "Forbidden");

        assertEquals(403, ex.getStatus());
    }
}