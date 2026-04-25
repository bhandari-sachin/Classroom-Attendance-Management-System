package frontend.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleRedirectTest {

    @Test
    void testAdminRedirect() {
        String result = RoleRedirect.routeFor(Role.ADMIN);
        assertEquals("admin-dashboard", result, "ADMIN should route to admin-dashboard");
    }

    @Test
    void testTeacherRedirect() {
        String result = RoleRedirect.routeFor(Role.TEACHER);
        assertEquals("teacher-dashboard", result, "TEACHER should route to teacher-dashboard");
    }

    @Test
    void testStudentRedirect() {
        String result = RoleRedirect.routeFor(Role.STUDENT);
        assertEquals("student-dashboard", result, "STUDENT should route to student-dashboard");
    }
}