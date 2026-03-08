package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import config.AttendanceSQL;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherReportsHandlerTest {

    @Test
    void missingAuthorization_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/reports?classId=3", null);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verifyNoInteractions(attendanceSQL, classSQL);
    }

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/reports?classId=3", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleIdSubject("TEACHER", 10L, "10", false);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        verifyNoInteractions(attendanceSQL, classSQL);
    }

    @Test
    void missingClassIdQueryParam_returns400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/reports", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleIdSubject("TEACHER", 10L, "10", false);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("classId query param is required"));
        verifyNoInteractions(attendanceSQL, classSQL);
    }

    @Test
    void teacher_notOwner_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/reports?classId=3", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleIdSubject("TEACHER", 10L, "10", false);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(3L, 10L)).thenReturn(false);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden: not your class"));
        verify(attendanceSQL, never()).reportByClass(anyLong());
    }

    @Test
    void teacher_owner_returns200_andReport() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/reports?classId=3", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleIdSubject("TEACHER", 10L, "10", false);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(3L, 10L)).thenReturn(true);

        // easiest stable return type: empty list (avoids generics headaches)
        when(attendanceSQL.reportByClass(3L)).thenReturn(Collections.emptyList());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        String body = ex.responseBodyString();
        assertTrue(body.contains("\"classId\":3"));
        assertTrue(body.contains("\"data\""));

        verify(attendanceSQL).reportByClass(3L);
    }

    @Test
    void admin_bypassesOwnership_returns200() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/reports?classId=99", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleIdSubject("ADMIN", 1L, "1", false);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        // ownership check should not be required for ADMIN
        when(attendanceSQL.reportByClass(99L)).thenReturn(Collections.emptyList());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL, never()).isClassOwnedByTeacher(anyLong(), anyLong());
        verify(attendanceSQL).reportByClass(99L);
    }

    @Test
    void subjectFallback_whenIdClaimNull() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/reports?classId=3", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        // id claim isNull -> teacherId from subject "123"
        DecodedJWT jwt = jwtWithRoleIdSubject("TEACHER", null, "123", true);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(3L, 123L)).thenReturn(true);
        when(attendanceSQL.reportByClass(3L)).thenReturn(Collections.emptyList());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL).isClassOwnedByTeacher(3L, 123L);
    }

    @Test
    void attendanceSqlThrows_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/reports?classId=3", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleIdSubject("ADMIN", 1L, "1", false);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(attendanceSQL.reportByClass(3L)).thenThrow(new RuntimeException("DB down"));

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
    }

    // ---- helper ----
    private static DecodedJWT jwtWithRoleIdSubject(String role, Long id, String subject, boolean idIsNull) {
        DecodedJWT jwt = mock(DecodedJWT.class);

        Claim roleClaim = mock(Claim.class);
        when(roleClaim.isNull()).thenReturn(false);
        when(roleClaim.asString()).thenReturn(role);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        Claim idClaim = mock(Claim.class);
        when(idClaim.isNull()).thenReturn(idIsNull);
        if (!idIsNull) {
            when(idClaim.asLong()).thenReturn(id);
        }
        when(jwt.getClaim("id")).thenReturn(idClaim);

        when(jwt.getSubject()).thenReturn(subject);
        return jwt;
    }
}