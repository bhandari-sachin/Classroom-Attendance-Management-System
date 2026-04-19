package http;

import com.sun.net.httpserver.*;
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

class TeacherClassesHandlerTest {

    // -------------------------------------------------------------
    // SUCCESS CASE
    // -------------------------------------------------------------
    @Test
    void teacherClasses_returns200AndList() throws Exception {

        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        TeacherClassesHandler handler =
                new TeacherClassesHandler(jwtService, classSQL);

        TeacherClassesHandler spy = spy(handler);

        // bypass BaseHandler auth
        doReturn(null).when(spy).requireTeacherOrAdmin(any(), any());

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);
        when(ctx.getUserId()).thenReturn(42L);

        List<Map<String, Object>> mockList = List.of(
                Map.of("id", 1, "classCode", "MATH101"),
                Map.of("id", 2, "classCode", "PHY101")
        );

        when(classSQL.listForTeacher(42L)).thenReturn(mockList);

        FakeExchange ex = new FakeExchange("GET", "/teacher/classes", null);

        spy.handleRequest(ex, ctx);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("MATH101"));
        assertTrue(ex.responseBodyString().contains("PHY101"));

        verify(classSQL).listForTeacher(42L);
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
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}