package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import model.User;
import model.UserRole;
import repository.UserRepository;
import security.JwtService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUsersHandlerTest {

    @Test
    void methodNotAllowed_returns405() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new AdminUsersHandler(users, jwtService);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/admin/users", null);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
    }

    @Test
    void missingAuthorization_returns401() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new AdminUsersHandler(users, jwtService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/admin/users", null);
        // no Authorization header

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verify(users, never()).findAll();
    }

    @Test
    void nonAdminRole_returns401() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new AdminUsersHandler(users, jwtService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/admin/users", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("TEACHER");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden for role"));
        verify(users, never()).findAll();
    }

    @Test
    void get_admin_returns200_withExpectedShape() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new AdminUsersHandler(users, jwtService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/admin/users", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("ADMIN");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(users.countByRole(UserRole.STUDENT)).thenReturn(2);
        when(users.countByRole(UserRole.TEACHER)).thenReturn(1);
        when(users.countByRole(UserRole.ADMIN)).thenReturn(1);

        User u1 = mockUser("Sam", "Student", "student1@school.com", UserRole.STUDENT);
        User u2 = mockUser("Tina", "Teacher", "teacher@school.com", UserRole.TEACHER);
        when(users.findAll()).thenReturn(List.of(u1, u2));

        handler.handle(ex);

        assertEquals(200, ex.statusCode);

        String body = ex.responseBodyString();
        // Top-level keys
        assertTrue(body.contains("\"students\":2"));
        assertTrue(body.contains("\"teachers\":1"));
        assertTrue(body.contains("\"admins\":1"));
        assertTrue(body.contains("\"users\""));

        // User objects shape
        assertTrue(body.contains("\"name\":\"Sam Student\""));
        assertTrue(body.contains("\"email\":\"student1@school.com\""));
        assertTrue(body.contains("\"role\":\"STUDENT\""));
        assertTrue(body.contains("\"enrolled\":\"-\""));

        assertTrue(body.contains("\"name\":\"Tina Teacher\""));
        assertTrue(body.contains("\"email\":\"teacher@school.com\""));
        assertTrue(body.contains("\"role\":\"TEACHER\""));

        verify(users).findAll();
    }

    @Test
    void repoThrows_returns500() throws Exception {
        UserRepository users = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        var handler = new AdminUsersHandler(users, jwtService);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/admin/users", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("ADMIN");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(users.countByRole(UserRole.STUDENT)).thenThrow(new RuntimeException("DB down"));

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
    }

    // ---- helpers ----

    private static DecodedJWT jwtWithRole(String role) {
        DecodedJWT jwt = mock(DecodedJWT.class);

        Claim roleClaim = mock(Claim.class);
        when(roleClaim.asString()).thenReturn(role);
        when(jwt.getClaim("role")).thenReturn(roleClaim);

        // Safe defaults
        Claim idClaim = mock(Claim.class);
        when(idClaim.asLong()).thenReturn(1L);
        when(jwt.getClaim("id")).thenReturn(idClaim);

        when(jwt.getSubject()).thenReturn("1");
        return jwt;
    }

    private static User mockUser(String first, String last, String email, UserRole role) {
        User u = mock(User.class);
        when(u.getFirstName()).thenReturn(first);
        when(u.getLastName()).thenReturn(last);
        when(u.getEmail()).thenReturn(email);
        // your handler uses u.getUserType().name()
        // so getUserType() must return an enum that has name()
        when(u.getUserType()).thenReturn(role);
        return u;
    }
}