package auth;

import frontend.auth.AuthState;
import frontend.auth.Role;
import frontend.auth.RouteGuard;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RouteGuardTest {

    @Test
    void require_shouldAllowAccess_whenRoleAllowed() {

        AuthState state = new AuthState("token123", Role.ADMIN, "Admin User");

        assertDoesNotThrow(() ->
                RouteGuard.require(state, Set.of(Role.ADMIN, Role.TEACHER))
        );
    }

    @Test
    void require_shouldThrow_whenStateIsNull() {

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                RouteGuard.require(null, Set.of(Role.ADMIN))
        );

        assertEquals("Not authenticated", ex.getMessage());
    }

    @Test
    void require_shouldThrow_whenRoleNotAllowed() {

        AuthState state = new AuthState("token123", Role.STUDENT, "Student User");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                RouteGuard.require(state, Set.of(Role.ADMIN, Role.TEACHER))
        );

        assertTrue(ex.getMessage().contains("Forbidden"));
    }

    @Test
    void require_shouldAllow_whenSingleAllowedRoleMatches() {

        AuthState state = new AuthState("token123", Role.TEACHER, "Teacher");

        assertDoesNotThrow(() ->
                RouteGuard.require(state, Set.of(Role.TEACHER))
        );
    }
}