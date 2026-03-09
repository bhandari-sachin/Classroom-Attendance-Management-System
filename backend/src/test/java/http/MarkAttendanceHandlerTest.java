package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import security.JwtService;
import service.AttendanceService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarkAttendanceHandlerTest {

    @Test
    void missingAuthorization_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new MarkAttendanceHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/attendance/mark", "{\"code\":\"ABC\"}");

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verify(attendanceService, never()).markByCode(anyLong(), anyString());
    }

    @Test
    void wrongRole_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new MarkAttendanceHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/attendance/mark", "{\"code\":\"ABC\"}");
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 5L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden for role"));
        verify(attendanceService, never()).markByCode(anyLong(), anyString());
    }

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new MarkAttendanceHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/attendance/mark", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("STUDENT", 5L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        verify(attendanceService, never()).markByCode(anyLong(), anyString());
    }

    @Test
    void invalidJson_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new MarkAttendanceHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/attendance/mark", "{bad-json");
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("STUDENT", 5L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
        verify(attendanceService, never()).markByCode(anyLong(), anyString());
    }

    @Test
    void missingCode_returns400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new MarkAttendanceHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/attendance/mark", "{\"code\":\"   \"}");
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("STUDENT", 7L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Attendance code required"));
        verify(attendanceService, never()).markByCode(anyLong(), anyString());
    }

    @Test
    void success_usesIdClaim_whenPresent() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new MarkAttendanceHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/attendance/mark", "{\"code\":\"QR-123\"}");
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("STUDENT", 42L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("attendance marked"));

        verify(attendanceService).markByCode(42L, "QR-123");
    }

    @Test
    void success_fallsBackToSubject_whenIdClaimIsNull() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new MarkAttendanceHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/attendance/mark", "{\"code\":\"QR-999\"}");
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndNullIdClaim("STUDENT", "123");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(attendanceService).markByCode(123L, "QR-999");
    }

    @Test
    void serviceThrowsIllegalArgument_returns400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        var handler = new MarkAttendanceHandler(jwtService, attendanceService);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/attendance/mark", "{\"code\":\"BAD\"}");
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("STUDENT", 9L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        doThrow(new IllegalArgumentException("Invalid code")).when(attendanceService).markByCode(9L, "BAD");

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Invalid code"));
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

    private static DecodedJWT jwtWithRoleAndNullIdClaim(String role, String subject) {
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