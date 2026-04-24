package http;

import backend.exception.ApiException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.*;
import config.AttendanceSQL;
import dto.AttendanceView;
import http.BaseHandler.RequestContext;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAttendanceReportsHandlerTest {

    @Test
    void missingClassId_throws400ApiException() {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        AdminAttendanceReportsHandler handler =
                new AdminAttendanceReportsHandler(jwtService, attendanceSQL);
        AdminAttendanceReportsHandler spyHandler = spy(handler);

        doReturn(null).when(spyHandler).requireAdmin(any(), any());

        FakeExchange ex = new FakeExchange("GET", "/admin/attendance", null);
        ex.getRequestHeaders().add("Authorization", "Bearer test-token");

        // ✅ FIX: inline JWT mock (no helper method)
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim role = mock(Claim.class);
        when(role.asString()).thenReturn("ADMIN");
        when(jwt.getClaim("role")).thenReturn(role);

        when(jwtService.verify("test-token")).thenReturn(jwt);

        RequestContext ctx = mock(RequestContext.class);
        when(ctx.getClassId()).thenReturn(null);

        ApiException exThrown =
                assertThrows(ApiException.class, () -> spyHandler.handleRequest(ex, ctx));

        assertEquals(400, exThrown.getStatus());
        assertTrue(exThrown.getMessage().contains("classId is required"));
    }

    @Test
    void validRequest_callsSqlAndReturns200() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        AdminAttendanceReportsHandler handler =
                new AdminAttendanceReportsHandler(jwtService, attendanceSQL);
        AdminAttendanceReportsHandler spyHandler = spy(handler);

        doReturn(null).when(spyHandler).requireAdmin(any(), any());

        FakeExchange ex = new FakeExchange("GET", "/admin/attendance?lang=en", null);
        ex.getRequestHeaders().add("Authorization", "Bearer test-token");

        // ✅ FIX: inline JWT mock here too
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim role = mock(Claim.class);
        when(role.asString()).thenReturn("ADMIN");
        when(jwt.getClaim("role")).thenReturn(role);

        when(jwtService.verify("test-token")).thenReturn(jwt);

        RequestContext ctx = mock(RequestContext.class);
        when(ctx.getClassId()).thenReturn(10L);
        when(ctx.getPeriod()).thenReturn("WEEK");
        when(ctx.getQuery("search", "")).thenReturn("");
        when(ctx.getQuery("lang", "en")).thenReturn("en");

        List<AttendanceView> rows = List.of(mock(AttendanceView.class));
        when(attendanceSQL.getAdminAttendanceReport(10L, "WEEK", "", "en"))
                .thenReturn(rows);

        spyHandler.handleRequest(ex, ctx);

        assertEquals(200, ex.statusCode);
        assertFalse(ex.responseBodyString().isBlank());
    }

    // ---------------------------------------------------------------------
    // Fake HttpExchange
    // ---------------------------------------------------------------------
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
        @Override public void close() {
            // No-op for testing
        }
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
        @Override public void setAttribute(String name, Object value) {
            // No-op for testing
        }
        @Override public void setStreams(InputStream i, OutputStream o) {
            // No-op for testing
        }
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}