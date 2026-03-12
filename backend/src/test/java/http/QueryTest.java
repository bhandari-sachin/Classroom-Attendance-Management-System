package http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

    // -----------------
    // getLong tests
    // -----------------

    @Test
    void getLong_nullQuery_returnsNull() {
        assertNull(Query.getLong(null, "id"));
    }

    @Test
    void getLong_blankQuery_returnsNull() {
        assertNull(Query.getLong("   ", "id"));
    }

    @Test
    void getLong_missingKey_returnsNull() {
        assertNull(Query.getLong("a=1&b=2", "id"));
    }

    @Test
    void getLong_validNumber_returnsParsedValue() {
        assertEquals(42L, Query.getLong("id=42", "id"));
        assertEquals(7L, Query.getLong("a=1&id=7&b=2", "id"));
    }

    @Test
    void getLong_trimsWhitespace() {
        assertEquals(99L, Query.getLong("id=  99  ", "id"));
    }

    @Test
    void getLong_invalidNumber_returnsNull() {
        assertNull(Query.getLong("id=abc", "id"));
    }

    @Test
    void getLong_blankValue_returnsNull() {
        assertNull(Query.getLong("id=", "id"));
    }

    @Test
    void getLong_urlEncodedValue() {
        assertEquals(100L, Query.getLong("id=100", "id"));
    }

    // -----------------
    // get tests
    // -----------------

    @Test
    void get_nullQuery_returnsNull() {
        assertNull(Query.get(null, "x"));
    }

    @Test
    void get_missingKey_returnsNull() {
        assertNull(Query.get("a=1&b=2", "x"));
    }

    @Test
    void get_simpleKeyValue() {
        assertEquals("42", Query.get("id=42", "id"));
    }

    @Test
    void get_multipleParams() {
        assertEquals("hello", Query.get("a=1&name=hello&b=2", "name"));
    }

    @Test
    void get_paramWithoutValue_returnsEmptyString() {
        assertEquals("", Query.get("flag", "flag"));
    }

    @Test
    void get_urlEncodedValue_decodedCorrectly() {
        assertEquals("hello world", Query.get("name=hello%20world", "name"));
    }

    @Test
    void get_urlEncodedKey_decodedCorrectly() {
        assertEquals("123", Query.get("user%20id=123", "user id"));
    }

    @Test
    void get_duplicateKeys_lastOneWins() {
        assertEquals("2", Query.get("id=1&id=2", "id"));
    }
}