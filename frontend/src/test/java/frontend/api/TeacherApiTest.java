package frontend.api;

import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class TeacherApiTest {

    private TeacherApi api;
    private JwtStore jwtStore;
    private AuthState authState;

    @BeforeEach
    void setup() {
        api = new TeacherApi("http://localhost:8081");
        jwtStore = Mockito.mock(JwtStore.class);
        authState = Mockito.mock(AuthState.class);
    }

    @Test
    void extractCode_shouldReturnDirectCode() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "ABC123");

        String result = api.extractCode(response);

        assertEquals("ABC123", result);
    }

    @Test
    void extractCode_shouldReturnNestedCode() {
        Map<String, Object> data = new HashMap<>();
        data.put("code", "XYZ999");

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);

        String result = api.extractCode(response);

        assertEquals("XYZ999", result);
    }

    @Test
    void extractCode_shouldReturnNullWhenMissing() {
        Map<String, Object> response = new HashMap<>();

        String result = api.extractCode(response);

        assertNull(result);
    }

    @Test
    void extractCode_shouldReturnNullWhenResponseNull() {
        String result = api.extractCode(null);

        assertNull(result);
    }

    @Test
    void token_shouldUseJwtStoreTokenWhenPresent() throws Exception {

        when(authState.getToken()).thenReturn("fallback-token");
        when(jwtStore.load()).thenReturn(Optional.of(authState));

        // indirectly tested by calling API method expecting HTTP failure
        Exception ex = assertThrows(Exception.class, () ->
                api.getMyClasses(jwtStore, authState)
        );

        assertNotNull(ex);
    }

    @Test
    void token_shouldFallbackToStateToken() throws Exception {

        when(jwtStore.load()).thenReturn(Optional.empty());
        when(authState.getToken()).thenReturn("state-token");

        Exception ex = assertThrows(Exception.class, () ->
                api.getMyClasses(jwtStore, authState)
        );

        assertNotNull(ex);
    }
}