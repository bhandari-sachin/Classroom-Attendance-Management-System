package frontend.auth;

public class AuthState {
    private final String token;
    private final Role role;
    private final String name;
    private String language;

    public AuthState(String token, Role role, String name, String language) {
        this.token = token;
        this.role = role;
        this.name = name;
        this.language = language;
    }

    public String getToken() { return token; }
    public Role getRole() { return role; }
    public String getName() { return name; }
    public void setLanguage(String language) {
        this.language = language;
    }
    public String getLanguage() {
        return language != null ? language : "en";
    }
}
