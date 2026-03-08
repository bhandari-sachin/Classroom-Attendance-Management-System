package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherClassesHandlerTest {

    @Test
    void missingAuthorization_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        var handler = new TeacherClassesHandler(jwtService, classSQL);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/classes", null);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verify(classSQL, never()).listForTeacher(anyLong());
    }

    @Test
    void wrongRole_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        var handler = new TeacherClassesHandler(jwtService, classSQL);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/classes", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("STUDENT", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden for role"));
        verify(classSQL, never()).listForTeacher(anyLong());
    }

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        var handler = new TeacherClassesHandler(jwtService, classSQL);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/classes", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        verify(classSQL, never()).listForTeacher(anyLong());
    }

    @Test
    void success_usesIdClaim_whenPresent() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        var handler = new TeacherClassesHandler(jwtService, classSQL);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/classes", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 77L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        // safest: list elements can be simple maps/objects
        List<Map<String, Object>> dummyList =
                List.<Map<String, Object>>of(
                        Map.of("id", 1L, "name", "Class A"),
                        Map.of("id", 2L, "name", "Class B")
                );

        when(classSQL.listForTeacher(77L)).thenReturn(dummyList);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL).listForTeacher(77L);

        String body = ex.responseBodyString();
        assertTrue(body.contains("\"data\""));
        assertTrue(body.contains("Class A"));
    }

    @Test
    void success_fallsBackToSubject_whenIdClaimIsNull() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        var handler = new TeacherClassesHandler(jwtService, classSQL);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/classes", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndNullId("ADMIN", "123"); // ADMIN is allowed too
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.listForTeacher(123L)).thenReturn(List.of());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL).listForTeacher(123L);
    }

    @Test
    void classSqlThrows_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        var handler = new TeacherClassesHandler(jwtService, classSQL);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/classes", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 7L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.listForTeacher(7L)).thenThrow(new RuntimeException("DB down"));

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