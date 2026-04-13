package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * API client for student-teacher related requests.
 */
public record StudentTeacherApi(
        String baseUrl,
        HttpClient client,
        ObjectMapper objectMapper,
        Paths paths
) {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public StudentTeacherApi(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient(), new ObjectMapper(), Paths.defaults());
    }

    public StudentTeacherApi {
        baseUrl = stripTrailingSlash(baseUrl);

        if (client == null) {
            throw new IllegalArgumentException("HttpClient must not be null.");
        }
        if (objectMapper == null) {
            throw new IllegalArgumentException("ObjectMapper must not be null.");
        }
        if (paths == null) {
            throw new IllegalArgumentException("Paths must not be null.");
        }
    }

    /**
     * Configurable API paths.
     */
    public record Paths(String teachersPath) {

        public Paths {
            requirePath(teachersPath);
        }

        public static Paths defaults() {
            return new Paths("/api/student/teachers");
        }

        private static void requirePath(String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("teachersPath" + " must not be null or blank.");
            }
        }
    }

    /**
     * Returns the list of teachers visible to the current student.
     */
    public List<Map<String, Object>> getTeachers(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        String responseBody = get(jwtStore, state);

        return objectMapper.readValue(
                responseBody,
                new TypeReference<>() {}
        );
    }

    private String get(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        HttpRequest request = authorizedRequest(jwtStore, state)
                .GET()
                .build();

        return send(request);
    }

    private HttpRequest.Builder authorizedRequest(JwtStore jwtStore, AuthState state) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + paths.teachersPath()))
                .header(AUTHORIZATION, BEARER_PREFIX + resolveToken(jwtStore, state));
    }

    private String send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new ApiException("HTTP " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    private String resolveToken(JwtStore jwtStore, AuthState state) {
        String token = jwtStore.load()
                .map(AuthState::getToken)
                .orElse(state != null ? state.getToken() : null);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("JWT token is missing. Please log in again.");
        }

        return token;
    }

    private static String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be null or blank.");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}