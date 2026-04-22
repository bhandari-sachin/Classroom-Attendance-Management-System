package http;

import backend.exception.ApiException;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import security.JwtService;
import service.SessionService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StartSessionHandlerTest {

    // -------------------------
    // SUCCESS CASE
    // -------------------------
    @Test
    void shouldReturn200_whenSessionStartsSuccessfully() throws Exception {

        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);

        StartSessionHandler handler = new StartSessionHandler(jwtService, sessionService);
        StartSessionHandler spy = spy(handler);

        doReturn(null).when(spy).requireTeacher(any(), any());

        when(sessionService.startSession(10L)).thenReturn("ABC123");

        FakeExchange ex = new FakeExchange("POST", "/start-session",
                "{\"sessionId\":10}");

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);

        spy.handleRequest(ex, ctx);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Session started successfully"));
        assertTrue(ex.responseBodyString().contains("ABC123"));
    }

    // -------------------------
    // MISSING SESSION ID -> 400
    // -------------------------
    @Test
    void shouldThrow400_whenSessionIdMissing() {

        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);

        StartSessionHandler handler = new StartSessionHandler(jwtService, sessionService);
        StartSessionHandler spy = spy(handler);

        doReturn(null).when(spy).requireTeacher(any(), any());

        FakeExchange ex = new FakeExchange("POST", "/start-session",
                "{}");

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);

        ApiException exThrown =
                assertThrows(ApiException.class, () -> spy.handleRequest(ex, ctx));

        assertEquals(400, exThrown.getStatus());
        assertTrue(exThrown.getMessage().contains("sessionId is required"));
    }

    // -------------------------
    // INVALID JSON -> 400
    // -------------------------
    @Test
    void shouldThrow400_whenInvalidJson() {

        JwtService jwtService = mock(JwtService.class);
        SessionService sessionService = mock(SessionService.class);

        StartSessionHandler handler = new StartSessionHandler(jwtService, sessionService);
        StartSessionHandler spy = spy(handler);

        doReturn(null).when(spy).requireTeacher(any(), any());

        FakeExchange ex = new FakeExchange("POST", "/start-session",
                "{invalid-json");

        BaseHandler.RequestContext ctx = mock(BaseHandler.RequestContext.class);

        ApiException exThrown =
                assertThrows(ApiException.class, () -> spy.handleRequest(ex, ctx));

        assertEquals(400, exThrown.getStatus());
        assertTrue(exThrown.getMessage().contains("Invalid JSON"));
    }

    // -------------------------
    // FAKE HTTP EXCHANGE
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

        @Override public void close() {            // No-op: not needed for tests
        }

        @Override public InetSocketAddress getRemoteAddress() { return new InetSocketAddress(0); }

        @Override
        public int getResponseCode() {
            return 0;
        }

        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }

        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {            // No-op: not needed
        }
        @Override public void setStreams(InputStream i, OutputStream o) {            // Not used in this fake implementation
        }
        @Override public com.sun.net.httpserver.HttpContext getHttpContext() { return null; }
        @Override public com.sun.net.httpserver.Headers getRequestHeaders() { return new com.sun.net.httpserver.Headers(); }
        @Override public com.sun.net.httpserver.Headers getResponseHeaders() { return new com.sun.net.httpserver.Headers(); }
        @Override public com.sun.net.httpserver.HttpPrincipal getPrincipal() { return null; }
    }
}