package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import dto.AttendanceView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import security.Auth;
import security.JwtService;
import service.AttendanceService;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class StudentAttendanceRecordsHandlerTest {

    private JwtService jwtService;
    private AttendanceService attendanceService;
    private StudentAttendanceRecordsHandler handler;

    private HttpExchange exchange;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        attendanceService = mock(AttendanceService.class);

        handler = new StudentAttendanceRecordsHandler(jwtService, attendanceService);

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    private void request(String method, String path) throws Exception {
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(new URI(path));
    }

    private DecodedJWT mockJwt(Long id) {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(claim);
        when(claim.asLong()).thenReturn(id);

        return jwt;
    }

    // ---------------------------------------
    // SUCCESS CASE
    // ---------------------------------------

    @Test
    void success_returnsAttendanceRecords() throws Exception {
        request("GET", "/api/attendance?classId=1&period=THIS_MONTH");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(10L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(attendanceService.getStudentAttendanceViews(
                    anyLong(), anyLong(), anyString(), anyString()
            )).thenReturn(List.of(mock(AttendanceView.class)));

            handler.handle(exchange);

            verify(attendanceService).getStudentAttendanceViews(
                    10L,
                    1L,
                    "THIS_MONTH",
                    "en"
            );

            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
    }

    // ---------------------------------------
    // DEFAULT LANG FALLBACK
    // ---------------------------------------

    @Test
    void missingLang_defaultsToEn() throws Exception {
        request("GET", "/api/attendance?classId=2&period=ALL");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(5L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(attendanceService.getStudentAttendanceViews(
                    anyLong(), anyLong(), anyString(), anyString()
            )).thenReturn(List.of());

            handler.handle(exchange);

            verify(attendanceService).getStudentAttendanceViews(
                    5L,
                    2L,
                    "ALL",
                    "en" // default
            );
        }
    }

    // ---------------------------------------
    // AUTH FAILURE
    // ---------------------------------------

    @Test
    void authFails_returns403() throws Exception {
        request("GET", "/api/attendance");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenThrow(new SecurityException("Invalid token"));

            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(403), anyLong());
            assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("Invalid token"));
        }
    }
}