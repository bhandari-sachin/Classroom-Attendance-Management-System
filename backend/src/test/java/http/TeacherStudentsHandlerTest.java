package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.ClassSQL;
import backend.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherStudentsHandlerTest {

    private JwtService jwtService;
    private ClassSQL classSQL;
    private TeacherStudentsHandler handler;

    private HttpExchange exchange;
    private BaseHandler.RequestContext ctx;
    private DecodedJWT jwt;
    private Claim roleClaim;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        classSQL = mock(ClassSQL.class);

        handler = new TeacherStudentsHandler(jwtService, classSQL);

        exchange = mock(HttpExchange.class);

        // =========================
        // ✅ FIX: prevent null headers crash
        // =========================
        Headers headers = new Headers();
        headers.add("Authorization", "Bearer dummy");

        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(URI.create("/test"));
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());

        ctx = mock(BaseHandler.RequestContext.class);
        jwt = mock(DecodedJWT.class);
        roleClaim = mock(Claim.class);

        when(ctx.getJwt()).thenReturn(jwt);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        when(roleClaim.isNull()).thenReturn(false);
        when(roleClaim.asString()).thenReturn("TEACHER");
    }

    // =========================
    // SUCCESS CASE
    // =========================

    @Test
    void shouldReturnStudents_whenTeacherOwnsClass() throws IOException {
        Long classId = 1L;
        Long teacherId = 10L;

        when(ctx.getLongQuery("classId")).thenReturn(classId);
        when(ctx.getUserId()).thenReturn(teacherId);

        when(classSQL.isClassOwnedByTeacher(classId, teacherId)).thenReturn(true);
        when(classSQL.listStudentsForClass(classId)).thenReturn(List.of());

        handler.handleRequest(exchange, ctx);

        verify(classSQL).listStudentsForClass(classId);
    }

    // =========================
    // 400 missing classId
    // =========================

    @Test
    void shouldThrow400_whenClassIdMissing() {
        when(ctx.getLongQuery("classId")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(400, ex.getStatus());
    }

    // =========================
    // 403 not owner
    // =========================

    @Test
    void shouldThrow403_whenNotOwner() {
        Long classId = 1L;
        Long teacherId = 10L;

        when(ctx.getLongQuery("classId")).thenReturn(classId);
        when(ctx.getUserId()).thenReturn(teacherId);

        when(classSQL.isClassOwnedByTeacher(classId, teacherId)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () ->
                handler.handleRequest(exchange, ctx)
        );

        assertEquals(403, ex.getStatus());
    }

    // =========================
    // ADMIN bypass
    // =========================

    @Test
    void shouldAllowAdmin_withoutOwnershipCheck() throws IOException {
        Long classId = 1L;

        when(roleClaim.asString()).thenReturn("ADMIN");

        when(ctx.getLongQuery("classId")).thenReturn(classId);
        when(ctx.getUserId()).thenReturn(99L);

        when(classSQL.listStudentsForClass(classId)).thenReturn(List.of());

        handler.handleRequest(exchange, ctx);

        verify(classSQL, never()).isClassOwnedByTeacher(any(), any());
        verify(classSQL).listStudentsForClass(classId);
    }
}