package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.AttendanceSQL;
import config.ClassSQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import security.Auth;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TeacherReportsHandlerTest {

    private JwtService jwtService;
    private AttendanceSQL attendanceSQL;
    private ClassSQL classSQL;

    private TeacherReportsHandler handler;
    private HttpExchange exchange;

    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        attendanceSQL = mock(AttendanceSQL.class);
        classSQL = mock(ClassSQL.class);

        handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    // ---------------- helpers ----------------

    private void request(String path) throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI(path));
    }

    private DecodedJWT mockJwt(Long id, String role) {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim idClaim = mock(Claim.class);
        Claim roleClaim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(idClaim);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        when(idClaim.asLong()).thenReturn(id);
        when(roleClaim.asString()).thenReturn(role);
        when(roleClaim.isNull()).thenReturn(role == null);

        return jwt;
    }

    // ---------------- tests ----------------

    @Test
    void missingClassId_returns400() throws Exception {
        request("/api/reports/teacher");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString().contains("classId query param is required"));
    }

    @Test
    void notOwnerTeacher_returns403() throws Exception {
        request("/api/reports/teacher?classId=10");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(classSQL.isClassOwnedByTeacher(10L, 1L))
                    .thenReturn(false);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(403), anyLong());
        assertTrue(responseBody.toString().contains("not your class"));
    }

    @Test
    void admin_bypassesOwnership_returns200() throws Exception {
        request("/api/reports/teacher?classId=10");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(99L, "ADMIN");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(classSQL.isClassOwnedByTeacher(anyLong(), anyLong()))
                    .thenReturn(false);

            when(attendanceSQL.reportByClass(10L))
                    .thenReturn(List.of(Map.of("student", "A")));

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        assertTrue(responseBody.toString().contains("classId"));
    }

    @Test
    void teacher_owner_returns200() throws Exception {
        request("/api/reports/teacher?classId=10");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(classSQL.isClassOwnedByTeacher(10L, 1L))
                    .thenReturn(true);

            when(attendanceSQL.reportByClass(10L))
                    .thenReturn(List.of(Map.of("student", "A")));

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
    }
}