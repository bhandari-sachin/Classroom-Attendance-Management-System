package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.ClassSQL;
import config.SessionSQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import security.Auth;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TeacherSessionsHandlerTest {

    private JwtService jwtService;
    private ClassSQL classSQL;
    private SessionSQL sessionSQL;

    private TeacherSessionsHandler handler;
    private HttpExchange exchange;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        classSQL = mock(ClassSQL.class);
        sessionSQL = mock(SessionSQL.class);

        handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    // ---------------- helpers ----------------

    private void request(String method, String uri) throws Exception {
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(new URI(uri));
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

    // ---------------- GET tests ----------------

    @Test
    void get_missingClassId_returns400() throws Exception {
        request("GET", "/api/sessions");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("classId is required"));
    }

    @Test
    void get_notOwner_returns403() throws Exception {
        request("GET", "/api/sessions?classId=10");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            when(classSQL.isClassOwnedByTeacher(10L, 1L)).thenReturn(false);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(403), anyLong());
    }

    @Test
    void get_owner_returns200() throws Exception {
        request("GET", "/api/sessions?classId=10");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            when(classSQL.isClassOwnedByTeacher(10L, 1L)).thenReturn(true);
            when(sessionSQL.listForClass(10L)).thenReturn(List.of(Map.of("id", 1)));

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
    }

    // ---------------- POST tests ----------------

    @Test
    void post_missingClassId_returns400() throws Exception {
        request("POST", "/api/sessions");

        when(exchange.getRequestBody())
                .thenReturn(new java.io.ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("classId is required"));
    }

    @Test
    void post_validRequest_returns201() throws Exception {
        request("POST", "/api/sessions");

        String json = "{\"classId\":10}";

        when(exchange.getRequestBody())
                .thenReturn(new java.io.ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            when(classSQL.isClassOwnedByTeacher(10L, 1L)).thenReturn(true);
            when(sessionSQL.createSession(anyLong(), any(), any(), any(), anyString()))
                    .thenReturn(99L);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(201), anyLong());
        assertTrue(responseBody.toString(StandardCharsets.UTF_8).contains("sessionId"));
    }

    @Test
    void unsupported_method_returns405() throws Exception {
        request("DELETE", "/api/sessions?classId=1");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
    }

    @Test
    void get_nullRole_defaultsToEmptyString() throws Exception {
        request("GET", "/api/sessions?classId=10");

        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim idClaim = mock(Claim.class);
        Claim roleClaim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(idClaim);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        when(idClaim.asLong()).thenReturn(1L);
        when(roleClaim.isNull()).thenReturn(true); // 👈 IMPORTANT

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            when(classSQL.isClassOwnedByTeacher(10L, 1L)).thenReturn(true);
            when(sessionSQL.listForClass(10L)).thenReturn(List.of());

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    void post_missingTimes_usesDefaults() throws Exception {
        request("POST", "/api/sessions");

        String json = "{\"classId\":10}"; // no startTime/endTime

        when(exchange.getRequestBody())
                .thenReturn(new java.io.ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L, "TEACHER");

            auth.when(() -> Auth.requireJwt(any(), any())).thenReturn(jwt);
            auth.when(() -> Auth.requireRole(any(), any())).thenAnswer(i -> null);

            when(classSQL.isClassOwnedByTeacher(10L, 1L)).thenReturn(true);
            when(sessionSQL.createSession(anyLong(), any(), any(), any(), anyString()))
                    .thenReturn(1L);

            handler.handle(exchange);
        }

        verify(exchange).sendResponseHeaders(eq(201), anyLong());
    }
}