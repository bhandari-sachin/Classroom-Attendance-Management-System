package frontend.admin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminPagesTest {

    @Test
    void toIntShouldReturnZeroWhenValueIsNull() {
        assertEquals(0, AdminPages.toInt(null));
    }

    @Test
    void toIntShouldReturnIntegerValueWhenNumberIsGiven() {
        assertEquals(12, AdminPages.toInt(12));
        assertEquals(5, AdminPages.toInt(5L));
        assertEquals(7, AdminPages.toInt(7.9));
    }

    @Test
    void toIntShouldParseNumericString() {
        assertEquals(42, AdminPages.toInt("42"));
    }

    @Test
    void toIntShouldReturnZeroForInvalidString() {
        assertEquals(0, AdminPages.toInt("abc"));
        assertEquals(0, AdminPages.toInt(""));
    }

    @Test
    void toDoubleShouldReturnZeroWhenValueIsNull() {
        assertEquals(0.0, AdminPages.toDouble(null));
    }

    @Test
    void toDoubleShouldReturnDoubleValueWhenNumberIsGiven() {
        assertEquals(12.0, AdminPages.toDouble(12));
        assertEquals(5.5, AdminPages.toDouble(5.5));
    }

    @Test
    void toDoubleShouldParseNumericString() {
        assertEquals(42.5, AdminPages.toDouble("42.5"));
    }

    @Test
    void toDoubleShouldReturnZeroForInvalidString() {
        assertEquals(0.0, AdminPages.toDouble("abc"));
        assertEquals(0.0, AdminPages.toDouble(""));
    }

    @Test
    void valueOrShouldReturnFallbackWhenValueIsNull() {
        assertEquals("fallback", AdminPages.valueOr(null, "fallback"));
    }

    @Test
    void valueOrShouldReturnStringValueWhenValueIsPresent() {
        assertEquals("hello", AdminPages.valueOr("hello", "fallback"));
        assertEquals("123", AdminPages.valueOr(123, "fallback"));
    }

    @Test
    void safeErrorMessageShouldReturnUnknownErrorWhenThrowableIsNull() {
        assertEquals("Unknown error", AdminPages.safeErrorMessage(null));
    }

    @Test
    void safeErrorMessageShouldReturnUnknownErrorWhenMessageIsNull() {
        assertEquals("Unknown error", AdminPages.safeErrorMessage(new RuntimeException((String) null)));
    }

    @Test
    void safeErrorMessageShouldReturnUnknownErrorWhenMessageIsBlank() {
        assertEquals("Unknown error", AdminPages.safeErrorMessage(new RuntimeException("   ")));
    }

    @Test
    void safeErrorMessageShouldReturnThrowableMessageWhenPresent() {
        assertEquals("Something failed", AdminPages.safeErrorMessage(new RuntimeException("Something failed")));
    }
}