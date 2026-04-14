package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import config.ClassSQL;
import config.SessionSQL;
import model.Session;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherSessionsListHandlerTest {

    private static class FakeExchange extends AdminClassesHandlerTest.FakeExchange {
        FakeExchange(String method, String path, String body) {
            super(method, path, body);
        }
    }

    private static DecodedJWT jwtWithRoleAndId(String role, long id) {
        DecodedJWT jwt = mock(DecodedJWT.class);

        Claim roleClaim = mock(Claim.class);
        when(roleClaim.isNull()).thenReturn(false);
        when(roleClaim.asString()).thenReturn(role);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        Claim idClaim = mock(Claim.class);
        when(idClaim.isNull()).thenReturn(false);
        when(idClaim.asLong()).thenReturn(id);
        when(jwt.getClaim("id")).thenReturn(idClaim);

        when(jwt.getSubject()).thenReturn(String.valueOf(id));
        return jwt;
    }

    // -----------------------------
    @Test
    void missingAuthorization_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new FakeExchange("GET", "/teacher/sessions/list?classId=1", null);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));

        verifyNoInteractions(classSQL, sessionSQL);
    }

    // -----------------------------
    @Test
    void missingClassId_returns400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new FakeExchange("GET", "/teacher/sessions/list", null);
        ex.getRequestHeaders().set("Authorization", "Bearer token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("classId is required"));
    }

    // -----------------------------
    @Test
    void forbiddenNotOwner_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new FakeExchange("GET", "/teacher/sessions/list?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(false);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden: not your class"));
    }

    // -----------------------------
    @Test
    void admin_bypassesOwnership_returns200() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        Session session = mock(Session.class);
        when(session.getId()).thenReturn(1L);
        when(session.getClassId()).thenReturn(5L);
        when(session.getSessionDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(session.getQrCode()).thenReturn("ABC");

        when(sessionSQL.findById(5L)).thenReturn(session);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new FakeExchange("GET", "/teacher/sessions/list?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer token");

        DecodedJWT jwt = jwtWithRoleAndId("ADMIN", 1L);
        when(jwtService.verify("token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("\"data\""));

        verify(sessionSQL).findById(5L);
    }

    // -----------------------------
    @Test
    void teacher_success_returns200_withData() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        Session session = mock(Session.class);
        when(session.getId()).thenReturn(1L);
        when(session.getClassId()).thenReturn(5L);
        when(session.getSessionDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(session.getQrCode()).thenReturn("XYZ");

        when(sessionSQL.findById(5L)).thenReturn(session);
        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(true);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new FakeExchange("GET", "/teacher/sessions/list?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);

        String body = ex.responseBodyString();
        assertTrue(body.contains("\"data\""));
        assertTrue(body.contains("\"classId\":5"));
        assertTrue(body.contains("\"code\":\"XYZ\""));

        verify(sessionSQL).findById(5L);
    }

    // -----------------------------
    @Test
    void databaseException_shouldReturn500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        // Force DB failure
        when(sessionSQL.findById(5L))
                .thenThrow(new java.sql.SQLException("DB error"));

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);

        var ex = new FakeExchange("GET", "/teacher/sessions?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer token");

        DecodedJWT jwt = jwtWithRoleAndId("ADMIN", 1L);
        when(jwtService.verify("token")).thenReturn(jwt);

        // IMPORTANT: class ownership must pass so we reach DB layer
        when(classSQL.isClassOwnedByTeacher(5L, 1L)).thenReturn(true);

        handler.handle(ex);

        // ✅ Handler converts exception → HTTP 500 (not thrown)
        assertEquals(500, ex.statusCode);

        String body = ex.responseBodyString().toLowerCase();

        assertTrue(
                body.contains("database")
                        || body.contains("error")
                        || body.contains("failed")
        );

        verify(sessionSQL).findById(5L);
    }
}