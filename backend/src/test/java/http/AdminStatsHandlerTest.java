package http;

import com.sun.net.httpserver.*;
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
    void shouldReturn200AndStats_whenAdminAccessIsValid() throws Exception {
        // Arrange
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);

        AdminStatsHandler handler = new AdminStatsHandler(jwtService, attendanceService);
        AdminStatsHandler spyHandler = spy(handler);

        // ✅ FIX: requireAdmin is NOT void → use doReturn()
        doReturn(null).when(spyHandler).requireAdmin(any(), any());

        AttendanceStats stats = mock(AttendanceStats.class);
        when(attendanceService.getOverallStats()).thenReturn(stats);

        FakeExchange ex = new FakeExchange();
        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);

        // Act
        spyHandler.handleRequest(ex, ctx);

        // Assert
        assertEquals(200, ex.statusCode);
        assertFalse(ex.responseBodyString().isBlank());

        verify(attendanceService, times(1)).getOverallStats();
    }

    // ---------------------------------------------------------------------
    // Sonar-compliant Fake HttpExchange
    // ---------------------------------------------------------------------
    static class FakeExchange extends HttpExchange {
        private final Headers reqHeaders = new Headers();
        private final Headers respHeaders = new Headers();
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        int statusCode = -1;

        String responseBodyString() {
            return responseBody.toString(StandardCharsets.UTF_8);
        }

        @Override public Headers getRequestHeaders() { return reqHeaders; }
        @Override public Headers getResponseHeaders() { return respHeaders; }
        @Override public URI getRequestURI() { return URI.create("http://localhost/admin/stats"); }
        @Override public String getRequestMethod() { return "GET"; }

        @Override
        public HttpContext getHttpContext() {
            // Not required for unit testing
            return null;
        }

        @Override
        public void close() {
            // No-op: not needed for tests
        }

        @Override
        public InputStream getRequestBody() {
            return InputStream.nullInputStream();
        }

        @Override
        public OutputStream getResponseBody() {
            return responseBody;
        }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) {
            this.statusCode = rCode;
        }

        @Override public InetSocketAddress getRemoteAddress() { return new InetSocketAddress(0); }
        @Override public int getResponseCode() { return statusCode; }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }

        @Override
        public Object getAttribute(String name) {
            // Attributes not used in this test
            return null;
        }

        @Override
        public void setAttribute(String name, Object value) {
            // No-op: not needed
        }

        @Override
        public void setStreams(InputStream i, OutputStream o) {
            // Not used in this fake implementation
        }

        @Override
        public HttpPrincipal getPrincipal() {
            // Authentication handled via JwtService, not HttpExchange
            return null;
        }
    }
}