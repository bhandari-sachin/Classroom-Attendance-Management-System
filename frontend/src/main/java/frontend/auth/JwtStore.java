package frontend.auth;

import java.util.Optional;
import java.util.prefs.Preferences;

public class JwtStore {

    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_ROLE  = "jwt_role";
    private static final String KEY_NAME  = "jwt_name";
    private static final String KEY_USER_ID = "jwt_user_id";

    private final Preferences prefs = Preferences.userNodeForPackage(JwtStore.class);

    public void save(AuthState state) {
        prefs.put(KEY_TOKEN, state.getToken());
        prefs.put(KEY_ROLE, state.getRole().name());
        prefs.put(KEY_NAME, state.getName() == null ? "" : state.getName());
        if (state.getUserId() != null) {
            prefs.putLong(KEY_USER_ID, state.getUserId());
        } else {
            prefs.remove(KEY_USER_ID);
        }
    }

    public Optional<AuthState> load() {
        String token = prefs.get(KEY_TOKEN, "");
        if (token == null || token.isBlank()) return Optional.empty();

        String role = prefs.get(KEY_ROLE, "STUDENT");
        String name = prefs.get(KEY_NAME, "");
        Long userId = prefs.getLong(KEY_USER_ID, -1L);
        if (userId == -1L) {
            userId = null;
        }
        return Optional.of(new AuthState(token, Role.fromString(role), name, userId));
    }

    public void clear() {
        prefs.remove(KEY_TOKEN);
        prefs.remove(KEY_ROLE);
        prefs.remove(KEY_NAME);
        prefs.remove(KEY_USER_ID);
    }
}