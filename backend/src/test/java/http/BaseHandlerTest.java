package http;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseHandlerTest {

    // ---------------- TEST HANDLER ----------------
    static class TestHandler extends BaseHandler {

        TestHandler(JwtService jwtService) {
            super(jwtService, "GET");
        }

        @Override
        protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
            HttpUtil.send(ex, 200, "OK");
        }
    }

    // ---------------- TESTS ----------------

    @Test
    void allowedMethod_returns200() throws Exception {
        JwtService jwt = mock(JwtService.class);
        TestHandler handler = new TestHandler(jwt);

        HttpExchange ex = mock(HttpExchange.class);
        when(ex.getRequestMethod()).thenReturn("GET");
        when(ex.getRequestURI()).thenReturn(URI.create("/test"));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(ex.getResponseBody()).thenReturn(os);

        handler.handle(ex);

        verify(ex, atLeastOnce()).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwt = mock(JwtService.class);
        TestHandler handler = new TestHandler(jwt);

        HttpExchange ex = mock(HttpExchange.class);
        when(ex.getRequestMethod()).thenReturn("POST");
        when(ex.getRequestURI()).thenReturn(URI.create("/test"));

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(405), anyLong());
    }

    @Test
    void apiException_returns400() throws Exception {
        JwtService jwt = mock(JwtService.class);

        TestHandler handler = new TestHandler(jwt) {
            @Override
            protected void handleRequest(HttpExchange ex, RequestContext ctx) {
                throw new backend.exception.ApiException(400, "Bad request");
            }
        };

        HttpExchange ex = mock(HttpExchange.class);
        when(ex.getRequestMethod()).thenReturn("GET");
        when(ex.getRequestURI()).thenReturn(URI.create("/test"));

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void securityException_returns403() throws Exception {
        JwtService jwt = mock(JwtService.class);

        TestHandler handler = new TestHandler(jwt) {
            @Override
            protected void handleRequest(HttpExchange ex, RequestContext ctx) {
                throw new SecurityException("Forbidden");
            }
        };

        HttpExchange ex = mock(HttpExchange.class);
        when(ex.getRequestMethod()).thenReturn("GET");
        when(ex.getRequestURI()).thenReturn(URI.create("/test"));

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(403), anyLong());
    }

    @Test
    void genericException_returns500() throws Exception {
        JwtService jwt = mock(JwtService.class);

        TestHandler handler = new TestHandler(jwt) {
            @Override
            protected void handleRequest(HttpExchange ex, RequestContext ctx) {
                throw new RuntimeException("Crash");
            }
        };

        HttpExchange ex = mock(HttpExchange.class);
        when(ex.getRequestMethod()).thenReturn("GET");
        when(ex.getRequestURI()).thenReturn(URI.create("/test"));

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(500), anyLong());
    }
}