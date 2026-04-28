package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TeacherSessionsListHandlerTest {

    private JwtService jwtService;
    private ClassSQL classSQL;
    private SessionSQL sessionSQL;

    private TeacherSessionsListHandler handler;
    private HttpExchange exchange;

    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        classSQL = mock(ClassSQL.class);
        sessionSQL = mock(SessionSQL.class);

        handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

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

        when(idClaim.isNull()).thenReturn(false);
        when(idClaim.asLong()).thenReturn(id);

        when(roleClaim.isNull()).thenReturn(false);
        when(roleClaim.asString()).thenReturn(role);

        return jwt;
    }

    @Test
    void success_returnsSessions() throws Exception {

        request("/api/sessions?classId=10");

        DecodedJWT jwt = mockJwt(5L, "TEACHER");

        try (MockedStatic<security.Auth> auth = mockStatic(security.Auth.class)) {

            auth.when(() -> security.Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> security.Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(classSQL.isClassOwnedByTeacher(10L, 5L))
                    .thenReturn(true);

            Session session = mock(Session.class);
            when(session.getId()).thenReturn(1L);
            when(session.getClassId()).thenReturn(10L);
            when(session.getSessionDate()).thenReturn(LocalDate.of(2026, 1, 1));
            when(session.getQrCode()).thenReturn("ABC123");

            when(sessionSQL.findById(10L))
                    .thenReturn(session);

            handler.handle(exchange);
        }

        String body = responseBody.toString(StandardCharsets.UTF_8);

        assertTrue(body.contains("data"));
        assertTrue(body.contains("ABC123"));
        assertTrue(body.contains("classId"));
    }

    @Test
    void missing_classId_returns400() throws Exception {
        request("/api/sessions"); // no query string

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(eq(jwt), anyString()))
                    .thenAnswer(i -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void invalid_classId_returns400() throws Exception {
        request( "/api/sessions?classId=abc");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(eq(jwt), anyString()))
                    .thenAnswer(i -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void teacher_notOwner_returns403() throws Exception {
        request("/api/sessions?classId=1");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(5L, "TEACHER");

            when(jwt.getClaim("role").isNull()).thenReturn(false);
            when(jwt.getClaim("role").asString()).thenReturn("TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(eq(jwt), anyString()))
                    .thenAnswer(i -> null);

            when(classSQL.isClassOwnedByTeacher(1L, 5L))
                    .thenReturn(false);

            handler.handle(exchange);
        }

        verify(classSQL).isClassOwnedByTeacher(1L, 5L);
        verify(exchange).sendResponseHeaders(eq(403), anyLong());
    }

    @Test
    void admin_skipsOwnershipCheck_returns200() throws Exception {
        request("/api/sessions?classId=1");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "ADMIN");

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), anyString()))
                    .thenAnswer(i -> null);

            Session session = mock(Session.class);
            when(session.getId()).thenReturn(1L);
            when(session.getClassId()).thenReturn(1L);
            when(session.getSessionDate()).thenReturn(LocalDate.now());
            when(session.getQrCode()).thenReturn("ABC");

            when(sessionSQL.findById(1L)).thenReturn(session);

            handler.handle(exchange);
        }

        verify(classSQL, never()).isClassOwnedByTeacher(anyLong(), anyLong());
        verify(exchange).sendResponseHeaders(eq(200), anyLong());
    }
}