package frontend.auth;

import java.util.Objects;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Stores and retrieves JWT authentication data using Java Preferences.
 */
public class JwtStore {

    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_ROLE  = "jwt_role";
    private static final String KEY_NAME  = "jwt_name";

    private final Preferences preferences;

    public JwtStore() {
        this.preferences = Preferences.userNodeForPackage(JwtStore.class);
    }

    public void save(AuthState state) {
        Objects.requireNonNull(state, "AuthState must not be null");

        preferences.put(KEY_TOKEN, state.getToken());
        preferences.put(KEY_ROLE, state.getRole().name());
        preferences.put(KEY_NAME, state.getName() == null ? "" : state.getName());
    }

    public Optional<AuthState> load() {
        String token = preferences.get(KEY_TOKEN, null);

        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String roleValue = preferences.get(KEY_ROLE, Role.STUDENT.name());
        String name = preferences.get(KEY_NAME, "");

        Role role = Role.fromString(roleValue);

        return Optional.of(new AuthState(token, role, name));
    }

    public void clear() {
        preferences.remove(KEY_TOKEN);
        preferences.remove(KEY_ROLE);
        preferences.remove(KEY_NAME);
    }
}