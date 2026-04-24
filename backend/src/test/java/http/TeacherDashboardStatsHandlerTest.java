package http;

import com.sun.net.httpserver.*;
import config.AttendanceSQL;
import config.ClassSQL;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherDashboardStatsHandlerTest {

    // -------------------------------------------------------------
    // SUCCESS CASE
    // -------------------------------------------------------------
    @Test
    void dashboardStats_returns200AndStats() throws Exception {

        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        TeacherDashboardStatsHandler handler =
                new TeacherDashboardStatsHandler(jwtService, classSQL, attendanceSQL);

        TeacherDashboardStatsHandler spy = spy(handler);

        // bypass BaseHandler authentication
        doReturn(null).when(spy).requireTeacherOrAdmin(any(), any());

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);
        when(ctx.getUserId()).thenReturn(100L);

        // -------------------------
        // Mock SQL responses
        // -------------------------
        when(classSQL.countForTeacher(100L)).thenReturn(5);
        when(classSQL.countStudentsForTeacher(100L)).thenReturn(120);

        when(attendanceSQL.countTodayForTeacher(100L, "PRESENT")).thenReturn(80);
        when(attendanceSQL.countTodayForTeacher(100L, "ABSENT")).thenReturn(10);

        FakeExchange ex = new FakeExchange("GET", "/teacher/dashboard", null);

        spy.handleRequest(ex, ctx);

        assertEquals(200, ex.statusCode);

        String body = ex.responseBodyString();

        assertTrue(body.contains("totalClasses"));
        assertTrue(body.contains("totalStudents"));
        assertTrue(body.contains("presentToday"));
        assertTrue(body.contains("absentToday"));

        verify(classSQL).countForTeacher(100L);
        verify(classSQL).countStudentsForTeacher(100L);
        verify(attendanceSQL).countTodayForTeacher(100L, "PRESENT");
        verify(attendanceSQL).countTodayForTeacher(100L, "ABSENT");
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
            // no-op for testing
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