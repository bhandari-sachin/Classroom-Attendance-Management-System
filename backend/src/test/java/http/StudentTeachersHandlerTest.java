package http;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import repository.UserRepository;
import security.JwtService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentTeachersHandlerTest {

    @Test
    void missingAuthorization_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserRepository repo = mock(UserRepository.class);
        var handler = new StudentTeachersHandler(jwtService, repo);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teachers", null);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Missing Authorization header"));
        verify(repo, never()).findAllTeachers();
    }

    @Test
    void methodNotAllowed_returns405() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserRepository repo = mock(UserRepository.class);
        var handler = new StudentTeachersHandler(jwtService, repo);

        var ex = new AdminClassesHandlerTest.FakeExchange("POST", "/teachers", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("STUDENT");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(405, ex.statusCode);
        verify(repo, never()).findAllTeachers();
    }


    @Test
    void get_asTeacher_returns200() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserRepository repo = mock(UserRepository.class);
        var handler = new StudentTeachersHandler(jwtService, repo);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teachers", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("TEACHER");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(repo.findAllTeachers()).thenReturn(List.of());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(repo).findAllTeachers();
    }

    @Test
    void get_asAdmin_returns200() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserRepository repo = mock(UserRepository.class);
        var handler = new StudentTeachersHandler(jwtService, repo);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teachers", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("ADMIN");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(repo.findAllTeachers()).thenReturn(List.of());

        handler.handle(ex);

        assertEquals(200, ex.statusCode);
        verify(repo).findAllTeachers();
    }

    @Test
    void get_withUnauthorizedRole_returns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserRepository repo = mock(UserRepository.class);
        var handler = new StudentTeachersHandler(jwtService, repo);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teachers", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("GUEST");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        handler.handle(ex);

        assertEquals(401, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Forbidden for role"));
        verify(repo, never()).findAllTeachers();
    }

    @Test
    void repoThrows_returns500() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserRepository repo = mock(UserRepository.class);
        var handler = new StudentTeachersHandler(jwtService, repo);

        var ex = new AdminClassesHandlerTest.FakeExchange("GET", "/teachers", null);
        ex.getRequestHeaders().set("Authorization", "Bearer test-token");

        DecodedJWT jwt = jwtWithRole("STUDENT");
        when(jwtService.verify("test-token")).thenReturn(jwt);

        when(repo.findAllTeachers()).thenThrow(new RuntimeException("DB down"));

        handler.handle(ex);

        assertEquals(500, ex.statusCode);
        assertTrue(ex.responseBodyString().contains("Server error"));
    }

    // ---- helper ----
    private static DecodedJWT jwtWithRole(String role) {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim roleClaim = mock(Claim.class);
        when(roleClaim.asString()).thenReturn(role);
        when(jwt.getClaim("role")).thenReturn(roleClaim);
        when(jwt.getSubject()).thenReturn("1");
        return jwt;
    }
}