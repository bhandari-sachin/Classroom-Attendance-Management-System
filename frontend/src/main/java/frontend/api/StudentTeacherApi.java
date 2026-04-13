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
 *
 * <p>This class retrieves the list of teachers available to the student.</p>
 */
public class StudentTeacherApi {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public StudentTeacherApi(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient(), new ObjectMapper());
    }

    public StudentTeacherApi(String baseUrl, HttpClient client, ObjectMapper objectMapper) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.client = client;
        this.objectMapper = objectMapper;
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

    /**
     * Sends an authenticated GET request and returns the response body.
     */
    private String get(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        HttpRequest request = authorizedRequest("/api/student/teachers", jwtStore, state)
                .GET()
                .build();

        return send(request);
    }

    /**
     * Builds an authenticated request with the current JWT token.
     */
    private HttpRequest.Builder authorizedRequest(String path, JwtStore jwtStore, AuthState state) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header(AUTHORIZATION, BEARER_PREFIX + resolveToken(jwtStore, state));
    }

    /**
     * Sends the request and returns the response body.
     * Throws an exception for HTTP error responses.
     */
    private String send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    /**
     * Resolves the JWT token from the store first, then falls back to the provided auth state.
     */
    private String resolveToken(JwtStore jwtStore, AuthState state) {
        String token = jwtStore.load()
                .map(AuthState::getToken)
                .orElse(state != null ? state.getToken() : null);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("JWT token is missing. Please log in again.");
        }

        return token;
    }

    /**
     * Removes a trailing slash from the base URL to avoid malformed URLs.
     */
    private String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be null or blank.");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}