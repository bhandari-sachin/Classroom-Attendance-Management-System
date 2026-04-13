package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import dto.AttendanceView;
import model.Session;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherSessionReportHandlerTest {

    @Test
    void missingAuthorization_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherSessionReportHandler(jwtService, classSQL, sessionSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/session-report?sessionId=5", null);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verifyNoInteractions(classSQL, sessionSQL, attendanceSQL);
    }

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherSessionReportHandler(jwtService, classSQL, sessionSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/session-report?sessionId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        verifyNoInteractions(classSQL, sessionSQL, attendanceSQL);
    }

    @Test
    void missingSessionId_returns400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherSessionReportHandler(jwtService, classSQL, sessionSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/session-report", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("sessionId is required"));
        verifyNoInteractions(classSQL, sessionSQL, attendanceSQL);
    }

    @Test
    void sessionNotFound_returns404() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherSessionReportHandler(jwtService, classSQL, sessionSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/session-report?sessionId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(sessionSQL.findById(5L)).thenReturn(null);

        handler.handle(ex);

        assertEquals(404, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Session not found"));
        verify(sessionSQL).findById(5L);
        verifyNoInteractions(classSQL, attendanceSQL);
    }

    @Test
    void teacherNotOwner_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherSessionReportHandler(jwtService, classSQL, sessionSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/session-report?sessionId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        Session session = mock(Session.class);
        when(session.getClassId()).thenReturn(3L);
        when(session.getSessionDate()).thenReturn(LocalDate.of(2026, 3, 3));
        when(sessionSQL.findById(5L)).thenReturn(session);

        when(classSQL.isClassOwnedByTeacher(3L, 10L)).thenReturn(false);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden: not your session"));
        verify(attendanceSQL, never()).getSessionReport(anyLong(), anyString());
    }


    @Test
    void attendanceSqlThrows_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherSessionReportHandler(jwtService, classSQL, sessionSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/session-report?sessionId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("ADMIN", 1L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        Session session = mock(Session.class);
        when(session.getClassId()).thenReturn(3L);
        when(session.getSessionDate()).thenReturn(LocalDate.of(2026, 3, 3));
        when(sessionSQL.findById(5L)).thenReturn(session);

        when(attendanceSQL.getSessionReport(5L, "en")).thenThrow(new RuntimeException("DB down"));

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
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