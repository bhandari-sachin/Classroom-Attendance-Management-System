package frontend.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthStateTest {

    @Test
    void constructor_shouldCreateValidState() {
        AuthState state = new AuthState("token123", Role.ADMIN, "Farah");

        assertEquals("token123", state.getToken());
        assertEquals(Role.ADMIN, state.getRole());
        assertEquals("Farah", state.getName());
    }

    @Test
    void constructor_shouldAllowNullName() {
        AuthState state = new AuthState("token123", Role.STUDENT, null);

        assertEquals("token123", state.getToken());
        assertEquals(Role.STUDENT, state.getRole());
        assertNull(state.getName());
    }

    @Test
    void constructor_shouldThrowWhenTokenNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AuthState(null, Role.ADMIN, "Farah")
        );

        assertEquals("Token must not be null or blank", ex.getMessage());
    }

    @Test
    void constructor_shouldThrowWhenTokenBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AuthState("   ", Role.ADMIN, "Farah")
        );

        assertEquals("Token must not be null or blank", ex.getMessage());
    }

    @Test
    void constructor_shouldThrowWhenRoleNull() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new AuthState("token123", null, "Farah")
        );

        assertEquals("Role must not be null", ex.getMessage());
    }

    @Test
    void getters_shouldReturnCorrectValues() {
        AuthState state = new AuthState("abc", Role.TEACHER, "John");

        assertEquals("abc", state.getToken());
        assertEquals(Role.TEACHER, state.getRole());
        assertEquals("John", state.getName());
    }
}