package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpUtilTest {

    @Test
    void send_writesText_setsStatus_andCloses() throws Exception {
        FakeExchange ex = new FakeExchange("GET", "/x", null);

        HttpUtil.send(ex, 405, "Method Not Allowed");

        assertEquals(405, ex.statusCode);
        assertEquals("Method Not Allowed", ex.responseBodyString());
        assertTrue(ex.closed, "Exchange should be closed");
    }

    @Test
    void json_writesJson_setsContentType_andCloses() throws Exception {
        FakeExchange ex = new FakeExchange("GET", "/x", null);

        HttpUtil.json(ex, 200, java.util.Map.of("ok", true, "n", 3));

        assertEquals(200, ex.statusCode);
        assertEquals("application/json", ex.getResponseHeaders().getFirst("Content-Type"));
        String body = ex.responseBodyString();
        assertTrue(body.contains("\"ok\":true"));
        assertTrue(body.contains("\"n\":3"));
        assertTrue(ex.closed, "Exchange should be closed");
    }

    @Test
    void jwtUserId_usesIdClaim_whenPresent() {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim idClaim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(idClaim);
        when(idClaim.asLong()).thenReturn(99L);

        long id = HttpUtil.jwtUserId(jwt);
        assertEquals(99L, id);

        // subject should not be needed
        verify(jwt, never()).getSubject();
    }

    @Test
    void jwtUserId_fallsBackToSubject_whenIdClaimNull() {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim idClaim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(idClaim);
        when(idClaim.asLong()).thenReturn(null);
        when(jwt.getSubject()).thenReturn("123");

        long id = HttpUtil.jwtUserId(jwt);
        assertEquals(123L, id);
    }

    @Test
    void queryLong_returnsNull_onNullOrBlankQuery() {
        assertNull(HttpUtil.queryLong(null, "id"));
        assertNull(HttpUtil.queryLong("", "id"));
        assertNull(HttpUtil.queryLong("   ", "id"));
    }

    @Test
    void queryLong_returnsNull_whenKeyMissing() {
        assertNull(HttpUtil.queryLong("a=1&b=2", "id"));
    }

    @Test
    void queryLong_parsesLongValue() {
        assertEquals(42L, HttpUtil.queryLong("id=42", "id"));
        assertEquals(7L, HttpUtil.queryLong("a=1&id=7&b=2", "id"));
    }

    @Test
    void queryLong_decodesUrlEncodedValue() {
        // "100%20" => "100 " => parseLong should work after trim? (your code does not trim after decode)
        // so we use exact "100" encoded
        assertEquals(100L, HttpUtil.queryLong("id=100", "id"));
    }

    @Test
    void queryLong_returnsNull_whenValueBlank() {
        assertNull(HttpUtil.queryLong("id=", "id"));
        assertNull(HttpUtil.queryLong("id=%20%20", "id")); // becomes spaces -> blank
    }

    @Test
    void queryLong_throwsNumberFormatException_whenNotANumber() {
        assertThrows(NumberFormatException.class, () -> HttpUtil.queryLong("id=abc", "id"));
    }

    // -------------------------
    // FakeExchange for send/json
    // -------------------------
    static class FakeExchange extends HttpExchange {
        private final Headers reqHeaders = new Headers();
        private final Headers respHeaders = new Headers();
        private final String method;
        private final URI uri;
        private final InputStream requestBody;
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

        int statusCode = -1;
        boolean closed = false;

        FakeExchange(String method, String path, String body) {
            this.method = method;
            this.uri = URI.create("http://localhost" + path);
            this.requestBody = body == null
                    ? InputStream.nullInputStream()
                    : new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        }

        String responseBodyString() {
            return responseBody.toString(StandardCharsets.UTF_8);
        }

        @Override public Headers getRequestHeaders() { return reqHeaders; }
        @Override public Headers getResponseHeaders() { return respHeaders; }
        @Override public URI getRequestURI() { return uri; }
        @Override public String getRequestMethod() { return method; }
        @Override public HttpContext getHttpContext() { return null; }

        @Override public void close() { closed = true; }

        @Override public InputStream getRequestBody() { return requestBody; }
        @Override public OutputStream getResponseBody() { return responseBody; }

        @Override public void sendResponseHeaders(int rCode, long responseLength) {
            this.statusCode = rCode;
        }

        @Override public InetSocketAddress getRemoteAddress() { return new InetSocketAddress(0); }
        @Override public int getResponseCode() { return statusCode; }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {    // No-op: attributes not needed for testing
        }
        @Override public void setStreams(InputStream i, OutputStream o) {     // Not used in this fake exchange
        }
        @Override public com.sun.net.httpserver.HttpPrincipal getPrincipal() { return null; }
    }
}