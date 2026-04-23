package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import security.Auth;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TeacherSessionReportHandlerTest {

    private JwtService jwtService;
    private ClassSQL classSQL;
    private SessionSQL sessionSQL;
    private AttendanceSQL attendanceSQL;

    private TeacherSessionReportHandler handler;
    private HttpExchange exchange;

    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        classSQL = mock(ClassSQL.class);
        sessionSQL = mock(SessionSQL.class);
        attendanceSQL = mock(AttendanceSQL.class);

        handler = new TeacherSessionReportHandler(
                jwtService,
                classSQL,
                sessionSQL,
                attendanceSQL
        );

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    // ---------------- helpers ----------------

    private void request(String query) throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/session/report?" + query));
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

    private Session mockSession(Long classId) {
        Session session = mock(Session.class);
        when(session.getClassId()).thenReturn(classId);
        when(session.getSessionDate()).thenReturn(LocalDate.of(2026, 1, 1));
        return session;
    }

    // ---------------- tests ----------------

    @Test
    void missingSessionId_returns400() throws Exception {
        request("");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString().contains("sessionId is required"));
    }

    @Test
    void sessionNotFound_returns404() throws Exception {
        request("sessionId=10");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(sessionSQL.findById(10L)).thenReturn(null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(404), anyLong());
        assertTrue(responseBody.toString().contains("Session not found"));
    }

    @Test
    void notOwnerTeacher_returns403() throws Exception {
        request("sessionId=10");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            Session session = mockSession(99L);

            when(sessionSQL.findById(10L)).thenReturn(session);
            when(classSQL.isClassOwnedByTeacher(99L, 1L)).thenReturn(false);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(403), anyLong());
        assertTrue(responseBody.toString().contains("Forbidden"));
    }

    @Test
    void teacher_owner_returns200() throws Exception {
        request("sessionId=10&languageCode=en");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            Session session = mockSession(99L);

            when(sessionSQL.findById(10L)).thenReturn(session);
            when(classSQL.isClassOwnedByTeacher(99L, 1L)).thenReturn(true);

            when(attendanceSQL.getSessionReport(10L, "en"))
                    .thenReturn(Map.of("student", "A"));
            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        assertTrue(responseBody.toString().contains("sessionId"));
    }

    @Test
    void admin_bypassesOwnership_returns200() throws Exception {
        request("sessionId=10");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(99L, "ADMIN");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            Session session = mockSession(77L);

            when(sessionSQL.findById(10L)).thenReturn(session);
            when(attendanceSQL.getSessionReport(10L, "en"))
                    .thenReturn(Map.of("student", "A"));
            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
    }
}