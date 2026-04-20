package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import config.ClassSQL;
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

class AdminClassesHandlerTest {

    {
        new ObjectMapper();
    }

    // -------------------------------------------------------------
    // GET TEST
    // -------------------------------------------------------------
    @Test
    void getClasses_returns200AndList() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        AdminClassesHandler handler = new AdminClassesHandler(jwtService, classSQL);
        AdminClassesHandler spyHandler = spy(handler);

        doReturn(null).when(spyHandler).requireAdmin(any(), any());

        FakeExchange ex = new FakeExchange("GET", "/api/admin/classes", null);
        ex.getRequestHeaders().add("Authorization", "Bearer test-token");

        // ✅ INLINE JWT MOCK (FIX)
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim role = mock(Claim.class);
        when(role.asString()).thenReturn("ADMIN");
        when(jwt.getClaim("role")).thenReturn(role);

        when(jwtService.verify("test-token")).thenReturn(jwt);

        RequestContext ctx = mock(RequestContext.class);

        ClassSQL.ClassView cv =
                new ClassSQL.ClassView(
                        1L,
                        "MATH101",
                        "Math",
                        "teacher@test.com",
                        "Fall",
                        "2025",
                        30
                );

        when(classSQL.listAllForAdmin()).thenReturn(List.of(cv));

        spyHandler.handleRequest(ex, ctx);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("MATH101"));
    }

    // -------------------------------------------------------------
    // POST TEST
    // -------------------------------------------------------------
    @Test
    void postClasses_createsClassAndReturns201() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        AdminClassesHandler handler = new AdminClassesHandler(jwtService, classSQL);
        AdminClassesHandler spyHandler = spy(handler);

        doReturn(null).when(spyHandler).requireAdmin(any(), any());

        FakeExchange ex = new FakeExchange("POST", "/api/admin/classes", """
                {
                  "classCode": "MATH101",
                  "name": "Math",
                  "teacherEmail": "teacher@test.com"
                }
                """);

        ex.getRequestHeaders().add("Authorization", "Bearer test-token");

        // ✅ INLINE JWT MOCK (FIX)
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim role = mock(Claim.class);
        when(role.asString()).thenReturn("ADMIN");
        when(jwt.getClaim("role")).thenReturn(role);

        when(jwtService.verify("test-token")).thenReturn(jwt);

        RequestContext ctx = mock(RequestContext.class);

        when(classSQL.findTeacherIdByEmail("teacher@test.com")).thenReturn(5L);
        when(classSQL.createClass(any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(99L);

        spyHandler.handleRequest(ex, ctx);

        assertEquals(201, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("created"));
        assertTrue(ex.responseBodyString().contains("99"));
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
        @Override public void close() {     // No-op: not needed for unit testing
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
        @Override public void setAttribute(String name, Object value) {    // Attributes are not used in this fake implementation
        }
        @Override public void setStreams(InputStream i, OutputStream o) {    // Not used in this fake exchange
        }
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}