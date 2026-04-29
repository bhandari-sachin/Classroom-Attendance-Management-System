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
        RuntimeException cause = new RuntimeException();

        ReportExportException ex = assertThrows(
                ReportExportException.class,
                () -> { throw new ReportExportException("Export failed", cause); }
        );

        assertEquals("Export failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}