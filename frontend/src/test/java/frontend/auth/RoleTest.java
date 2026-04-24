package frontend.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    // ===== fromString() tests =====

    @Test
    void fromString_shouldReturnAdmin_whenValidAdmin() {
        assertEquals(Role.ADMIN, Role.fromString("ADMIN"));
    }

    @Test
    void fromString_shouldBeCaseInsensitive() {
        assertEquals(Role.TEACHER, Role.fromString("teacher"));
        assertEquals(Role.STUDENT, Role.fromString("StUdEnT"));
    }

    @Test
    void fromString_shouldTrimSpaces() {
        assertEquals(Role.ADMIN, Role.fromString("  admin  "));
    }

    @Test
    void fromString_shouldReturnStudent_whenNull() {
        assertEquals(Role.STUDENT, Role.fromString(null));
    }

    @Test
    void fromString_shouldReturnStudent_whenBlank() {
        assertEquals(Role.STUDENT, Role.fromString("   "));
    }

    @Test
    void fromString_shouldReturnStudent_whenInvalidValue() {
        assertEquals(Role.STUDENT, Role.fromString("INVALID_ROLE"));
    }

    // ===== isAdmin() tests =====

    @Test
    void isAdmin_shouldReturnTrue_onlyForAdmin() {
        assertTrue(Role.ADMIN.isAdmin());
        assertFalse(Role.TEACHER.isAdmin());
        assertFalse(Role.STUDENT.isAdmin());
    }

    // ===== isTeacher() tests =====

    @Test
    void isTeacher_shouldReturnTrue_onlyForTeacher() {
        assertTrue(Role.TEACHER.isTeacher());
        assertFalse(Role.ADMIN.isTeacher());
        assertFalse(Role.STUDENT.isTeacher());
    }

    // ===== isStudent() tests =====

    @Test
    void isStudent_shouldReturnTrue_onlyForStudent() {
        assertTrue(Role.STUDENT.isStudent());
        assertFalse(Role.ADMIN.isStudent());
        assertFalse(Role.TEACHER.isStudent());
    }
}