package dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void testConstructorAndGetters() {
        AuthResponse response = new AuthResponse(
                "jwt-token-123",
                101L,
                "test@example.com",
                "ADMIN"
        );

        assertEquals("jwt-token-123", response.getToken());
        assertEquals(101L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void testImmutabilityBehavior() {
        AuthResponse response = new AuthResponse(
                "token-xyz",
                202L,
                "user@example.com",
                "USER"
        );

        // Since fields are final, values should remain unchanged
        assertEquals("token-xyz", response.getToken());
        assertEquals(202L, response.getUserId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("USER", response.getRole());
    }
}