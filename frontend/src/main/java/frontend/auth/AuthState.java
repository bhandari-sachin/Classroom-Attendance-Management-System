package frontend.auth;

public class AuthState {
    private final String token;
    private final Role role;
    private final String name;
    private final Long userId;

    public AuthState(String token, Role role, String name, Long userId) {
        this.token = token;
        this.role = role;
        this.name = name;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public Role getRole() { return role; }
    public String getName() { return name; }
    public Long getUserId() { return userId; }
}
