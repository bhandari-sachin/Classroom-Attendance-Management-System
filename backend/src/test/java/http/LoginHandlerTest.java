package http;

import model.User;
import model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import repository.UserRepository;
import security.JwtService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginHandlerTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void methodNotAllowed_returns405() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new LoginHandler(users, jwtService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/login", null);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        assertEquals("Method Not Allowed", ex.responseBodyString());
    }

    @Test
    void invalidJson_returns400() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new LoginHandler(users, jwtService);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/login", "{not-json");

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Invalid JSON"));
    }

    @Test
    void missingEmailOrPassword_returns400() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new LoginHandler(users, jwtService);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/login", "{\"email\":\"\",\"password\":\"   \"}");

        handler.handle(ex);

        assertEquals(400, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Email and password are required"));
        verify(users, never()).findByEmail(anyString());
    }

    @Test
    void userNotFound_returns401() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new LoginHandler(users, jwtService);

        when(users.findByEmail("user@x.com")).thenReturn(Optional.empty());

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/login", "{\"email\":\"user@x.com\",\"password\":\"pass\"}");

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Invalid credentials"));
        verify(jwtService, never()).issueToken(anyLong(), anyString(), anyString());
    }

    @Test
    void wrongPassword_returns401() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new LoginHandler(users, jwtService);

        User u = mockUser(1L, "user@x.com", UserRole.STUDENT, "Sam", "Student",
                encoder.encode("correct-password"));

        when(users.findByEmail("user@x.com")).thenReturn(Optional.of(u));

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/login", "{\"email\":\"user@x.com\",\"password\":\"wrong\"}");

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Invalid credentials"));
        verify(jwtService, never()).issueToken(anyLong(), anyString(), anyString());
    }

    @Test
    void success_returns200_withTokenAndUserInfo() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new LoginHandler(users, jwtService);

        User u = mockUser(7L, "user@x.com", UserRole.ADMIN, "System", "Admin",
                encoder.encode("secret"));

        when(users.findByEmail("user@x.com")).thenReturn(Optional.of(u));
        when(jwtService.issueToken(7L, "user@x.com", "ADMIN")).thenReturn("TOKEN123");

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/login", "{\"email\":\"user@x.com\",\"password\":\"secret\"}");

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        String body = ex.responseBodyString();

        assertTrue(body.contains("\"token\":\"TOKEN123\""));
        assertTrue(body.contains("\"userId\":7"));
        assertTrue(body.contains("\"email\":\"user@x.com\""));
        assertTrue(body.contains("\"role\":\"ADMIN\""));
        assertTrue(body.contains("\"name\":\"System Admin\""));

        verify(jwtService).issueToken(7L, "user@x.com", "ADMIN");
    }

    @Test
    void normalizesEmail_trimAndLowercase() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new LoginHandler(users, jwtService);

        User u = mockUser(2L, "user@x.com", UserRole.TEACHER, "Tina", "Teacher",
                encoder.encode("pw"));

        // IMPORTANT: handler will normalize to "user@x.com"
        when(users.findByEmail("user@x.com")).thenReturn(Optional.of(u));
        when(jwtService.issueToken(2L, "user@x.com", "TEACHER")).thenReturn("TOK");

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/login", "{\"email\":\"  USER@X.COM  \",\"password\":\"pw\"}");

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("\"token\":\"TOK\""));

        verify(users).findByEmail("user@x.com");
    }

    // ---- helper ----
    private static User mockUser(long id, String email, UserRole role,
                                 String first, String last, String hash) {
        User u = mock(User.class);
        when(u.getId()).thenReturn(id);
        when(u.getEmail()).thenReturn(email);
        when(u.getUserType()).thenReturn(role); // must be enum
        when(u.getFirstName()).thenReturn(first);
        when(u.getLastName()).thenReturn(last);
        when(u.getPasswordHash()).thenReturn(hash);
        return u;
    }
}