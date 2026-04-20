package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Request;
import config.ClassSQL;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentAttendanceFiltersHandlerTest {

    @Test
    void shouldReturn200_withClassesAndPeriods() throws Exception {

        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        StudentAttendanceFiltersHandler handler =
                new StudentAttendanceFiltersHandler(jwtService, classSQL);

        StudentAttendanceFiltersHandler spy = spy(handler);

        doReturn(null).when(spy).requireStudent(any(), any());

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);
        when(ctx.getUserId()).thenReturn(1L);

        when(classSQL.listClassesForStudent(1L))
                .thenReturn(List.of(
                        Map.of("id", 1, "name", "Math"),
                        Map.of("id", 2, "name", "Science")
                ));

        FakeExchange ex = new FakeExchange("GET", "/student/filters", null);

        spy.handleRequest(ex, ctx);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("classes"));
        assertTrue(ex.responseBodyString().contains("periods"));
        assertTrue(ex.responseBodyString().contains("THIS_MONTH"));
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

        @Override public void close() {     // No-op: not needed for unit testing
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
        @Override public void setStreams(InputStream i, OutputStream o) {     // Not used in this fake exchange
        }
        @Override public com.sun.net.httpserver.HttpContext getHttpContext() { return null; }
        @Override public com.sun.net.httpserver.Headers getRequestHeaders() { return new com.sun.net.httpserver.Headers(); }

        @Override
        public Request with(String headerName, List<String> headerValues) {
            return super.with(headerName, headerValues);
        }

        @Override public com.sun.net.httpserver.Headers getResponseHeaders() { return new com.sun.net.httpserver.Headers(); }
        @Override public com.sun.net.httpserver.HttpPrincipal getPrincipal() { return null; }
    }
}