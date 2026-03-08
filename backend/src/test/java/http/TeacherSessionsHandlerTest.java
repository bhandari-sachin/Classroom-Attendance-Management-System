package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import config.SessionSQL;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import security.JwtService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherSessionsHandlerTest {

    @Test
    void missingAuthorization_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions?classId=1", null);

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

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("PUT", "/teacher/sessions?classId=1", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        verifyNoInteractions(classSQL, sessionSQL);
    }

    // -------------------------
    // GET / list
    // -------------------------

    @Test
    void get_missingClassId_returns400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("classId is required"));
        verifyNoInteractions(classSQL, sessionSQL);
    }

    @Test
    void get_forbiddenNotOwner_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(false);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden: not your class"));
        verify(sessionSQL, never()).listForClass(anyLong());
    }

    @Test
    void get_success_returns200_withData() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(true);

        List<Map<String, Object>> sessions = Collections.<Map<String, Object>>emptyList();
        when(sessionSQL.listForClass(5L)).thenReturn(sessions);

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("\"data\""));
        verify(sessionSQL).listForClass(5L);
    }

    @Test
    void get_adminBypassesOwnership_returns200() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teacher/sessions?classId=5", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("ADMIN", 1L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(sessionSQL.listForClass(5L)).thenReturn(Collections.<Map<String, Object>>emptyList());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(classSQL, never()).isClassOwnedByTeacher(anyLong(), anyLong());
        verify(sessionSQL).listForClass(5L);
    }

    // -------------------------
    // POST / create
    // -------------------------

    @Test
    void post_missingClassId_returns400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/sessions", "{\"x\":1}");
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("classId is required"));
        verifyNoInteractions(classSQL, sessionSQL);
    }

    @Test
    void post_forbiddenNotOwner_returns403() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/sessions", "{\"classId\":5}");
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(false);

        handler.handle(ex);

        assertEquals(403, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden: not your class"));
        verify(sessionSQL, never()).createSession(anyLong(), any(), any(), any(), anyString());
    }

    @Test
    void post_endBeforeStart_returns400() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        String json = """
            {"classId":5,"startTime":"10:00","endTime":"09:00"}
        """;
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/sessions", json);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(true);

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("endTime must be after startTime"));
        verify(sessionSQL, never()).createSession(anyLong(), any(), any(), any(), anyString());
    }

    @Test
    void post_success_returns201_andCallsCreateSession() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        String json = """
            {"classId":5,"startTime":"10:00","endTime":"11:00"}
        """;
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/sessions", json);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(classSQL.isClassOwnedByTeacher(5L, 10L)).thenReturn(true);
        when(sessionSQL.createSession(eq(5L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class), anyString()))
                .thenReturn(999L);

        handler.handle(ex);

        assertEquals(201, ex.statusCode);
        String body = ex.responseBodyString();
        assertTrue(body.contains("\"sessionId\":999"));
        assertTrue(body.contains("\"classId\":5"));
        assertTrue(body.contains("\"startTime\":\"10:00\""));
        assertTrue(body.contains("\"endTime\":\"11:00\""));
        assertTrue(body.contains("\"code\":\""));

        // Verify date is "today" and start/end passed correctly
        ArgumentCaptor<LocalDate> dateCap = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalTime> startCap = ArgumentCaptor.forClass(LocalTime.class);
        ArgumentCaptor<LocalTime> endCap = ArgumentCaptor.forClass(LocalTime.class);
        ArgumentCaptor<String> codeCap = ArgumentCaptor.forClass(String.class);

        verify(sessionSQL).createSession(eq(5L), dateCap.capture(), startCap.capture(), endCap.capture(), codeCap.capture());

        assertEquals(LocalDate.now(), dateCap.getValue());
        assertEquals(LocalTime.parse("10:00"), startCap.getValue());
        assertEquals(LocalTime.parse("11:00"), endCap.getValue());
        assertNotNull(codeCap.getValue());
        assertEquals(8, codeCap.getValue().length());
    }

    @Test
    void post_invalidJson_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        ClassSQL classSQL = mock(ClassSQL.class);
        SessionSQL sessionSQL = mock(SessionSQL.class);

        var handler = new TeacherSessionsHandler(jwtService, classSQL, sessionSQL);
        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teacher/sessions", "{bad-json");
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRoleAndId("TEACHER", 10L);
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
    }

    // ---- helper ----
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
}