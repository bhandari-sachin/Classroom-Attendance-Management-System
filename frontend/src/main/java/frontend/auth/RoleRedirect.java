package frontend.auth;

public class RoleRedirect {

    public static String routeFor(Role role) {
        return switch (role) {
            case ADMIN -> "admin-dashboard";
            case TEACHER -> "teacher-dashboard";
            case STUDENT -> "student-dashboard";
        };
    }
}