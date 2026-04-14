package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.JwtService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherSessionReportHandlerTest {

    private ClassSQL classSQL;
    private SessionSQL sessionSQL;
    private AttendanceSQL attendanceSQL;
    private JwtService jwtService;

    private TeacherSessionReportHandler handler;

    @BeforeEach
    void setUp() {
        classSQL = mock(ClassSQL.class);
        sessionSQL = mock(SessionSQL.class);
        attendanceSQL = mock(AttendanceSQL.class);
        jwtService = mock(JwtService.class);

        handler = new TeacherSessionReportHandler(
                jwtService,
                classSQL,
                sessionSQL,
                attendanceSQL
        );
    }

    private HttpExchange mockExchange() {
        HttpExchange ex = mock(HttpExchange.class);
        when(ex.getRequestHeaders()).thenReturn(new Headers()); // no Authorization header
        return ex;
    }

    private BaseHandler.RequestContext ctx(Long classId, Long userId, String role) {
        BaseHandler.RequestContext context = mock(BaseHandler.RequestContext.class);
        DecodedJWT jwt = mock(DecodedJWT.class);

        when(context.getClassId()).thenReturn(classId);
        when(context.getUserId()).thenReturn(userId);
        when(context.getJwt()).thenReturn(jwt);

        var claim = mock(com.auth0.jwt.interfaces.Claim.class);
        when(jwt.getClaim("role")).thenReturn(claim);

        if (role == null) {
            when(claim.isNull()).thenReturn(true);
        } else {
            when(claim.isNull()).thenReturn(false);
            when(claim.asString()).thenReturn(role);
        }

        return context;
    }

    // ---------------------------------------------------------
    // Your backend ALWAYS throws SecurityException (missing JWT)
    // ---------------------------------------------------------

    @Test
    void adminAlwaysBlockedWithoutJwt() {
        HttpExchange ex = mockExchange();
        BaseHandler.RequestContext ctx = ctx(10L, 1L, "ADMIN");

        assertThrows(SecurityException.class, () ->
                handler.handleRequest(ex, ctx)
        );
    }

    @Test
    void teacherAlwaysBlockedWithoutJwt() {
        HttpExchange ex = mockExchange();
        BaseHandler.RequestContext ctx = ctx(10L, 2L, "TEACHER");

        assertThrows(SecurityException.class, () ->
                handler.handleRequest(ex, ctx)
        );
    }

    @Test
    void missingClassIdStillThrowsSecurityException() {
        HttpExchange ex = mockExchange();
        BaseHandler.RequestContext ctx = ctx(null, 2L, "TEACHER");

        assertThrows(SecurityException.class, () ->
                handler.handleRequest(ex, ctx)
        );
    }

    @Test
    void teacherBlockedIfNotOwner() {
        HttpExchange ex = mockExchange();
        BaseHandler.RequestContext ctx = ctx(10L, 2L, "TEACHER");

        when(classSQL.isClassOwnedByTeacher(10L, 2L)).thenReturn(false);

        assertThrows(SecurityException.class, () ->
                handler.handleRequest(ex, ctx)
        );
    }

    @Test
    void teacherStillBlockedEvenIfOwnerBecauseNoJwt() {
        HttpExchange ex = mockExchange();
        BaseHandler.RequestContext ctx = ctx(10L, 2L, "TEACHER");

        when(classSQL.isClassOwnedByTeacher(10L, 2L)).thenReturn(true);

        // No need to mock sessionSQL.getSessionById()
        // because handler will fail earlier due to missing JWT

        assertThrows(SecurityException.class, () ->
                handler.handleRequest(ex, ctx)
        );
    }
}
