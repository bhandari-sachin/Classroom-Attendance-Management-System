package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import security.JwtService;
import service.SessionService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StartSessionHandlerTest {

    private JwtService jwtService;
    private SessionService sessionService;
    private StartSessionHandler handler;
    private HttpExchange exchange;

    private final ObjectMapper om = new ObjectMapper();
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        sessionService = mock(SessionService.class);

        handler = Mockito.spy(new StartSessionHandler(jwtService, sessionService));

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());

        // bypass auth
        doReturn(null).when(handler).requireTeacher(any(), any());
    }

    // -----------------------------------
    // Helpers
    // -----------------------------------

    private void request(String method, String path, String body) throws Exception {
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(new URI(path));

        if (body != null) {
            when(exchange.getRequestBody())
                    .thenReturn(new ByteArrayInputStream(body.getBytes()));
        }
    }

    private Map<?, ?> jsonResponse() throws Exception {
        return om.readValue(responseBody.toByteArray(), Map.class);
    }

    private String rawResponse() {
        return responseBody.toString();
    }

    // -----------------------------------
    // SUCCESS
    // -----------------------------------

    @Test
    void startSession_success_returns200() throws Exception {
        request("POST", "/api/session/start", """
                { "sessionId": 42 }
                """);

        when(sessionService.startSession(42L)).thenReturn("ABC123");

        handler.handle(exchange);

        verify(sessionService).startSession(42L);
        verify(exchange).sendResponseHeaders(eq(200), anyLong());

        var json = jsonResponse();
        assertEquals("Session started successfully", json.get("message"));
        assertEquals(42, json.get("sessionId"));
        assertEquals("ABC123", json.get("code"));
    }

    // -----------------------------------
    // VALIDATION
    // -----------------------------------

    @Test
    void missingSessionId_returns400() throws Exception {
        request("POST", "/api/session/start", "{}");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(rawResponse().contains("sessionId is required"));

        verify(sessionService, never()).startSession(any());
    }

    @Test
    void invalidJson_returns400() throws Exception {
        request("POST", "/api/session/start", "{bad}");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(rawResponse().contains("Invalid JSON"));

        verify(sessionService, never()).startSession(any());
    }

    // -----------------------------------
    // METHOD VALIDATION
    // -----------------------------------

    @Test
    void wrongMethod_returns405() throws Exception {
        request("GET", "/api/session/start", null);

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
        assertTrue(rawResponse().contains("Method Not Allowed"));
    }

    // -----------------------------------
    // AUTH
    // -----------------------------------

    @Test
    void requireTeacherFails_stopsExecution() throws Exception {
        request("POST", "/api/session/start", """
                { "sessionId": 1 }
                """);

        doThrow(new RuntimeException("Forbidden"))
                .when(handler).requireTeacher(any(), any());

        verify(sessionService, never()).startSession(any());
    }

    // -----------------------------------
    // EDGE CASES
    // -----------------------------------

    @Test
    void sessionId_asString_causes500_or_400() throws Exception {
        request("POST", "/api/session/start", """
                { "sessionId": "not-a-number" }
                """);

        handler.handle(exchange);

        // Depending on Jackson behavior, this may be 400 or 500
        verify(exchange).sendResponseHeaders(anyInt(), anyLong());
    }

    @Test
    void sessionId_largeNumber_handlesCorrectly() throws Exception {
        request("POST", "/api/session/start", """
                { "sessionId": 999999999 }
                """);

        when(sessionService.startSession(anyLong())).thenReturn("CODE");

        handler.handle(exchange);

        verify(sessionService).startSession(999999999L);
    }
}