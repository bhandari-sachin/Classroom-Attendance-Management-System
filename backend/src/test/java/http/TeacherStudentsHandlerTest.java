package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.ClassSQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TeacherStudentsHandlerTest {

    private JwtService jwtService;
    private ClassSQL classSQL;
    private TeacherStudentsHandler handler;

    private HttpExchange exchange;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        classSQL = mock(ClassSQL.class);

        handler = new TeacherStudentsHandler(jwtService, classSQL);

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    private void request(String path) throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI(path));
    }

    private DecodedJWT mockJwt(long id, String role) {
        DecodedJWT jwt = mock(DecodedJWT.class);

        Claim idClaim = mock(Claim.class);
        Claim roleClaim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(idClaim);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        when(idClaim.isNull()).thenReturn(false);
        when(idClaim.asLong()).thenReturn(id);

        when(roleClaim.isNull()).thenReturn(false);
        when(roleClaim.asString()).thenReturn(role);

        return jwt;
    }

    @Test
    void success_returnsStudents() throws Exception {

        request("/api/teacher/students?classId=10");

        DecodedJWT jwt = mockJwt(5L, "TEACHER");

        try (MockedStatic<security.Auth> auth = mockStatic(security.Auth.class)) {

            auth.when(() -> security.Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> security.Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(classSQL.isClassOwnedByTeacher(10L, 5L))
                    .thenReturn(true);

            when(classSQL.listStudentsForClass(10L))
                    .thenReturn(List.of(
                            Map.of("id", 1, "name", "Alice"),
                            Map.of("id", 2, "name", "Bob")
                    ));

            handler.handle(exchange);
        }

        String body = responseBody.toString();

        assertTrue(body.contains("data"));
        assertTrue(body.contains("Alice"));
        assertTrue(body.contains("Bob"));
    }

    @Test
    void missingClassId_returns400() throws Exception {

        request("/api/teacher/students");

        DecodedJWT jwt = mockJwt(5L, "TEACHER");

        try (MockedStatic<security.Auth> auth = mockStatic(security.Auth.class)) {

            auth.when(() -> security.Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> security.Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            handler.handle(exchange);
        }

        String body = responseBody.toString();
        assertTrue(body.contains("classId is required"));
    }

    @Test
    void forbidden_when_not_owner() throws Exception {

        request("/api/teacher/students?classId=10");

        DecodedJWT jwt = mockJwt(5L, "TEACHER");

        try (MockedStatic<security.Auth> auth = mockStatic(security.Auth.class)) {

            auth.when(() -> security.Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> security.Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(classSQL.isClassOwnedByTeacher(10L, 5L))
                    .thenReturn(false);

            handler.handle(exchange);
        }

        String body = responseBody.toString();
        assertTrue(body.contains("Forbidden"));
    }
}