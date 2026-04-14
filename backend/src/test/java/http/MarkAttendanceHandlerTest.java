package http;

import backend.exception.ApiException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.*;
import org.junit.jupiter.api.Test;
import security.JwtService;
import service.AttendanceService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarkAttendanceHandlerTest {

    // -------------------------------------------------------------
    // Mock ADMIN JWT (safe inline version - avoids UnfinishedStubbing)
    // -------------------------------------------------------------
    private DecodedJWT mockStudentJwt() {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim role = mock(Claim.class);
        when(role.asString()).thenReturn("STUDENT");
        when(jwt.getClaim("role")).thenReturn(role);
        return jwt;
    }

    // -------------------------------------------------------------
    // SUCCESS CASE
    // -------------------------------------------------------------
    @Test
    void markAttendance_success_returns200() throws Exception {

        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);

        MarkAttendanceHandler handler = new MarkAttendanceHandler(jwtService, attendanceService);
        MarkAttendanceHandler spy = spy(handler);

        // bypass authentication (requireStudent)
        doReturn(null).when(spy).requireStudent(any(), any());

        FakeExchange ex = new FakeExchange("POST", "/attendance/mark",
                "{\"code\":\"ABC123\"}");

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);
        when(ctx.getUserId()).thenReturn(1L);

        spy.handleRequest(ex, ctx);

        verify(attendanceService).markByCode(1L, "ABC123");

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("attendance marked"));
    }

    // -------------------------------------------------------------
    // INVALID JSON
    // -------------------------------------------------------------
    @Test
    void markAttendance_invalidJson_returns400() throws Exception {

        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);

        MarkAttendanceHandler handler = new MarkAttendanceHandler(jwtService, attendanceService);
        MarkAttendanceHandler spy = spy(handler);

        doReturn(null).when(spy).requireStudent(any(), any());

        FakeExchange ex = new FakeExchange("POST", "/attendance/mark", "NOT_JSON");

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);

        assertThrows(ApiException.class, () -> spy.handleRequest(ex, ctx));
    }

    // -------------------------------------------------------------
    // MISSING CODE
    // -------------------------------------------------------------
    @Test
    void markAttendance_missingCode_throws400() throws Exception {

        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);

        MarkAttendanceHandler handler = new MarkAttendanceHandler(jwtService, attendanceService);
        MarkAttendanceHandler spy = spy(handler);

        doReturn(null).when(spy).requireStudent(any(), any());

        FakeExchange ex = new FakeExchange("POST", "/attendance/mark",
                "{\"code\":\"\"}");

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);
        when(ctx.getUserId()).thenReturn(1L);

        ApiException exThrown =
                assertThrows(ApiException.class, () -> spy.handleRequest(ex, ctx));

        assertEquals(400, exThrown.getStatus());
        assertTrue(exThrown.getMessage().contains("Attendance code required"));
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