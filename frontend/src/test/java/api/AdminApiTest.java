package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.api.AdminApi;
import frontend.api.ApiException;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminApiTest {

    private JwtStore jwtStore;
    private HttpClient httpClient;
    private HttpResponse<String> httpResponse;
    private ObjectMapper objectMapper;
    private AdminApi api;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        jwtStore = mock(JwtStore.class);
        httpClient = mock(HttpClient.class);
        httpResponse = (HttpResponse<String>) mock(HttpResponse.class);
        objectMapper = new ObjectMapper();

        api = new AdminApi(
                "http://localhost:8081",
                jwtStore,
                httpClient,
                objectMapper,
                AdminApi.Paths.defaults()
        );
    }

    @Test
    void tokenOrThrow_shouldThrow_whenNotLoggedIn() {
        when(jwtStore.load()).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> api.getAttendanceStats()
        );

        assertEquals("No authentication state found. Please log in first.", ex.getMessage());
    }

    @Test
    void tokenOrThrow_shouldThrow_whenTokenMissing() {
        AuthState state = mock(AuthState.class);
        when(state.getToken()).thenReturn("");
        when(jwtStore.load()).thenReturn(Optional.of(state));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> api.getAttendanceStats()
        );

        assertEquals("JWT token is missing or empty. Please log in again.", ex.getMessage());
    }

    @Test
    void constructor_shouldCreateApiInstance() {
        AdminApi testApi = new AdminApi(
                "http://localhost:8081",
                jwtStore,
                httpClient,
                objectMapper,
                AdminApi.Paths.defaults()
        );

        assertNotNull(testApi);
    }

    @Test
    void getAttendanceReport_shouldBuildUrlWithoutSearch() throws Exception {
        AuthState state = mock(AuthState.class);
        when(state.getToken()).thenReturn("test-token");
        when(jwtStore.load()).thenReturn(Optional.of(state));

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("[]");
        when(httpClient.send(
                any(HttpRequest.class),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        )).thenReturn(httpResponse);

        api.getAttendanceReport(1L, "THIS_MONTH", null);

        verify(httpClient).send(
                argThat(request ->
                        request.uri().toString().equals(
                                "http://localhost:8081/api/admin/attendance/report?classId=1&period=THIS_MONTH"
                        ) &&
                                "Bearer test-token".equals(
                                        request.headers().firstValue("Authorization").orElse(null)
                                )
                ),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        );
    }

    @Test
    void getAttendanceReport_shouldBuildUrlWithSearch() throws Exception {
        AuthState state = mock(AuthState.class);
        when(state.getToken()).thenReturn("test-token");
        when(jwtStore.load()).thenReturn(Optional.of(state));

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("[]");
        when(httpClient.send(
                any(HttpRequest.class),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        )).thenReturn(httpResponse);

        api.getAttendanceReport(1L, "THIS_MONTH", "John Doe");

        verify(httpClient).send(
                argThat(request ->
                        request.uri().toString().equals(
                                "http://localhost:8081/api/admin/attendance/report?classId=1&period=THIS_MONTH&search=John+Doe"
                        ) &&
                                "Bearer test-token".equals(
                                        request.headers().firstValue("Authorization").orElse(null)
                                )
                ),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        );
    }

    @Test
    void send_shouldThrowApiException_whenHttpStatusIsError() throws Exception {
        AuthState state = mock(AuthState.class);
        when(state.getToken()).thenReturn("test-token");
        when(jwtStore.load()).thenReturn(Optional.of(state));

        when(httpResponse.statusCode()).thenReturn(500);
        when(httpResponse.body()).thenReturn("{\"error\":\"Server error\"}");
        when(httpClient.send(
                any(HttpRequest.class),
                Mockito.<HttpResponse.BodyHandler<String>>any()
        )).thenReturn(httpResponse);

        ApiException ex = assertThrows(ApiException.class, () -> api.getAttendanceStats());

        assertEquals("HTTP 500: {\"error\":\"Server error\"}", ex.getMessage());
    }

    @Test
    void constructor_shouldThrow_whenBaseUrlIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                this::createAdminApiWithBlankUrl
        );

        assertEquals("Base URL must not be null or blank.", ex.getMessage());
    }

    @Test
    void constructor_shouldThrow_whenJwtStoreIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                this::createAdminApiWithNullJwtStore
        );

        assertEquals("JwtStore must not be null.", ex.getMessage());
    }

    @Test
    void paths_shouldThrow_whenAnyPathIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                this::createPathsWithBlankClassesPath
        );

        assertEquals("classesPath must not be null or blank.", ex.getMessage());
    }

    private void createAdminApiWithBlankUrl() {
        new AdminApi(
                "   ",
                jwtStore,
                httpClient,
                objectMapper,
                AdminApi.Paths.defaults()
        );
    }

    private void createAdminApiWithNullJwtStore() {
        new AdminApi(
                "http://localhost:8081",
                null,
                httpClient,
                objectMapper,
                AdminApi.Paths.defaults()
        );
    }

    private void createPathsWithBlankClassesPath() {
        new AdminApi.Paths(
                "",
                "/api/admin/users",
                "/api/admin/attendance/stats",
                "/api/admin/attendance/report"
        );
    }
}