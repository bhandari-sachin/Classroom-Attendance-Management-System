package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import security.Auth;
import security.JwtService;
import service.AttendanceService;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TeacherMarkAttendanceHandlerTest {

    private JwtService jwtService;
    private AttendanceService attendanceService;
    private TeacherMarkAttendanceHandler handler;

    private HttpExchange exchange;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        attendanceService = mock(AttendanceService.class);

        handler = new TeacherMarkAttendanceHandler(jwtService, attendanceService);

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    private void request(String jsonBody) throws Exception {
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/attendance"));

        when(exchange.getRequestBody())
                .thenReturn(new ByteArrayInputStream(jsonBody.getBytes()));
    }

    private DecodedJWT mockJwt(Long id) {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(claim);
        when(claim.asLong()).thenReturn(id);

        return jwt;
    }

    // ---------------------------------------
    // SUCCESS: PRESENT
    // ---------------------------------------

    @Test
    void present_success() throws Exception {
        request("""
                {
                  "studentId": 1,
                  "sessionId": 10,
                  "status": "PRESENT"
                }
                """);

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            handler.handle(exchange);

            verify(attendanceService).markPresent(1L, 10L);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
    }

    // ---------------------------------------
    // SUCCESS: ABSENT
    // ---------------------------------------

    @Test
    void absent_success() throws Exception {
        request("""
                {
                  "studentId": 2,
                  "sessionId": 20,
                  "status": "ABSENT"
                }
                """);

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            handler.handle(exchange);

            verify(attendanceService).markAbsent(2L, 20L);
        }
    }

    // ---------------------------------------
    // SUCCESS: EXCUSED
    // ---------------------------------------

    @Test
    void excused_success() throws Exception {
        request("""
                {
                  "studentId": 3,
                  "sessionId": 30,
                  "status": "EXCUSED"
                }
                """);

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            handler.handle(exchange);

            verify(attendanceService).markExcused(3L, 30L);
        }
    }

    // ---------------------------------------
    // INVALID STATUS
    // ---------------------------------------

    @Test
    void invalidStatus_returns400() throws Exception {
        request("""
                {
                  "studentId": 1,
                  "sessionId": 10,
                  "status": "UNKNOWN"
                }
                """);

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);


            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(400), anyLong());
            assertTrue(responseBody.toString().contains("Invalid status"));
        }
    }

    // ---------------------------------------
    // MISSING FIELD
    // ---------------------------------------

    @Test
    void missingFields_returns400() throws Exception {
        request("""
                {
                  "studentId": 1
                }
                """);

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);


            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(400), anyLong());
            assertTrue(responseBody.toString().contains("required"));
        }
    }

    // ---------------------------------------
    // INVALID JSON
    // ---------------------------------------

    @Test
    void invalidJson_returns400() throws Exception {
        request("{ invalid json }");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(400), anyLong());
            assertTrue(responseBody.toString().contains("Invalid JSON"));
        }
    }

    // ---------------------------------------
    // AUTH FAIL
    // ---------------------------------------

    @Test
    void authFails_returns403() throws Exception {
        request("""
                {
                  "studentId": 1,
                  "sessionId": 10,
                  "status": "PRESENT"
                }
                """);

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenThrow(new SecurityException("Forbidden"));

            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(403), anyLong());
            assertTrue(responseBody.toString().contains("Forbidden"));
        }
    }
}