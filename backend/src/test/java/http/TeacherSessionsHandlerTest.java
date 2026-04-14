package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.ClassSQL;
import config.SessionSQL;
import backend.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherSessionsHandlerTest {

    private JwtService jwtService;
    private ClassSQL classSQL;
    private SessionSQL sessionSQL;
    private TeacherSessionsHandler handler;

    private HttpExchange exchange;
    private BaseHandler.RequestContext ctx;
    private DecodedJWT jwt;
    private Claim roleClaim;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        classSQL = mock(ClassSQL.class);
        sessionSQL = mock(SessionSQL.class);

        handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);

        exchange = mock(HttpExchange.class);
        ctx = mock(BaseHandler.RequestContext.class);
        jwt = mock(DecodedJWT.class);
        roleClaim = mock(Claim.class);

        // ================= FIX: prevent NULL crashes =================
        when(exchange.getRequestHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());

        when(ctx.getJwt()).thenReturn(jwt);
        when(jwt.getClaim("role")).thenReturn(roleClaim);
        when(roleClaim.isNull()).thenReturn(false);
        when(roleClaim.asString()).thenReturn("TEACHER");
    }

    // =========================
    // GET TESTS
    // =========================

    @Test
    void shouldListSessions_whenTeacherOwnsClass() throws IOException {
        Long classId = 1L;
        Long teacherId = 10L;

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(ctx.getLongQuery("classId")).thenReturn(classId);
        when(ctx.getUserId()).thenReturn(teacherId);

        when(classSQL.isClassOwnedByTeacher(classId, teacherId)).thenReturn(true);
        when(sessionSQL.listForClass(classId)).thenReturn(List.of());

        handler.handleRequest(exchange, ctx);

        verify(sessionSQL).listForClass(classId);
    }

    @Test
    void shouldThrow400_whenClassIdMissing_GET() {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(ctx.getLongQuery("classId")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(400, ex.getStatus());
    }

    @Test
    void shouldThrow403_whenNotOwner_GET() {
        when(exchange.getRequestMethod()).thenReturn("GET");

        when(ctx.getLongQuery("classId")).thenReturn(1L);
        when(ctx.getUserId()).thenReturn(10L);

        when(classSQL.isClassOwnedByTeacher(1L, 10L)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(403, ex.getStatus());
    }

    // =========================
    // POST TESTS
    // =========================

    @Test
    void shouldCreateSession_success() throws IOException {
        String json = """
                {
                  "classId": 1,
                  "startTime": "10:00",
                  "endTime": "11:00"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(
                new ByteArrayInputStream(json.getBytes())
        );

        when(ctx.getUserId()).thenReturn(10L);

        when(classSQL.isClassOwnedByTeacher(1L, 10L)).thenReturn(true);
        when(sessionSQL.createSession(anyLong(), any(), any(), any(), any()))
                .thenReturn(99L);

        handler.handleRequest(exchange, ctx);

        verify(sessionSQL).createSession(
                eq(1L),
                any(),
                eq(LocalTime.parse("10:00")),
                eq(LocalTime.parse("11:00")),
                any()
        );
    }

    @Test
    void shouldThrow400_whenMissingClassId_POST() throws IOException {
        String json = "{}";

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(
                new ByteArrayInputStream(json.getBytes())
        );

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(400, ex.getStatus());
    }

    @Test
    void shouldThrow403_whenNotOwner_POST() throws IOException {
        String json = """
                { "classId": 1 }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(
                new ByteArrayInputStream(json.getBytes())
        );

        when(ctx.getUserId()).thenReturn(10L);
        when(classSQL.isClassOwnedByTeacher(1L, 10L)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(403, ex.getStatus());
    }

    @Test
    void shouldThrow400_whenEndBeforeStart() throws IOException {
        String json = """
                {
                  "classId": 1,
                  "startTime": "12:00",
                  "endTime": "10:00"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(
                new ByteArrayInputStream(json.getBytes())
        );

        when(ctx.getUserId()).thenReturn(10L);
        when(classSQL.isClassOwnedByTeacher(1L, 10L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(400, ex.getStatus());
    }

    @Test
    void shouldThrow405_whenInvalidMethod() {
        when(exchange.getRequestMethod()).thenReturn("PUT");

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(405, ex.getStatus());
    }
}