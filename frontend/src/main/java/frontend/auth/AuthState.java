package frontend.auth;

import java.util.Objects;

/**
 * Represents the authenticated user state.
 */
public class AuthState {

    private final String token;
    private final Role role;
    private final String name;

    public AuthState(String token, Role role, String name) {
        this.token = validateToken(token);
        this.role = Objects.requireNonNull(role, "Role must not be null");
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public Role getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    private String validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be null or blank");
        }
        return token;
    }
}