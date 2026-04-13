package frontend.auth;

import java.util.Objects;
import java.util.Set;

/**
 * Utility class for protecting routes based on authentication and roles.
 */
public final class RouteGuard {

    private RouteGuard() {
        // Prevent instantiation
    }

    /**
     * Ensures the user is authenticated and has one of the allowed roles.
     *
     * @param state   current authentication state
     * @param allowed set of allowed roles
     * @throws IllegalStateException if not authenticated
     * @throws SecurityException if role is not allowed
     */
    public static void require(AuthState state, Set<Role> allowed) {
        if (state == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        Objects.requireNonNull(allowed, "Allowed roles must not be null");

        if (!allowed.contains(state.getRole())) {
            throw new SecurityException("Access denied for role: " + state.getRole());
        }
    }

}