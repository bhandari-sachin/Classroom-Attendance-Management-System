package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import dto.AttendanceStats;
import org.junit.jupiter.api.Test;
import security.JwtService;
import service.AttendanceService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminStatsHandlerTest {

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new AdminStatsHandler(jwtService, attendanceService);

        var ex = new FakeExchange("POST", "/admin/stats", null);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
    }

    @Test
    void get_withAdmin_returns200_andJson() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new AdminStatsHandler(jwtService, attendanceService);

        var ex = new FakeExchange("GET", "/admin/stats", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("ADMIN");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        // If AttendanceStats has a constructor, use it; otherwise mock it.
        AttendanceStats stats = mock(AttendanceStats.class);
        when(attendanceService.getOverallStats()).thenReturn(stats);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.getResponseHeaders().getFirst("Content-Type").contains("application/json"));

        String body = ex.responseBodyString();
        assertNotNull(body);
        assertFalse(body.isBlank());
        assertFalse(body.contains("\"error\""));
    }

    @Test
    void get_missingAuthorization_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new AdminStatsHandler(jwtService, attendanceService);

        var ex = new FakeExchange("GET", "/admin/stats", null);
        // no Authorization header

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verify(attendanceService, never()).getOverallStats();
    }

    @Test
    void get_serviceThrows_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new AdminStatsHandler(jwtService, attendanceService);

        var ex = new FakeExchange("GET", "/admin/stats", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("ADMIN");
        when(jwtService.verify("test-token")).thenReturn(jwt);
        when(attendanceService.getOverallStats()).thenThrow(new RuntimeException("DB down"));

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
    }

    // ---- helpers ----

    private static DecodedJWT jwtWithRole(String role) {
        DecodedJWT jwt = mock(DecodedJWT.class);

        Claim roleClaim = mock(Claim.class);
        when(roleClaim.asString()).thenReturn(role);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        Claim idClaim = mock(Claim.class);
        when(idClaim.asLong()).thenReturn(1L);
        when(jwt.getClaim("id")).thenReturn(idClaim);

        when(jwt.getSubject()).thenReturn("1");
        return jwt;
    }

    /**
     * Minimal HttpExchange that captures status code + response body + headers.
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