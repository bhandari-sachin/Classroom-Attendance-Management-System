package frontend.auth;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RouteGuardTest {

    @Test
    void require_shouldAllowAccess_whenRoleAllowed() {
        AuthState state = new AuthState("token123", Role.ADMIN, "Admin User");
        Set<Role> allowedRoles = Set.of(Role.ADMIN, Role.TEACHER);

        assertDoesNotThrow(() -> RouteGuard.require(state, allowedRoles));
    }

    @Test
    void require_shouldThrow_whenStateIsNull() {
        Set<Role> allowedRoles = Set.of(Role.ADMIN);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> RouteGuard.require(null, allowedRoles)
        );

        assertEquals("User is not authenticated", ex.getMessage());
    }

    @Test
    void require_shouldThrow_whenRoleNotAllowed() {
        AuthState state = new AuthState("token123", Role.STUDENT, "Student User");
        Set<Role> allowedRoles = Set.of(Role.ADMIN, Role.TEACHER);

        SecurityException ex = assertThrows(
                SecurityException.class,
                () -> RouteGuard.require(state, allowedRoles)
        );

        assertEquals("Access denied for role: STUDENT", ex.getMessage());
    }

    @Test
    void require_shouldAllow_whenSingleAllowedRoleMatches() {
        AuthState state = new AuthState("token123", Role.TEACHER, "Teacher");
        Set<Role> allowedRoles = Set.of(Role.TEACHER);

        assertDoesNotThrow(() -> RouteGuard.require(state, allowedRoles));
    }

    @Test
    void require_shouldThrow_whenAllowedRolesIsNull() {
        AuthState state = new AuthState("token123", Role.ADMIN, "Admin User");

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> RouteGuard.require(state, null)
        );

        assertEquals("Allowed roles must not be null", ex.getMessage());
    }
}