package frontend.auth;

import java.util.Objects;

/**
 * Utility class for resolving the default route based on user role.
 */
public final class RoleRedirect {

    private RoleRedirect() {
        // Prevent instantiation
    }

    /**
     * Returns the default route for a given role.
     *
     * @param role user role
     * @return route name
     */
    public static String routeFor(Role role) {
        Objects.requireNonNull(role, "Role must not be null");

        return switch (role) {
            case ADMIN -> "admin-dashboard";
            case TEACHER -> "teacher-dashboard";
            case STUDENT -> "student-dashboard";
        };
    }

    /**
     * Returns a safe route even if role is null.
     */
    public static String safeRouteFor(Role role) {
        if (role == null) {
            return "login"; // fallback route
        }
        return routeFor(role);
    }
}