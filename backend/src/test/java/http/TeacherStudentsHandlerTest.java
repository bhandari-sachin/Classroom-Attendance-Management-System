package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import config.ClassSQL;
import backend.exception.ApiException;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherStudentsHandlerTest {

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

    @Test
    void missingClassId_shouldReturn400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);

        var ex = new FakeExchange("GET", "/teacher/students", null);
        ex.getRequestHeaders().set("Authorization", "Bearer token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("classId is required"));
    }

    @Test
    void teacherNotOwner_shouldReturn403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);

        var ex = new FakeExchange("GET", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(false);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden"));
        verify(classSQL, never()).listStudentsForClass(anyLong());
    }

    @Test
    void teacherOwner_shouldReturn200() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);

        var ex = new FakeExchange("GET", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(true);
        when(classSQL.listStudentsForClass(5L))
                .thenReturn(Collections.emptyList());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL).listStudentsForClass(5L);
    }

    @Test
    void admin_shouldBypassOwnership() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);

        var ex = new FakeExchange("GET", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer token");

        DecodedJWT jwt = jwtWithRoleAndId("ADMIN", 1L);
        when(jwtService.verify("token")).thenReturn(jwt);

        when(classSQL.listStudentsForClass(5L)).thenReturn(Collections.emptyList());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL, never()).isClassOwnedByTeacher(anyLong(), anyLong());
        verify(classSQL).listStudentsForClass(5L);
    }

    @Test
    void success_shouldReturnStudents() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);

        var handler = new TeacherStudentsHandler(jwtService, classSQL);

        var ex = new FakeExchange("GET", "/teacher/students?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(true);
        when(classSQL.listStudentsForClass(5L))
                .thenReturn(List.of(Map.of("id", 1, "name", "John")));

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("John"));
    }
}