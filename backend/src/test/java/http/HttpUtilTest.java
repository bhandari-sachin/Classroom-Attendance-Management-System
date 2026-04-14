package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpUtilTest {

    // ---------------- jwtUserId ----------------

    @Test
    void jwtUserId_fromClaim_returnsValue() {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(claim);
        when(claim.asLong()).thenReturn(10L);

        long result = HttpUtil.jwtUserId(jwt);

        assertEquals(10L, result);
    }

    @Test
    void jwtUserId_fallbackToSubject() {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(claim);
        when(claim.asLong()).thenReturn(null);
        when(jwt.getSubject()).thenReturn("25");

        long result = HttpUtil.jwtUserId(jwt);

        assertEquals(25L, result);
    }

    // ---------------- queryLong ----------------

    @Test
    void queryLong_validValue_returnsParsedLong() {
        String query = "classId=10&name=test";

        Long result = HttpUtil.queryLong(query, "classId");

        assertEquals(10L, result);
    }

    @Test
    void queryLong_missingKey_returnsNull() {
        String query = "name=test";

        Long result = HttpUtil.queryLong(query, "classId");

        assertNull(result);
    }

    @Test
    void queryLong_blankValue_returnsNull() {
        String query = "classId=&name=test";

        Long result = HttpUtil.queryLong(query, "classId");

        assertNull(result);
    }

    // ---------------- queryString ----------------

    @Test
    void queryString_validValue_returnsString() {
        String query = "name=John&class=math";

        String result = HttpUtil.queryString(query, "name");

        assertEquals("John", result);
    }

    @Test
    void queryString_missingKey_returnsNull() {
        String query = "class=math";

        String result = HttpUtil.queryString(query, "name");

        assertNull(result);
    }

    @Test
    void queryString_blankValue_returnsNull() {
        String query = "name=&class=math";

        String result = HttpUtil.queryString(query, "name");

        assertNull(result);
    }
}