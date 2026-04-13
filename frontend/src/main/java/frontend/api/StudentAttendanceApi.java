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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * API client for student attendance operations.
 */
public record StudentAttendanceApi(
        String baseUrl,
        HttpClient client,
        ObjectMapper objectMapper,
        StudentAttendanceApi.Paths paths
) {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    public StudentAttendanceApi(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient(), new ObjectMapper(), Paths.defaults());
    }

    public StudentAttendanceApi {
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
     * Customizable endpoint paths for StudentAttendanceApi.
     */
    public record Paths(
            String summaryPath,
            String recordsPath,
            String markAttendancePath
    ) {
        public Paths {
            requirePath(summaryPath, "summaryPath");
            requirePath(recordsPath, "recordsPath");
            requirePath(markAttendancePath, "markAttendancePath");
        }

        public static Paths defaults() {
            return new Paths(
                    "/api/student/attendance/summary",
                    "/api/student/attendance/records",
                    "/api/attendance/mark"
            );
        }

        private static String requirePath(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(fieldName + " must not be null or blank.");
            }
            return value;
        }
    }

    /**
     * Returns the student's attendance summary.
     */
    public Map<String, Object> getSummary(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        String responseBody = get(paths.summaryPath(), jwtStore, state);

        return objectMapper.readValue(
                responseBody,
                new TypeReference<>() {}
        );
    }

    /**
     * Returns the student's attendance records.
     */
    public List<Map<String, Object>> getRecords(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        String responseBody = get(paths.recordsPath(), jwtStore, state);

        return objectMapper.readValue(
                responseBody,
                new TypeReference<>() {}
        );
    }

    /**
     * Submits an attendance code for the current student.
     */
    public void submitCode(String code, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Attendance code is required.");
        }

        Map<String, String> requestBody = Map.of("code", code.trim());
        postAttendanceCode(requestBody, jwtStore, state);
    }

    /**
     * Sends an authenticated GET request and returns the response body.
     */
    private String get(String path, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        HttpRequest request = authorizedRequest(path, jwtStore, state)
                .GET()
                .build();

        return send(request);
    }

    /**
     * Sends the attendance code as an authenticated POST request.
     */
    private void postAttendanceCode(Object body, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = authorizedRequest(paths.markAttendancePath(), jwtStore, state)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        send(request);
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
     * Throws ApiException for HTTP error responses.
     */
    private String send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new ApiException("HTTP " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    /**
     * Resolves the JWT token.
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
     * Removes trailing slash from base URL.
     */
    private static String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be null or blank.");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}