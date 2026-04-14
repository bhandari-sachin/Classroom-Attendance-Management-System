package http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

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

    // ---------------- MOCK HELPER ----------------
    private HttpExchange mockExchange(String method, String uri, String authHeader) {
        HttpExchange exchange = mock(HttpExchange.class);

        Headers headers = new Headers();
        if (authHeader != null) {
            headers.add("Authorization", authHeader);
        }

        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(URI.create(uri));
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());

        return exchange;
    }

    // ---------------- TESTS ----------------

    @Test
    void allowedMethod_returns200() throws Exception {
        JwtService jwt = mock(JwtService.class);
        TestHandler handler = new TestHandler(jwt);

        HttpExchange ex = mockExchange("GET", "/test", "Bearer dummy");

        handler.handle(ex);

        verify(ex, atLeastOnce()).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwt = mock(JwtService.class);
        TestHandler handler = new TestHandler(jwt);

        HttpExchange ex = mockExchange("POST", "/test", "Bearer dummy");

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

        HttpExchange ex = mockExchange("GET", "/test", "Bearer dummy");

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

        HttpExchange ex = mockExchange("GET", "/test", "Bearer dummy");

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

        HttpExchange ex = mockExchange("GET", "/test", "Bearer dummy");

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(500), anyLong());
    }
}