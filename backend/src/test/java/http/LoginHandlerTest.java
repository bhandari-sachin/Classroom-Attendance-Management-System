package http;

import model.User;
import model.UserRole;
import org.junit.jupiter.api.Test;
import repository.UserRepository;
import security.JwtService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginHandlerTest {

    @Test
    void methodNotAllowed_returns405() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);

        LoginHandler handler = new LoginHandler(repo, jwt);

        var ex = mockHttp("GET", "/login", "");

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(405), anyLong());
    }

    @Test
    void invalidJson_returns400() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);

        LoginHandler handler = new LoginHandler(repo, jwt);

        var ex = mockHttp("POST", "/login", "invalid-json");

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void missingCredentials_returns400() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);

        LoginHandler handler = new LoginHandler(repo, jwt);

        var ex = mockHttp("POST", "/login",
                "{\"email\":\"\",\"password\":\"\"}");

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    void invalidCredentials_returns401() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);

        LoginHandler handler = new LoginHandler(repo, jwt);

        when(repo.findByEmail("test@test.com")).thenReturn(Optional.empty());

        var ex = mockHttp("POST", "/login",
                "{\"email\":\"test@test.com\",\"password\":\"1234\"}");

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(401), anyLong());
    }

    @Test
    void validLogin_returns200() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);

        LoginHandler handler = new LoginHandler(repo, jwt);

        String email = "test@test.com";
        String password = "pass";

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        String hash = encoder.encode(password);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn(email);
        when(user.getPasswordHash()).thenReturn(hash);
        when(user.getFirstName()).thenReturn("John");
        when(user.getLastName()).thenReturn("Doe");
        when(user.getUserType()).thenReturn(UserRole.STUDENT);

        when(repo.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwt.issueToken(1L, email, "STUDENT")).thenReturn("token123");

        var ex = mockHttp("POST", "/login",
                "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");

        handler.handle(ex);

        verify(ex).sendResponseHeaders(eq(200), anyLong());
    }

    // ---------------- helper ----------------

    private static com.sun.net.httpserver.HttpExchange mockHttp(String method, String path, String body) throws Exception {
        var ex = mock(com.sun.net.httpserver.HttpExchange.class);

        when(ex.getRequestMethod()).thenReturn(method);
        when(ex.getRequestURI()).thenReturn(URI.create(path));

        ByteArrayInputStream in = new ByteArrayInputStream(body.getBytes());
        when(ex.getRequestBody()).thenReturn(in);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(ex.getResponseBody()).thenReturn(out);

        when(ex.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());

        doNothing().when(ex).sendResponseHeaders(anyInt(), anyLong());
        doNothing().when(ex).close();

        return ex;
    }
}