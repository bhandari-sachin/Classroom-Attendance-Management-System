package frontend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BaseApiClientTest {

    private HttpClient client;
    private ObjectMapper objectMapper;
    private JwtStore jwtStore;
    private AuthState authState;
    private TestBaseApiClient api;

    @BeforeEach
    void setUp() {
        client = Mockito.mock(HttpClient.class);
        objectMapper = new ObjectMapper();
        jwtStore = Mockito.mock(JwtStore.class);
        authState = Mockito.mock(AuthState.class);
        api = new TestBaseApiClient("http://localhost:8081/", client, objectMapper);
    }

    @Test
    void constructor_shouldStripTrailingSlash() {
        assertEquals("http://localhost:8081", api.getBaseUrlValue());
    }

    @Test
    void constructor_shouldThrowWhenBaseUrlNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TestBaseApiClient(null, client, objectMapper)
        );

        assertEquals("Base URL must not be null or blank.", ex.getMessage());
    }

    @Test
    void constructor_shouldThrowWhenBaseUrlBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TestBaseApiClient("   ", client, objectMapper)
        );

        assertEquals("Base URL must not be null or blank.", ex.getMessage());
    }

    @Test
    void constructor_shouldThrowWhenClientNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TestBaseApiClient("http://localhost:8081", null, objectMapper)
        );

        assertEquals("HttpClient must not be null.", ex.getMessage());
    }

    @Test
    void constructor_shouldThrowWhenObjectMapperNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new TestBaseApiClient("http://localhost:8081", client, null)
        );

        assertEquals("ObjectMapper must not be null.", ex.getMessage());
    }

    @Test
    void authorizedRequest_shouldBuildUriAndAuthorizationHeader() {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("stored-token");

        HttpRequest.Builder builder = api.authorizedRequestPublic(jwtStore, authState);
        HttpRequest request = builder.GET().build();

        assertEquals(URI.create("http://localhost:8081/api/test-auth"), request.uri());
        assertEquals("Bearer stored-token", request.headers().firstValue("Authorization").orElse(""));
    }

    @Test
    void resolveToken_shouldUseTokenFromStoreWhenPresent() {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("store-token");

        String token = api.resolveTokenPublic(jwtStore, authState);

        assertEquals("store-token", token);
    }

    @Test
    void resolveToken_shouldFallbackToStateTokenWhenStoreEmpty() {
        when(jwtStore.load()).thenReturn(Optional.empty());
        when(authState.getToken()).thenReturn("state-token");

        String token = api.resolveTokenPublic(jwtStore, authState);

        assertEquals("state-token", token);
    }

    @Test
    void resolveToken_shouldThrowWhenTokenMissing() {
        when(jwtStore.load()).thenReturn(Optional.empty());
        when(authState.getToken()).thenReturn(null);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> api.resolveTokenPublic(jwtStore, authState)
        );

        assertEquals("JWT token is missing. Please log in again.", ex.getMessage());
    }

    @Test
    void resolveToken_shouldThrowWhenTokenBlank() {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("   ");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> api.resolveTokenPublic(jwtStore, authState)
        );

        assertEquals("JWT token is missing. Please log in again.", ex.getMessage());
    }

    @Test
    void send_shouldReturnResponseBodyWhenStatusIsSuccessful() throws IOException, InterruptedException {
        HttpResponse<String> response = mockStringResponse(200, "{\"ok\":true}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/test-success"))
                .GET()
                .build();

        String result = api.sendPublic(request);

        assertEquals("{\"ok\":true}", result);
    }

    @Test
    void send_shouldThrowApiExceptionWhenStatusIsError() throws IOException, InterruptedException {
        HttpResponse<String> response = mockStringResponse(500, "{\"error\":\"boom\"}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/test-error"))
                .GET()
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> api.sendPublic(request));

        assertEquals("HTTP 500: {\"error\":\"boom\"}", ex.getMessage());
    }

    @Test
    void get_shouldSendAuthenticatedGetRequest() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("abc123");

        HttpResponse<String> response = mockStringResponse(200, "{\"status\":\"ok\"}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        String result = api.getPublic(jwtStore, authState);

        assertEquals("{\"status\":\"ok\"}", result);
    }

    @Test
    void postJson_shouldSendJsonRequest() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("abc123");

        HttpResponse<String> response = mockStringResponse(200, "{\"saved\":true}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        String result = api.postJsonPublic(Map.of("name", "Farah"), jwtStore, authState);

        assertEquals("{\"saved\":true}", result);
    }

    @Test
    void readGet_shouldParseMapResponse() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("abc123");

        HttpResponse<String> response = mockStringResponse(200, "{\"name\":\"Math\",\"students\":12}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        Map<String, Object> result = api.readGetPublic(jwtStore, authState);

        assertEquals("Math", result.get("name"));
        assertEquals(12, result.get("students"));
    }

    @Test
    void readPost_shouldParseMapResponse() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("abc123");

        HttpResponse<String> response = mockStringResponse(200, "{\"status\":\"created\",\"id\":7}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        Map<String, Object> result = api.readPostPublic(
                Map.of("name", "Physics"),
                jwtStore,
                authState
        );

        assertEquals("created", result.get("status"));
        assertEquals(7, result.get("id"));
    }

    @Test
    void readList_shouldParseListResponse() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("abc123");

        HttpResponse<String> response = mockStringResponse(200, "[{\"id\":1},{\"id\":2}]");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        List<Map<String, Object>> result = api.readListPublic(jwtStore, authState);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).get("id"));
        assertEquals(2, result.get(1).get("id"));
    }

    @Test
    void readWrappedList_shouldParseRawArrayResponse() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("abc123");

        HttpResponse<String> response = mockStringResponse(200, "[{\"code\":\"A1\"},{\"code\":\"B2\"}]");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        List<Map<String, Object>> result = api.readWrappedListPublic("/api/classes-raw", jwtStore, authState);

        assertEquals(2, result.size());
        assertEquals("A1", result.get(0).get("code"));
        assertEquals("B2", result.get(1).get("code"));
    }

    @Test
    void readWrappedList_shouldParseWrappedDataResponse() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("abc123");

        HttpResponse<String> response = mockStringResponse(200, "{\"data\":[{\"code\":\"A1\"},{\"code\":\"B2\"}]}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        List<Map<String, Object>> result = api.readWrappedListPublic("/api/classes-wrapped", jwtStore, authState);

        assertEquals(2, result.size());
        assertEquals("A1", result.get(0).get("code"));
        assertEquals("B2", result.get(1).get("code"));
    }

    @Test
    void readWrappedList_shouldReturnEmptyListWhenDataMissing() throws IOException, InterruptedException {
        when(jwtStore.load()).thenReturn(Optional.of(authState));
        when(authState.getToken()).thenReturn("abc123");

        HttpResponse<String> response = mockStringResponse(200, "{\"status\":\"ok\"}");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        List<Map<String, Object>> result = api.readWrappedListPublic("/api/classes-empty", jwtStore, authState);

        assertTrue(result.isEmpty());
    }

    @Test
    void stripTrailingSlash_shouldRemoveTrailingSlash() {
        String result = TestBaseApiClient.stripTrailingSlashPublic("http://localhost:8081/");

        assertEquals("http://localhost:8081", result);
    }

    @Test
    void stripTrailingSlash_shouldReturnSameWhenNoTrailingSlash() {
        String result = TestBaseApiClient.stripTrailingSlashPublic("http://localhost:8081");

        assertEquals("http://localhost:8081", result);
    }

    @Test
    void stripTrailingSlash_shouldThrowWhenNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TestBaseApiClient.stripTrailingSlashPublic(null)
        );

        assertEquals("Base URL must not be null or blank.", ex.getMessage());
    }

    @Test
    void requirePath_shouldAcceptValidPath() {
        TestBaseApiClient.requirePathPublic("/api/test-path", "testPath");
        assertTrue(true);
    }

    @Test
    void requirePath_shouldThrowWhenNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TestBaseApiClient.requirePathPublic(null, "classesPath")
        );

        assertEquals("classesPath must not be null or blank.", ex.getMessage());
    }

    @Test
    void requirePath_shouldThrowWhenBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TestBaseApiClient.requirePathPublic("   ", "classesPath")
        );

        assertEquals("classesPath must not be null or blank.", ex.getMessage());
    }

    @Test
    void send_shouldUseStringBodyResponse() throws IOException, InterruptedException {
        HttpResponse<String> response = mockStringResponse(200, "plain-text");
        when(client.send(any(HttpRequest.class), anyStringBodyHandler())).thenReturn(response);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/ping"))
                .GET()
                .build();

        String result = api.sendPublic(request);

        assertInstanceOf(String.class, result);
        assertEquals("plain-text", result);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse.BodyHandler<String> anyStringBodyHandler() {
        return (HttpResponse.BodyHandler<String>) any(HttpResponse.BodyHandler.class);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockStringResponse(int statusCode, String body) {
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        when(response.headers()).thenReturn(HttpHeaders.of(Map.of(), (left, right) -> true));
        return response;
    }

    private static final class TestBaseApiClient extends BaseApiClient {

        private TestBaseApiClient(String baseUrl, HttpClient client, ObjectMapper objectMapper) {
            super(baseUrl, client, objectMapper);
        }

        private String getBaseUrlValue() {
            return baseUrl;
        }

        private HttpRequest.Builder authorizedRequestPublic(JwtStore jwtStore, AuthState state) {
            return authorizedRequest("/api/test-auth", jwtStore, state);
        }

        private String getPublic(JwtStore jwtStore, AuthState state)
                throws IOException, InterruptedException {
            return get("/api/sample-get", jwtStore, state);
        }

        private String postJsonPublic(Object body, JwtStore jwtStore, AuthState state)
                throws IOException, InterruptedException {
            return postJson("/api/sample-post", body, jwtStore, state);
        }

        private Map<String, Object> readGetPublic(JwtStore jwtStore, AuthState state)
                throws IOException, InterruptedException {
            return readGet("/api/classes-get", jwtStore, state);
        }

        private Map<String, Object> readPostPublic(Object body, JwtStore jwtStore, AuthState state)
                throws IOException, InterruptedException {
            return readPost("/api/classes-post", body, jwtStore, state);
        }

        private List<Map<String, Object>> readListPublic(JwtStore jwtStore, AuthState state)
                throws IOException, InterruptedException {
            return readList("/api/classes-list", jwtStore, state);
        }

        private List<Map<String, Object>> readWrappedListPublic(String path, JwtStore jwtStore, AuthState state)
                throws IOException, InterruptedException {
            return readWrappedList(path, jwtStore, state);
        }

        private String sendPublic(HttpRequest request) throws IOException, InterruptedException {
            return send(request);
        }

        private String resolveTokenPublic(JwtStore jwtStore, AuthState state) {
            return resolveToken(jwtStore, state);
        }

        private static String stripTrailingSlashPublic(String url) {
            return stripTrailingSlash(url);
        }

        private static void requirePathPublic(String value, String fieldName) {
            requirePath(value, fieldName);
        }
    }
}
