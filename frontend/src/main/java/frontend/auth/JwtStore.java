package frontend.auth;

import java.util.Optional;
import java.util.prefs.Preferences;

public class JwtStore {

    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_ROLE  = "jwt_role";
    private static final String KEY_NAME  = "jwt_name";

    private final Preferences prefs = Preferences.userNodeForPackage(JwtStore.class);

    public void save(AuthState state) {
        prefs.put(KEY_TOKEN, state.getToken());
        prefs.put(KEY_ROLE, state.getRole().name());
        prefs.put(KEY_NAME, state.getName() == null ? "" : state.getName());
    }

    public Optional<AuthState> load() {
        String token = prefs.get(KEY_TOKEN, "");
        if (token == null || token.isBlank()) return Optional.empty();

        String role = prefs.get(KEY_ROLE, "STUDENT");
        String name = prefs.get(KEY_NAME, "");
        return Optional.of(new AuthState(token, Role.fromString(role), name));
    }

    public void clear() {
        prefs.remove(KEY_TOKEN);
        prefs.remove(KEY_ROLE);
        prefs.remove(KEY_NAME);
    }
}