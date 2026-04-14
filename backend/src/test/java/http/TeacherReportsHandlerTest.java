package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.AttendanceSQL;
import config.ClassSQL;
import backend.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherReportsHandlerTest {

    private JwtService jwtService;
    private AttendanceSQL attendanceSQL;
    private ClassSQL classSQL;
    private TeacherReportsHandler handler;

    private HttpExchange exchange;
    private BaseHandler.RequestContext ctx;

    private DecodedJWT jwt;
    private Claim roleClaim;

    // ✅ FIX ADDED
    private Headers headers;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        attendanceSQL = mock(AttendanceSQL.class);
        classSQL = mock(ClassSQL.class);

        handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);

        exchange = mock(HttpExchange.class);
        ctx = mock(BaseHandler.RequestContext.class);

        jwt = mock(DecodedJWT.class);
        roleClaim = mock(Claim.class);

        // ✅ FIX ADDED (HEADERS MOCK)
        headers = mock(Headers.class);

        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Authorization")).thenReturn("Bearer test-token");

        when(ctx.getJwt()).thenReturn(jwt);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        when(roleClaim.isNull()).thenReturn(false);
        when(roleClaim.asString()).thenReturn("TEACHER");
    }

    @Test
    void shouldReturnReport_whenTeacherOwnsClass() throws IOException {
        Long classId = 1L;
        Long teacherId = 10L;

        when(ctx.getClassId()).thenReturn(classId);
        when(ctx.getUserId()).thenReturn(teacherId);

        when(classSQL.isClassOwnedByTeacher(classId, teacherId)).thenReturn(true);
        when(attendanceSQL.reportByClass(classId)).thenReturn(List.of());

        handler.handleRequest(exchange, ctx);

        verify(attendanceSQL).reportByClass(classId);
    }

    @Test
    void shouldThrow400_whenClassIdMissing() {
        when(ctx.getClassId()).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(400, ex.getStatus());    }

    @Test
    void shouldThrow403_whenTeacherDoesNotOwnClass() {
        Long classId = 1L;
        Long teacherId = 10L;

        when(ctx.getClassId()).thenReturn(classId);
        when(ctx.getUserId()).thenReturn(teacherId);

        when(classSQL.isClassOwnedByTeacher(classId, teacherId)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(400, ex.getStatus());    }

    @Test
    void shouldReturnReport_whenAdmin() throws IOException {
        Long classId = 1L;

        when(ctx.getClassId()).thenReturn(classId);
        when(ctx.getUserId()).thenReturn(99L);

        when(roleClaim.asString()).thenReturn("ADMIN");

        when(attendanceSQL.reportByClass(classId)).thenReturn(List.of());

        handler.handleRequest(exchange, ctx);

        verify(attendanceSQL).reportByClass(classId);
        verify(classSQL, never()).isClassOwnedByTeacher(any(), any());
    }
}