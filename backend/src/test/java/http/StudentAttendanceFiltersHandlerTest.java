package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.ClassSQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import security.Auth;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentAttendanceFiltersHandlerTest {

    private JwtService jwtService;
    private ClassSQL classSQL;
    private StudentAttendanceFiltersHandler handler;
    private HttpExchange exchange;

    private ByteArrayOutputStream responseBody;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        classSQL = mock(ClassSQL.class);

        handler = Mockito.spy(new StudentAttendanceFiltersHandler(jwtService, classSQL));

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    // -----------------------------
    // Helpers
    // -----------------------------

    private void request(String method, String path) throws Exception {
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(new URI(path));
    }

    private Map<?, ?> jsonResponse() throws Exception {
        return om.readValue(responseBody.toByteArray(), Map.class);
    }

    // -----------------------------
    // SUCCESS CASE
    // -----------------------------

    @Test
    void success_returnsClassesAndPeriods() throws Exception {
        request("GET", "/api/student/attendance/filters");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockDecodedJwt(10L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);
            when(classSQL.listClassesForStudent(10L))
                    .thenReturn(List.of(
                            Map.of("id", 1, "name", "Math")
                    ));

            handler.handle(exchange);

            verify(classSQL).listClassesForStudent(10L);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());

            var json = jsonResponse();

            assertTrue(json.containsKey("classes"));
            assertTrue(json.containsKey("periods"));

            List<?> periods = (List<?>) json.get("periods");
            assertEquals(4, periods.size());
        }
    }

    // -----------------------------
    // EMPTY DATA
    // -----------------------------

    @Test
    void emptyClasses_returnsEmptyList() throws Exception {
        request("GET", "/api/student/attendance/filters");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockDecodedJwt(5L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(classSQL.listClassesForStudent(5L))
                    .thenReturn(List.of());

            handler.handle(exchange);

            var json = jsonResponse();
            var classes = (List<?>) json.get("classes");

            assertTrue(classes.isEmpty());
        }
    }

    // -----------------------------
    // METHOD NOT ALLOWED
    // -----------------------------

    @Test
    void wrongMethod_returns405() throws Exception {
        request("POST", "/api/student/attendance/filters");

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
        assertTrue(responseBody.toString().contains("Method Not Allowed"));
    }

    // -----------------------------
    // AUTH FAILURE
    // -----------------------------

    @Test
    void authFails_returns403() throws Exception {
        request("GET", "/api/student/attendance/filters");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenThrow(new SecurityException("Forbidden"));

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(403), anyLong());
        assertTrue(responseBody.toString().contains("Forbidden"));
    }

    // -----------------------------
    // SERVICE FAILURE
    // -----------------------------

    @Test
    void serviceThrows_returns500() throws Exception {
        request("GET", "/api/student/attendance/filters");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockDecodedJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(classSQL.listClassesForStudent(anyLong()))
                    .thenThrow(new RuntimeException("DB failure"));

            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(500), anyLong());
        }
    }

    // -----------------------------
    // Helper JWT
    // -----------------------------

    private DecodedJWT mockDecodedJwt(Long id) {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(claim);
        when(claim.asLong()).thenReturn(id);

        return jwt;
    }
}