package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import config.ClassSQL;
import config.SessionSQL;
import backend.exception.DatabaseException;
import model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherSessionsListHandlerTest {

    private JwtService jwtService;
    private ClassSQL classSQL;
    private SessionSQL sessionSQL;
    private TeacherSessionsListHandler handler;

    private HttpExchange exchange;
    private BaseHandler.RequestContext ctx; // ✅ FIX
    private DecodedJWT jwt;

    private Claim roleClaim;
    private Claim idClaim;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        classSQL = mock(ClassSQL.class);
        sessionSQL = mock(SessionSQL.class);

        handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);

        exchange = mock(HttpExchange.class);
        ctx = mock(BaseHandler.RequestContext.class); // ✅ FIX
        jwt = mock(DecodedJWT.class);

        roleClaim = mock(Claim.class);
        idClaim = mock(Claim.class);

        when(ctx.getJwt()).thenReturn(jwt);

        when(jwt.getClaim("role")).thenReturn(roleClaim);
        when(jwt.getClaim("id")).thenReturn(idClaim);

        when(roleClaim.isNull()).thenReturn(false);
        when(roleClaim.asString()).thenReturn("TEACHER");

        when(idClaim.isNull()).thenReturn(false);
        when(idClaim.asLong()).thenReturn(10L);
    }

    // =========================
    // ✅ SUCCESS
    // =========================

    @Test
    void shouldReturnSessions_whenTeacherOwnsClass() throws IOException, SQLException {
        Long classId = 1L;

        Session session = mock(Session.class);

        when(exchange.getRequestURI())
                .thenReturn(URI.create("/sessions?classId=1"));

        when(classSQL.isClassOwnedByTeacher(classId, 10L)).thenReturn(true);

        when(sessionSQL.findById(classId)).thenReturn(session);

        when(session.getId()).thenReturn(100L);
        when(session.getClassId()).thenReturn(classId);
        when(session.getSessionDate()).thenReturn(LocalDate.now());
        when(session.getQrCode()).thenReturn("ABC123");

        handler.handleRequest(exchange, ctx);

        verify(sessionSQL).findById(classId);
    }

    // =========================
    // ✅ 400 (missing classId)
    // =========================

    @Test
    void shouldReturn400_whenClassIdMissing() throws IOException, SQLException {
        when(exchange.getRequestURI())
                .thenReturn(URI.create("/sessions")); // no query

        handler.handleRequest(exchange, ctx);

        verify(sessionSQL, never()).findById(any());
    }

    // =========================
    // ✅ 403 (not owner)
    // =========================

    @Test
    void shouldReturn403_whenNotOwner() throws IOException, SQLException {
        when(exchange.getRequestURI())
                .thenReturn(URI.create("/sessions?classId=1"));

        when(classSQL.isClassOwnedByTeacher(1L, 10L)).thenReturn(false);

        handler.handleRequest(exchange, ctx);

        verify(sessionSQL, never()).findById(any());
    }

    // =========================
    // ✅ ADMIN bypass
    // =========================

    @Test
    void shouldAllowAdmin_withoutOwnershipCheck() throws IOException, SQLException {
        Session session = mock(Session.class);

        when(exchange.getRequestURI())
                .thenReturn(URI.create("/sessions?classId=1"));

        when(roleClaim.asString()).thenReturn("ADMIN");

        when(sessionSQL.findById(1L)).thenReturn(session);
        when(session.getId()).thenReturn(1L);
        when(session.getClassId()).thenReturn(1L);
        when(session.getSessionDate()).thenReturn(LocalDate.now());
        when(session.getQrCode()).thenReturn("CODE");

        handler.handleRequest(exchange, ctx);

        verify(classSQL, never()).isClassOwnedByTeacher(any(), any());
    }

    // =========================
    // ✅ DB ERROR
    // =========================

    @Test
    void shouldThrowDatabaseException_whenSqlFails() throws SQLException {
        when(exchange.getRequestURI())
                .thenReturn(URI.create("/sessions?classId=1"));

        when(classSQL.isClassOwnedByTeacher(1L, 10L)).thenReturn(true);

        when(sessionSQL.findById(1L)).thenThrow(new SQLException());

        assertThrows(DatabaseException.class, () ->
                handler.handleRequest(exchange, ctx)
        );
    }
}