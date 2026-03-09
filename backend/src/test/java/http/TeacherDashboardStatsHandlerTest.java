package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import config.AttendanceSQL;
import config.ClassSQL;
import org.junit.jupiter.api.Test;
import security.JwtService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherDashboardStatsHandlerTest {

    @Test
    void missingAuthorization_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherDashboardStatsHandler(jwtService, classSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/dashboard/stats", null);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verifyNoInteractions(classSQL, attendanceSQL);
    }

    @Test
    void wrongRole_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherDashboardStatsHandler(jwtService, classSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/dashboard/stats", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("STUDENT", 7L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden for role"));
        verifyNoInteractions(classSQL, attendanceSQL);
    }

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherDashboardStatsHandler(jwtService, classSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/dashboard/stats", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 7L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        verifyNoInteractions(classSQL, attendanceSQL);
    }

    @Test
    void success_usesIdClaim_whenPresent_returns200WithCounts() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherDashboardStatsHandler(jwtService, classSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/dashboard/stats", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 77L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.countForTeacher(77L)).thenReturn(3);
        when(classSQL.countStudentsForTeacher(77L)).thenReturn(58);
        when(attendanceSQL.countTodayForTeacher(77L, "PRESENT")).thenReturn(12);
        when(attendanceSQL.countTodayForTeacher(77L, "ABSENT")).thenReturn(4);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);

        String body = ex.responseBodyString();
        assertTrue(body.contains("\"totalClasses\":3"));
        assertTrue(body.contains("\"totalStudents\":58"));
        assertTrue(body.contains("\"presentToday\":12"));
        assertTrue(body.contains("\"absentToday\":4"));

        verify(classSQL).countForTeacher(77L);
        verify(classSQL).countStudentsForTeacher(77L);
        verify(attendanceSQL).countTodayForTeacher(77L, "PRESENT");
        verify(attendanceSQL).countTodayForTeacher(77L, "ABSENT");
    }

    @Test
    void success_fallsBackToSubject_whenIdClaimIsNull() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherDashboardStatsHandler(jwtService, classSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/dashboard/stats", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndNullId("ADMIN", "123"); // ADMIN allowed
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.countForTeacher(123L)).thenReturn(1);
        when(classSQL.countStudentsForTeacher(123L)).thenReturn(10);
        when(attendanceSQL.countTodayForTeacher(123L, "PRESENT")).thenReturn(5);
        when(attendanceSQL.countTodayForTeacher(123L, "ABSENT")).thenReturn(1);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL).countForTeacher(123L);
        verify(attendanceSQL).countTodayForTeacher(123L, "PRESENT");
    }

    @Test
    void sqlThrows_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        AttendanceSQL attendanceSQL = mock(AttendanceSQL.class);

        var handler = new TeacherDashboardStatsHandler(jwtService, classSQL, attendanceSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/dashboard/stats", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 7L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.countForTeacher(7L)).thenThrow(new RuntimeException("DB down"));

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
    }

    // ---- helpers ----

    private static DecodedJWT jwtWithRoleAndId(String role, long id) {
        DecodedJWT jwt = mock(DecodedJWT.class);

        Claim roleClaim = mock(Claim.class);
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
        when(roleClaim.asString()).thenReturn(role);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        Claim idClaim = mock(Claim.class);
        when(idClaim.isNull()).thenReturn(true);
        when(jwt.getClaim("id")).thenReturn(idClaim);

        when(jwt.getSubject()).thenReturn(subject);
        return jwt;
    }
}