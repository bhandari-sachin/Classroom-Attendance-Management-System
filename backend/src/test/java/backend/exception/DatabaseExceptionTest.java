package backend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseExceptionTest {

    @Test
    void constructorShouldSetMessageAndCause() {
        Throwable cause = new RuntimeException("DB root error");

        DatabaseException ex =
                new DatabaseException("Database failed", cause);

        assertEquals("Database failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void constructorShouldSetOnlyCause() {
        Throwable cause = new RuntimeException("Connection lost");

        DatabaseException ex =
                new DatabaseException(cause);

        assertEquals(cause.toString(), ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void shouldBeThrownAsRuntimeException() {
        DatabaseException ex = assertThrows(
                DatabaseException.class,
                () -> { throw new DatabaseException(new RuntimeException("fail")); }
        );

        assertNotNull(ex.getCause());
    }
}