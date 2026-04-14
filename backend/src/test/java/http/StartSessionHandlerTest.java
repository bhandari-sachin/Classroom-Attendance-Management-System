package http;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import security.JwtService;
import service.SessionService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;

import static org.mockito.Mockito.*;

class StartSessionHandlerTest {

    // ---------------- helper ----------------

    private HttpExchange mockExchange(String body) throws Exception {
        HttpExchange ex = mock(HttpExchange.class);

        when(ex.getRequestMethod()).thenReturn("POST");
        when(ex.getRequestURI()).thenReturn(URI.create("/session/start"));

        ByteArrayInputStream in =
                new ByteArrayInputStream(body.getBytes());

        when(ex.getRequestBody()).thenReturn(in);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(ex.getResponseBody()).thenReturn(out);

        when(ex.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());

        doNothing().when(ex).sendResponseHeaders(anyInt(), anyLong());
        doNothing().when(ex).close();

        return ex;
    }

    // ---------------- tests ----------------

    @Test
    void invalidJson_returns400() throws Exception {
        JwtService jwt = mock(JwtService.class);
        SessionService service = mock(SessionService.class);

        StartSessionHandler handler = new StartSessionHandler(jwt, service);

        HttpExchange ex = mockExchange("invalid-json");

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void missingSessionId_returns400() throws Exception {
        JwtService jwt = mock(JwtService.class);
        SessionService service = mock(SessionService.class);

        StartSessionHandler handler = new StartSessionHandler(jwt, service);

        HttpExchange ex = mockExchange("{\"wrongKey\":1}");

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void success_returns200() throws Exception {
        JwtService jwt = mock(JwtService.class);
        SessionService service = mock(SessionService.class);

        when(service.startSession(10L)).thenReturn("ABC123");

        StartSessionHandler handler = new StartSessionHandler(jwt, service);

        HttpExchange ex = mockExchange("{\"sessionId\":10}");

        handler.handle(ex);

        verify(service).startSession(10L);
        verify(ex).sendResponseHeaders(eq(200), anyLong());
    }
}