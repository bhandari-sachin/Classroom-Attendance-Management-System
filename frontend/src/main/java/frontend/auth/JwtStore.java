package frontend.auth;

import java.util.Optional;
import java.util.prefs.Preferences;

public class JwtStore {

    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_ROLE  = "jwt_role";
    private static final String KEY_NAME  = "jwt_name";
    private static final String KEY_LANGUAGE = "jwt_language";

    private final Preferences prefs = Preferences.userNodeForPackage(JwtStore.class);

    private static JwtStore instance;
    public static JwtStore get() {
        if (instance == null) instance = new JwtStore();
        return instance;
    }

    public void save(AuthState state) {
        prefs.put(KEY_TOKEN, state.getToken());
        prefs.put(KEY_ROLE, state.getRole().name());
        prefs.put(KEY_NAME, state.getName() == null ? "" : state.getName());
        prefs.put(KEY_LANGUAGE, state.getLanguage() == null ? "en" : state.getLanguage());
    }

    public Optional<AuthState> load() {
        String token = prefs.get(KEY_TOKEN, "");
        if (token == null || token.isBlank()) return Optional.empty();

        String role = prefs.get(KEY_ROLE, "STUDENT");
        String name = prefs.get(KEY_NAME, "");
        String language = prefs.get(KEY_LANGUAGE, "en");
        return Optional.of(new AuthState(token, Role.fromString(role), name, language));
    }

    public void clear() {
        prefs.remove(KEY_TOKEN);
        prefs.remove(KEY_ROLE);
        prefs.remove(KEY_NAME);
        prefs.remove(KEY_LANGUAGE);
    }
}