package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherStudentsHandlerTest {

    @Test
    void missingAuthorization_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/students?classId=5", null);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verifyNoInteractions(classSQL);
    }

    @Test
    void wrongRole_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("STUDENT", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden for role"));
        verifyNoInteractions(classSQL);
    }

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        verifyNoInteractions(classSQL);
    }

    @Test
    void missingClassId_returns400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/students", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("classId is required"));
        verifyNoInteractions(classSQL);
    }

    @Test
    void forbidden_notOwner_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(false);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden: not your class"));
        verify(classSQL, never()).listStudentsForClass(anyLong());
    }

    @Test
    void success_teacherOwner_returns200_withData() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(true);

        // typed empty list avoids generics inference problems
        List<Map<String, Object>> students = Collections.<Map<String, Object>>emptyList();
        when(classSQL.listStudentsForClass(5L)).thenReturn(students);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        String body = ex.responseBodyString();
        assertTrue(body.contains("\"data\""));

        verify(classSQL).listStudentsForClass(5L);
    }

    @Test
    void success_adminBypassesOwnership_returns200() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("ADMIN", 1L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.listStudentsForClass(5L)).thenReturn(Collections.<Map<String, Object>>emptyList());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL, never()).isClassOwnedByTeacher(anyLong(), anyLong());
        verify(classSQL).listStudentsForClass(5L);
    }

    @Test
    void idClaimNull_fallsBackToSubject() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndNullId("TEACHER", "123");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 123L)).thenReturn(true);
        when(classSQL.listStudentsForClass(5L)).thenReturn(Collections.<Map<String, Object>>emptyList());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL).isClassOwnedByTeacher(5L, 123L);
        verify(classSQL).listStudentsForClass(5L);
    }

    @Test
    void classSqlThrows_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("ADMIN", 1L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.listStudentsForClass(5L)).thenThrow(new RuntimeException("DB down"));

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