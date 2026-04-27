package backend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportExportExceptionTest {

    @Test
    void constructorShouldSetMessageAndCause() {
        Throwable cause = new RuntimeException("Export failure root cause");

        ReportExportException ex =
                new ReportExportException("Report export failed", cause);

        assertEquals("Report export failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void shouldBeThrown() {
        ReportExportException ex = assertThrows(
                ReportExportException.class,
                () -> { throw new ReportExportException("Export failed", new RuntimeException()); }
        );

        assertEquals("Export failed", ex.getMessage());
    }
}