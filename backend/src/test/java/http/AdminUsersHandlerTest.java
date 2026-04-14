package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.Test;
import repository.UserRepository;
import security.JwtService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUsersHandlerTest {

    @Test
    void admin_returns200_withCorrectData() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);

        AdminUsersHandler handler = new AdminUsersHandler(repo, jwt);

        AdminClassesHandlerTest.FakeExchange ex =
                new AdminClassesHandlerTest.FakeExchange("GET", "/admin/users", null);

        ex.getRequestHeaders().set("Authorization", "Bearer token");

        when(jwt.verify("token")).thenReturn(mockAdminJwt());

        when(repo.countByRole(UserRole.STUDENT)).thenReturn(2);
        when(repo.countByRole(UserRole.TEACHER)).thenReturn(1);
        when(repo.countByRole(UserRole.ADMIN)).thenReturn(1);

        User u1 = mockUser("Sam", "Student", "s@school.com", UserRole.STUDENT);
        User u2 = mockUser("Tina", "Teacher", "t@school.com", UserRole.TEACHER);

        when(repo.findAll()).thenReturn(List.of(u1, u2));

        handler.handle(ex);

        assertEquals(200, ex.statusCode);

        String body = ex.responseBodyString();

        assertTrue(body.contains("\"students\":2"));
        assertTrue(body.contains("\"teachers\":1"));
        assertTrue(body.contains("\"admins\":1"));

        assertTrue(body.contains("Sam Student"));
        assertTrue(body.contains("Tina Teacher"));
    }

    // ---------------- helpers ----------------

    private static DecodedJWT mockAdminJwt() {
        DecodedJWT jwt = mock(DecodedJWT.class);

        Claim roleClaim = mock(Claim.class);
        when(roleClaim.asString()).thenReturn("ADMIN");
        when(jwt.getClaim("role")).thenReturn(roleClaim);

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
        when(u.getUserType()).thenReturn(role);
        return u;
    }
}