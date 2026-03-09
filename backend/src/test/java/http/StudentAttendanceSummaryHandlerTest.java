package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import dto.AttendanceStats;
import org.junit.jupiter.api.Test;
import security.JwtService;
import service.AttendanceService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentAttendanceSummaryHandlerTest {

    @Test
    void missingAuthorization_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new StudentAttendanceSummaryHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/student/attendance/summary", null);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verify(attendanceService, never()).getStudentStats(anyLong());
    }

    @Test
    void wrongRole_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new StudentAttendanceSummaryHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/student/attendance/summary", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndSubject("TEACHER", "123");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden for role"));
        verify(attendanceService, never()).getStudentStats(anyLong());
    }


    @Test
    void subjectNotNumeric_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new StudentAttendanceSummaryHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/student/attendance/summary", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndSubject("STUDENT", "abc");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
        verify(attendanceService, never()).getStudentStats(anyLong());
    }

    @Test
    void serviceThrows_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new StudentAttendanceSummaryHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/student/attendance/summary", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndSubject("STUDENT", "123");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(attendanceService.getStudentStats(123L))
                .thenThrow(new RuntimeException("DB down"));

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
    }

    // ---- helper ----
    private static DecodedJWT jwtWithRoleAndSubject(String role, String subject) {
        DecodedJWT jwt = mock(DecodedJWT.class);

        Claim roleClaim = mock(Claim.class);
        when(roleClaim.asString()).thenReturn(role);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        when(jwt.getSubject()).thenReturn(subject);
        return jwt;
    }
}