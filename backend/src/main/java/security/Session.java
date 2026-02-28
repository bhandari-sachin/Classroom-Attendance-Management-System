package security;

import model.UserRole;

public class Session {

    private final Long userId;
    private final String username;
    private final UserRole role;

    public Session(Long userId, String username, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
}