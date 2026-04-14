package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import backend.exception.ApiException;
import model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherSessionReportHandlerTest {

    private JwtService jwtService;
    private ClassSQL classSQL;
    private SessionSQL sessionSQL;
    private AttendanceSQL attendanceSQL;

    private TeacherSessionReportHandler handler;

    private HttpExchange exchange;
    private BaseHandler.RequestContext ctx;
    private DecodedJWT jwt;
    private Claim roleClaim;

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

        when(ctx.getQuery("languageCode", "en")).thenReturn("en");
    }

    @Test
    void shouldReturnReport_whenValidTeacher() throws IOException, SQLException {
        Long sessionId = 1L;
        Long teacherId = 10L;

        Session session = mock(Session.class);

        when(ctx.getLongQuery("sessionId")).thenReturn(sessionId);
        when(ctx.getUserId()).thenReturn(teacherId);

        when(sessionSQL.findById(sessionId)).thenReturn(session);
        when(session.getClassId()).thenReturn(5L);
        when(session.getSessionDate()).thenReturn(LocalDate.now());

        when(classSQL.isClassOwnedByTeacher(5L, teacherId)).thenReturn(true);
        when(attendanceSQL.getSessionReport(sessionId, "en")).thenReturn(Map.of());

        handler.handleRequest(exchange, ctx);

        verify(attendanceSQL).getSessionReport(sessionId, "en");
    }

    @Test
    void shouldReturnReport_whenAdmin() throws IOException, SQLException {
        Long sessionId = 1L;

        Session session = mock(Session.class);

        when(ctx.getLongQuery("sessionId")).thenReturn(sessionId);
        when(ctx.getUserId()).thenReturn(99L);

        when(roleClaim.asString()).thenReturn("ADMIN");

        when(sessionSQL.findById(sessionId)).thenReturn(session);
        when(session.getClassId()).thenReturn(5L);
        when(session.getSessionDate()).thenReturn(LocalDate.now());

        when(attendanceSQL.getSessionReport(sessionId, "en")).thenReturn(Map.of());

        handler.handleRequest(exchange, ctx);

        verify(classSQL, never()).isClassOwnedByTeacher(any(), any());
    }

    @Test
    void shouldThrow400_whenSessionIdMissing() {
        when(ctx.getLongQuery("sessionId")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(400, ex.getStatus());
    }

    @Test
    void shouldThrow404_whenSessionNotFound() throws SQLException {
        Long sessionId = 1L;

        when(ctx.getLongQuery("sessionId")).thenReturn(sessionId);
        when(sessionSQL.findById(sessionId)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(404, ex.getStatus());
    }

    @Test
    void shouldThrow403_whenTeacherNotOwner() throws SQLException {
        Long sessionId = 1L;
        Long teacherId = 10L;

        Session session = mock(Session.class);

        when(ctx.getLongQuery("sessionId")).thenReturn(sessionId);
        when(ctx.getUserId()).thenReturn(teacherId);

        when(sessionSQL.findById(sessionId)).thenReturn(session);
        when(session.getClassId()).thenReturn(5L);

        when(classSQL.isClassOwnedByTeacher(5L, teacherId)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(403, ex.getStatus());
    }

    @Test
    void shouldThrow500_whenDatabaseError() throws SQLException {
        Long sessionId = 1L;

        when(ctx.getLongQuery("sessionId")).thenReturn(sessionId);
        when(sessionSQL.findById(sessionId)).thenThrow(new SQLException());

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(500, ex.getStatus());
    }
}