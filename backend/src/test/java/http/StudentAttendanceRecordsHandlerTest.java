package http;

import com.sun.net.httpserver.HttpExchange;
import dto.AttendanceView;
import org.junit.jupiter.api.Test;
import security.JwtService;
import service.AttendanceService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentAttendanceRecordsHandlerTest {

    @Test
    void shouldReturn200_withAttendanceRecords() throws Exception {

        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);

        StudentAttendanceRecordsHandler handler =
                new StudentAttendanceRecordsHandler(jwtService, attendanceService);

        StudentAttendanceRecordsHandler spy = spy(handler);

        doReturn(null).when(spy).requireStudent(any(), any());

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);
        when(ctx.getUserId()).thenReturn(1L);
        when(ctx.getClassId()).thenReturn(10L);
        when(ctx.getPeriod()).thenReturn("WEEK");
        when(ctx.getQuery("lang", "en")).thenReturn("en");

        List<AttendanceView> mockList = List.of(mock(AttendanceView.class));

        when(attendanceService.getStudentAttendanceViews(1L, 10L, "WEEK", "en"))
                .thenReturn(mockList);

        FakeExchange ex = new FakeExchange("GET", "/student/attendance", null);

        spy.handleRequest(ex, ctx);

        assertEquals(200, ex.statusCode);
        assertFalse(ex.responseBodyString().isBlank());

        verify(attendanceService, times(1))
                .getStudentAttendanceViews(1L, 10L, "WEEK", "en");
    }

    // -------------------------
    // Fake HttpExchange
    // -------------------------
    static class FakeExchange extends HttpExchange {

        int statusCode = -1;
        private final String method;
        private final URI uri;
        private final InputStream requestBody;
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

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

        @Override public String getRequestMethod() { return method; }
        @Override public URI getRequestURI() { return uri; }
        @Override public InputStream getRequestBody() { return requestBody; }
        @Override public OutputStream getResponseBody() { return responseBody; }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) {
            this.statusCode = rCode;
        }

        @Override public void close() {    // No-op: not needed for unit testing
        }

        @Override public InetSocketAddress getRemoteAddress() { return new InetSocketAddress(0); }

        @Override
        public int getResponseCode() {
            return 0;
        }

        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }

        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {    // No-op: attributes not needed for testing
        }
        @Override public void setStreams(InputStream i, OutputStream o) {    // Not used in this fake exchange
        }
        @Override public com.sun.net.httpserver.HttpContext getHttpContext() { return null; }
        @Override public com.sun.net.httpserver.Headers getRequestHeaders() { return new com.sun.net.httpserver.Headers(); }
        @Override public com.sun.net.httpserver.Headers getResponseHeaders() { return new com.sun.net.httpserver.Headers(); }
        @Override public com.sun.net.httpserver.HttpPrincipal getPrincipal() { return null; }
    }
}