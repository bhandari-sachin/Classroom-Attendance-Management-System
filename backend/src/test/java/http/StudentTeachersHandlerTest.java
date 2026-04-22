package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import repository.UserRepository;
import security.Auth;
import security.JwtService;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class StudentTeachersHandlerTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private StudentTeachersHandler handler;

    private HttpExchange exchange;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);

        handler = new StudentTeachersHandler(jwtService, userRepository);

        exchange = mock(HttpExchange.class);
        responseBody = new ByteArrayOutputStream();

        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
    }

    private void request(String method, String path) throws Exception {
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(new URI(path));
    }

    private DecodedJWT mockJwt(Long id) {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);

        when(jwt.getClaim("id")).thenReturn(claim);
        when(claim.asLong()).thenReturn(id);

        return jwt;
    }

    // ---------------------------------------
    // SUCCESS CASE
    // ---------------------------------------

    @Test
    void success_returnsTeachersList() throws Exception {
        request("GET", "/api/teachers");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            DecodedJWT jwt = mockJwt(1L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);

            auth.when(() -> Auth.requireRole(any(), any()))
                    .thenAnswer(inv -> null);

            when(userRepository.findAllTeachers())
                    .thenReturn(List.of(
                            Map.of(
                                    "id", "1",
                                    "name", "John"
                            )
                    ));
            handler.handle(exchange);

            verify(userRepository).findAllTeachers();
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
    }

    // ---------------------------------------
    // AUTH FAILURE
    // ---------------------------------------

    @Test
    void authFails_returns403() throws Exception {
        request("GET", "/api/teachers");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenThrow(new SecurityException("Invalid token"));

            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(403), anyLong());
            assertTrue(responseBody.toString().contains("Invalid token"));
        }
    }

    // ---------------------------------------
    // METHOD NOT ALLOWED (from BaseHandler)
    // ---------------------------------------

    @Test
    void wrongMethod_returns405() throws Exception {
        request("POST", "/api/teachers");

        try (MockedStatic<Auth> auth = mockStatic(Auth.class)) {
            DecodedJWT jwt = mockJwt(5L);

            auth.when(() -> Auth.requireJwt(any(), any()))
                    .thenReturn(jwt);


            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(eq(405), anyLong());
            assertTrue(responseBody.toString().contains("Method Not Allowed"));
        }
    }
}