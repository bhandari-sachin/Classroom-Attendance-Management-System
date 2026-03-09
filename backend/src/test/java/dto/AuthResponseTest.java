package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void getToken() {
        AuthResponse response =
                new AuthResponse("jwt-token-123", 10L, "user@test.com", "ADMIN");

        assertEquals("jwt-token-123", response.getToken());
    }

    @Test
    void getUserId() {
        AuthResponse response =
                new AuthResponse("jwt-token-123", 10L, "user@test.com", "ADMIN");

        assertEquals(10L, response.getUserId());
    }

    @Test
    void getEmail() {
        AuthResponse response =
                new AuthResponse("jwt-token-123", 10L, "user@test.com", "ADMIN");

        assertEquals("user@test.com", response.getEmail());
    }

    @Test
    void getRole() {
        AuthResponse response =
                new AuthResponse("jwt-token-123", 10L, "user@test.com", "ADMIN");

        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void allFields_areSetCorrectly() {
        AuthResponse response =
                new AuthResponse("abc123", 99L, "boss@mail.com", "TEACHER");

        assertAll(
                () -> assertEquals("abc123", response.getToken()),
                () -> assertEquals(99L, response.getUserId()),
                () -> assertEquals("boss@mail.com", response.getEmail()),
                () -> assertEquals("TEACHER", response.getRole())
        );
    }
}