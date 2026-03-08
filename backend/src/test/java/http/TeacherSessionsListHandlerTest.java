package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import config.SessionSQL;
import model.Session;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherSessionsListHandlerTest {

    @Test
    void missingAuthorization_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions/list?classId=5", null);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verifyNoInteractions(classSQL, sessionSQL);
    }

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/sessions/list?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        verifyNoInteractions(classSQL, sessionSQL);
    }

    @Test
    void missingClassId_returns400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions/list", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("classId is required"));
        verifyNoInteractions(classSQL, sessionSQL);
    }

    @Test
    void forbidden_notOwner_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions/list?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(false);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden: not your class"));
        verify(sessionSQL, never()).findById(anyLong());
    }

    @Test
    void success_teacherOwner_returns200_andMapsSessionFields() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions/list?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(true);

        Session s = mock(Session.class);
        when(s.getId()).thenReturn(99L);
        when(s.getClassId()).thenReturn(5L);
        when(s.getSessionDate()).thenReturn(LocalDate.of(2026, 3, 3));
        when(s.getQrCode()).thenReturn("ABCD1234");

        // NOTE: handler calls findById(classId) (weird but we test current behavior)
        when(sessionSQL.findById(5L)).thenReturn(s);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        String body = ex.responseBodyString();

        assertTrue(body.contains("\"data\""));
        assertTrue(body.contains("\"id\":99"));
        assertTrue(body.contains("\"classId\":5"));
        assertTrue(body.contains("\"date\":\"2026-03-03\""));
        assertTrue(body.contains("\"code\":\"ABCD1234\""));

        verify(sessionSQL).findById(5L);
    }

    @Test
    void success_adminBypassesOwnership_returns200() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions/list?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("ADMIN", 1L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        Session s = mock(Session.class);
        when(s.getId()).thenReturn(1L);
        when(s.getClassId()).thenReturn(5L);
        when(s.getSessionDate()).thenReturn(LocalDate.of(2026, 3, 3));
        when(s.getQrCode()).thenReturn("X");

        when(sessionSQL.findById(5L)).thenReturn(s);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL, never()).isClassOwnedByTeacher(anyLong(), anyLong());
        verify(sessionSQL).findById(5L);
    }

    @Test
    void sessionSqlReturnsNull_causes500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions/list?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("ADMIN", 1L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(sessionSQL.findById(5L)).thenReturn(null);

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
    }

    @Test
    void idClaimNull_fallsBackToSubject() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsListHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions/list?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndNullId("TEACHER", "123");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 123L)).thenReturn(true);

        Session s = mock(Session.class);
        when(s.getId()).thenReturn(99L);
        when(s.getClassId()).thenReturn(5L);
        when(s.getSessionDate()).thenReturn(LocalDate.of(2026, 3, 3));
        when(s.getQrCode()).thenReturn("ABCD1234");

        when(sessionSQL.findById(5L)).thenReturn(s);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL).isClassOwnedByTeacher(5L, 123L);
    }

    // ---- helpers ----

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

    private static DecodedJWT jwtWithRoleAndNullId(String role, String subject) {
        DecodedJWT jwt = mock(DecodedJWT.class);

        Claim roleClaim = mock(Claim.class);
        when(roleClaim.isNull()).thenReturn(false);
        when(roleClaim.asString()).thenReturn(role);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        Claim idClaim = mock(Claim.class);
        when(idClaim.isNull()).thenReturn(true);
        when(jwt.getClaim("id")).thenReturn(idClaim);

        when(jwt.getSubject()).thenReturn(subject);
        return jwt;
    }
}