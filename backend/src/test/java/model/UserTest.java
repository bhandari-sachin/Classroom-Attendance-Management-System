package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testConstructorAndGetters() {
        User user = new User(
                1L,
                "john.doe@email.com",
                "hashedPassword123",
                "John",
                "Doe",
                UserRole.STUDENT,
                "STU001"
        );

        assertEquals(1L, user.getId());
        assertEquals("john.doe@email.com", user.getEmail());
        assertEquals("hashedPassword123", user.getPasswordHash());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(UserRole.STUDENT, user.getUserType());
        assertEquals("STU001", user.getStudentCode());
    }

    @Test
    void testStudentCodeNullForNonStudent() {
        User user = new User(
                2L,
                "teacher@email.com",
                "passHash",
                "Jane",
                "Smith",
                UserRole.TEACHER,
                null
        );

        assertNull(user.getStudentCode());
        assertEquals(UserRole.TEACHER, user.getUserType());
    }

    @Test
    void testAllFieldsRemainConsistent() {
        User user = new User(
                3L,
                "admin@email.com",
                "adminHash",
                "Admin",
                "User",
                UserRole.ADMIN,
                null
        );

        assertAll(
                () -> assertEquals(3L, user.getId()),
                () -> assertEquals("admin@email.com", user.getEmail()),
                () -> assertEquals("adminHash", user.getPasswordHash()),
                () -> assertEquals("Admin", user.getFirstName()),
                () -> assertEquals("User", user.getLastName()),
                () -> assertEquals(UserRole.ADMIN, user.getUserType()),
                () -> assertNull(user.getStudentCode())
        );
    }
}