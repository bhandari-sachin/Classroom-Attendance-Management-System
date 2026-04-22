package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.AttendanceSQL;
import config.ClassSQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class TeacherReportsHandlerTest {

    private JwtService jwtService;
    private AttendanceSQL attendanceSQL;
    private ClassSQL classSQL;

    private TeacherReportsHandler handler;

    private HttpExchange ex;
    private BaseHandler.RequestContext ctx;

    private DecodedJWT jwt;
    private Claim roleClaim;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        attendanceSQL = mock(AttendanceSQL.class);
        classSQL = mock(ClassSQL.class);

        handler = new TeacherReportsHandler(jwtService, attendanceSQL, classSQL);

        ex = mock(HttpExchange.class);
        ctx = mock(BaseHandler.RequestContext.class);

        jwt = mock(DecodedJWT.class);
        roleClaim = mock(Claim.class);

        // FIX: mock request headers to avoid NullPointerException
        Headers requestHeaders = new Headers();
        requestHeaders.add("Authorization", "Bearer token123");
        when(ex.getRequestHeaders()).thenReturn(requestHeaders);

        // Response headers + body
        when(ex.getResponseHeaders()).thenReturn(new Headers());
        when(ex.getResponseBody()).thenReturn(new ByteArrayOutputStream());

        // JwtService must return our mocked JWT
        when(jwtService.verify("token123")).thenReturn(jwt);
    }

    @Test
    void shouldReturn200_andReportData_forAdmin() throws Exception {

        // Context
        when(ctx.getClassId()).thenReturn(1L);
        when(ctx.getUserId()).thenReturn(10L);
        when(ctx.getJwt()).thenReturn(jwt);

        // ADMIN role
        when(jwt.getClaim("role")).thenReturn(roleClaim);
        when(roleClaim.isNull()).thenReturn(false);
        when(roleClaim.asString()).thenReturn("ADMIN");

        // SQL mock
        when(attendanceSQL.reportByClass(1L))
                .thenReturn(List.of(Map.of("studentId", 1, "present", 5)));

        // ACT
        handler.handleRequest(ex, ctx);

        // VERIFY
        verify(attendanceSQL, times(1)).reportByClass(1L);
    }
}
