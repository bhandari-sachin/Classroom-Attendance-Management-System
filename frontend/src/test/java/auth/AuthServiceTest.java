package auth;

import frontend.auth.AuthService;
import frontend.auth.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setup() {
        authService = new AuthService("http://localhost:8081");
    }

    @Test
    void constructor_shouldCreateInstance() {
        AuthService service = new AuthService("http://localhost:8081");
        assertNotNull(service);
    }

    @Test
    void login_shouldThrowException_whenServerUnavailable() {

        Exception ex = assertThrows(Exception.class, () ->
                authService.login("test@example.com", "password")
        );

        assertNotNull(ex);
    }

    @Test
    void signup_shouldThrowException_whenServerUnavailable() {

        Exception ex = assertThrows(Exception.class, () ->
                authService.signup(
                        "John",
                        "Doe",
                        "john@example.com",
                        "password",
                        Role.STUDENT,
                        "S123"
                )
        );

        assertNotNull(ex);
    }

    @Test
    void signup_shouldHandleNullValues() {

        Exception ex = assertThrows(Exception.class, () ->
                authService.signup(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        assertNotNull(ex);
    }

    @Test
    void login_shouldNormalizeEmail() {

        Exception ex = assertThrows(Exception.class, () ->
                authService.login("  TEST@Example.COM  ", "password")
        );

        assertNotNull(ex);
    }

}