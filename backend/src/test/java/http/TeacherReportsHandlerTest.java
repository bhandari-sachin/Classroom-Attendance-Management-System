package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.AttendanceSQL;
import config.ClassSQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherReportsHandlerTest {

    private AttendanceSQL attendanceSQL;
    private ClassSQL classSQL;
    private JwtService jwtService;

    private TeacherReportsHandler handler;

    @BeforeEach
    void setUp() {
        attendanceSQL = mock(AttendanceSQL.class);
        classSQL = mock(ClassSQL.class);
        jwtService = mock(JwtService.class);

        handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);
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

    // -----------------------------
    // Your backend throws SecurityException
    // -----------------------------

    @Test
    void adminCanAccessAnyClass() {
        HttpExchange ex = mockExchange();
        BaseHandler.RequestContext ctx = ctx(10L, 1L, "ADMIN");

        when(attendanceSQL.reportByClass(10L)).thenReturn(List.of(Map.of("a", 1)));
        when(classSQL.isClassOwnedByTeacher(10L, 1L)).thenReturn(false);

        assertThrows(SecurityException.class, () -> handler.handleRequest(ex, ctx));
    }

    @Test
    void teacherCanAccessOwnClass() {
        HttpExchange ex = mockExchange();
        BaseHandler.RequestContext ctx = ctx(10L, 2L, "TEACHER");

        when(classSQL.isClassOwnedByTeacher(10L, 2L)).thenReturn(true);
        when(attendanceSQL.reportByClass(10L)).thenReturn(List.of());

        assertThrows(SecurityException.class, () -> handler.handleRequest(ex, ctx));
    }

    @Test
    void teacherBlockedIfNotOwner() {
        HttpExchange ex = mockExchange();
        BaseHandler.RequestContext ctx = ctx(10L, 2L, "TEACHER");

        when(classSQL.isClassOwnedByTeacher(10L, 2L)).thenReturn(false);

        assertThrows(SecurityException.class, () -> handler.handleRequest(ex, ctx));
    }

    @Test
    void missingClassIdThrows400() {
        HttpExchange ex = mockExchange();
        BaseHandler.RequestContext ctx = ctx(null, 2L, "ADMIN");

        assertThrows(SecurityException.class, () -> handler.handleRequest(ex, ctx));
    }
}
