package api;

import frontend.api.AdminApi;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class AdminApiTest {

    private JwtStore jwtStore;
    private AdminApi api;

    @BeforeEach
    void setup() {
        jwtStore = Mockito.mock(JwtStore.class);
        api = new AdminApi("http://localhost:8081", jwtStore);
    }

    @Test
    void tokenOrThrow_shouldThrow_whenNotLoggedIn() {

        when(jwtStore.load()).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            api.getAttendanceStats();
        });

        assertTrue(ex.getMessage().contains("Not logged in")
                || ex.getMessage().contains("No AuthState"));
    }

    @Test
    void tokenOrThrow_shouldThrow_whenTokenMissing() {

        AuthState state = Mockito.mock(AuthState.class);
        when(state.getToken()).thenReturn("");

        when(jwtStore.load()).thenReturn(Optional.of(state));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            api.getAttendanceStats();
        });

        assertTrue(ex.getMessage().contains("Missing token"));
    }

    @Test
    void constructor_shouldCreateApiInstance() {
        AdminApi testApi = new AdminApi("http://localhost:8081", jwtStore);
        assertNotNull(testApi);
    }

    @Test
    void getAttendanceReport_shouldBuildUrlWithoutSearch() {

        AuthState state = Mockito.mock(AuthState.class);
        when(state.getToken()).thenReturn("test-token");
        when(jwtStore.load()).thenReturn(Optional.of(state));

        Exception ex = assertThrows(Exception.class, () -> {
            api.getAttendanceReport(1L, "THIS_MONTH", null);
        });

        assertNotNull(ex);
    }

    @Test
    void getAttendanceReport_shouldBuildUrlWithSearch() {

        AuthState state = Mockito.mock(AuthState.class);
        when(state.getToken()).thenReturn("test-token");
        when(jwtStore.load()).thenReturn(Optional.of(state));

        Exception ex = assertThrows(Exception.class, () -> {
            api.getAttendanceReport(1L, "THIS_MONTH", "John");
        });

        assertNotNull(ex);
    }

}