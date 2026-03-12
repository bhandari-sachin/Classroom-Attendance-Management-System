package frontend.auth;

public class AuthState {
    private final String token;
    private final Role role;
    private final String name;

    public AuthState(String token, Role role, String name) {
        this.token = token;
        this.role = role;
        this.name = name;
    }

    public String getToken() { return token; }
    public Role getRole() { return role; }
    public String getName() { return name; }
}
