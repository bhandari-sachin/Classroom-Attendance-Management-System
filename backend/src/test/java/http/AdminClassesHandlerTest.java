package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminClassesHandlerTest {

    @Test
    void authMissingHeader_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        var handler = new AdminClassesHandler(jwtService, classSQL);

        var ex = new FakeExchange("GET", "/admin/classes", null);
        // no Authorization header

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
    }

    @Test
    void forbiddenRole_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        var handler = new AdminClassesHandler(jwtService, classSQL);

        var ex = new FakeExchange("GET", "/admin/classes", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("STUDENT"); // not ADMIN
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(401, ex.statusCode); // your handler maps SecurityException -> 401
        assertTrue(ex.responseBodyString().contains("Forbidden for role"));
    }



    // ---- helpers ----

    private static DecodedJWT jwtWithRole(String role) {
        DecodedJWT jwt = mock(DecodedJWT.class);

        Claim roleClaim = mock(Claim.class);
        when(roleClaim.asString()).thenReturn(role);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        // optional: id for other endpoints
        Claim idClaim = mock(Claim.class);
        when(idClaim.asLong()).thenReturn(1L);
        when(jwt.getClaim("id")).thenReturn(idClaim);

        when(jwt.getSubject()).thenReturn("1");
        return jwt;
    }

    /**
     * Minimal HttpExchange that captures status code + body.
     */
    static class FakeExchange extends HttpExchange {
        private final Headers reqHeaders = new Headers();
        private final Headers respHeaders = new Headers();
        private final String method;
        private final URI uri;
        private final InputStream requestBody;
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

        int statusCode = -1;

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
        @Override public void close() { }
        @Override public InputStream getRequestBody() { return requestBody; }
        @Override public OutputStream getResponseBody() { return responseBody; }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) {
            this.statusCode = rCode;
        }

        @Override public InetSocketAddress getRemoteAddress() { return new InetSocketAddress(0); }
        @Override public int getResponseCode() { return statusCode; }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) { }
        @Override public void setStreams(InputStream i, OutputStream o) { }
        @Override public com.sun.net.httpserver.HttpPrincipal getPrincipal() { return null; }
    }
}