package frontend.auth;

import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JwtStoreTest {

    private JwtStore store;

    @BeforeEach
    void setup() {
        store = new JwtStore();
        store.clear(); // ensure clean state before each test
    }

    @Test
    void saveAndLoad_shouldReturnStoredAuthState() {

        AuthState state = new AuthState("test-token", Role.ADMIN, "John Doe");

        store.save(state);

        Optional<AuthState> loaded = store.load();

        assertTrue(loaded.isPresent());
        assertEquals("test-token", loaded.get().getToken());
        assertEquals(Role.ADMIN, loaded.get().getRole());
        assertEquals("John Doe", loaded.get().getName());
    }

    @Test
    void load_shouldReturnEmpty_whenNoTokenSaved() {

        Optional<AuthState> result = store.load();

        assertTrue(result.isEmpty());
    }

    @Test
    void clear_shouldRemoveStoredData() {

        AuthState state = new AuthState("token123", Role.STUDENT, "Alice");

        store.save(state);

        store.clear();

        Optional<AuthState> result = store.load();

        assertTrue(result.isEmpty());
    }

    @Test
    void save_shouldHandleNullName() {

        AuthState state = new AuthState("token456", Role.TEACHER, null);

        store.save(state);

        Optional<AuthState> loaded = store.load();

        assertTrue(loaded.isPresent());
        assertEquals("", loaded.get().getName());
    }
}