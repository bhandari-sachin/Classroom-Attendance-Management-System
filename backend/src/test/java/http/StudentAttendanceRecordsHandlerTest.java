package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import dto.AttendanceView;
import org.junit.jupiter.api.Test;
import security.JwtService;
import service.AttendanceService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentAttendanceRecordsHandlerTest {

    @Test
    void missingAuthorization_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new StudentAttendanceRecordsHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/student/attendance", null);
        // no Authorization header

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verify(attendanceService, never()).getStudentAttendanceViews(anyLong());
    }

    @Test
    void wrongRole_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new StudentAttendanceRecordsHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/student/attendance", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndSubject("TEACHER", "123");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden for role"));
        verify(attendanceService, never()).getStudentAttendanceViews(anyLong());
    }

    @Test
    void success_returns200_andCallsServiceWithSubjectId() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new StudentAttendanceRecordsHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/student/attendance", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndSubject("STUDENT", "123");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        // safest: return mocks so Jackson can serialize to [{}] or [] without LocalDate issues
        AttendanceView v1 = mock(AttendanceView.class);
        AttendanceView v2 = mock(AttendanceView.class);
        when(attendanceService.getStudentAttendanceViews(123L)).thenReturn(List.of(v1, v2));

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(attendanceService).getStudentAttendanceViews(123L);

        String body = ex.responseBodyString();
        assertTrue(body.startsWith("["), "Expected JSON array response");
    }

    @Test
    void subjectNotNumeric_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new StudentAttendanceRecordsHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/student/attendance", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndSubject("STUDENT", "abc");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
        verify(attendanceService, never()).getStudentAttendanceViews(anyLong());
    }

    @Test
    void serviceThrows_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new StudentAttendanceRecordsHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/student/attendance", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndSubject("STUDENT", "123");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(attendanceService.getStudentAttendanceViews(123L))
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