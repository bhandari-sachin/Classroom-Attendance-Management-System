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

class StudentAttendanceSummaryHandlerTest {

    // -------------------------------------------------------------
    // SUCCESS CASE
    // -------------------------------------------------------------
    @Test
    void summary_returns200AndStats() throws Exception {

        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);

        StudentAttendanceSummaryHandler handler =
                new StudentAttendanceSummaryHandler(jwtService, attendanceService);

        StudentAttendanceSummaryHandler spy = spy(handler);

        // bypass BaseHandler authentication
        doReturn(null).when(spy).requireStudent(any(), any());

        // mock request context
        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);
        when(ctx.getUserId()).thenReturn(1L);
        when(ctx.getClassId()).thenReturn(10L);
        when(ctx.getPeriod()).thenReturn("WEEK");

        // mock service result
        AttendanceStats stats = mock(AttendanceStats.class);
        when(stats.getPresentCount()).thenReturn(5);
        when(stats.getAbsentCount()).thenReturn(2);
        when(stats.getExcusedCount()).thenReturn(1);
        when(stats.getTotalDays()).thenReturn(8);
        when(stats.getAttendanceRate()).thenReturn(0.75);

        when(attendanceService.getStudentStats(1L, 10L, "WEEK"))
                .thenReturn(stats);

        FakeExchange ex = new FakeExchange("GET", "/student/summary", null);

        spy.handleRequest(ex, ctx);

        assertEquals(200, ex.statusCode);

        String body = ex.responseBodyString();
        assertTrue(body.contains("presentCount"));
        assertTrue(body.contains("absentCount"));
        assertTrue(body.contains("attendanceRate"));

        verify(attendanceService).getStudentStats(1L, 10L, "WEEK");
    }

    // -------------------------------------------------------------
    // Fake HttpExchange
    // -------------------------------------------------------------
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