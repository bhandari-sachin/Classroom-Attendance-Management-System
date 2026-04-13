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
 * API client for teacher-related operations.
 *
 * <p>This class handles teacher endpoints such as:
 * classes,
 * students,
 * sessions,
 * reports,
 * dashboard statistics,
 * and attendance marking.</p>
 */
public class TeacherApi {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public TeacherApi(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient(), new ObjectMapper());
    }

    public TeacherApi(String baseUrl, HttpClient client, ObjectMapper objectMapper) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.client = client;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns the teacher's classes.
     */
    public List<Map<String, Object>> getMyClasses(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return readWrappedList("/api/teacher/classes", jwtStore, state);
    }

    /**
     * Returns the students for the given class.
     */
    public List<Map<String, Object>> getStudentsForClass(JwtStore jwtStore, AuthState state, long classId)
            throws IOException, InterruptedException {
        return readWrappedList("/api/teacher/students?classId=" + classId, jwtStore, state);
    }

    /**
     * Returns the sessions for the given class.
     */
    public List<Map<String, Object>> getSessionsForClass(JwtStore jwtStore, AuthState state, long classId)
            throws IOException, InterruptedException {
        return readWrappedList("/api/teacher/sessions?classId=" + classId, jwtStore, state);
    }

    /**
     * Creates a new session for the given class.
     */
    public Map<String, Object> createSession(JwtStore jwtStore, AuthState state, long classId)
            throws IOException, InterruptedException {

        Map<String, Object> requestBody = Map.of("classId", classId);

        return readPost(
                requestBody,
                jwtStore,
                state
        );
    }

    /**
     * Returns the report for the given session.
     */
    public Map<String, Object> getSessionReport(JwtStore jwtStore, AuthState state, long sessionId)
            throws IOException, InterruptedException {
        return readGet("/api/teacher/reports/session?sessionId=" + sessionId, jwtStore, state);
    }

    /**
     * Returns teacher dashboard statistics.
     */
    public Map<String, Object> getDashboardStats(JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return readGet("/api/teacher/dashboard/stats", jwtStore, state);
    }

    /**
     * Marks attendance for a student in a session.
     */
    public void markAttendance(JwtStore jwtStore, AuthState state, long studentId, long sessionId, String status)
            throws IOException, InterruptedException {

        Map<String, Object> requestBody = Map.of(
                "studentId", studentId,
                "sessionId", sessionId,
                "status", status
        );

        postJson("/api/teacher/attendance/mark", requestBody, jwtStore, state);
    }

    /**
     * Extracts an attendance/session code from a response.
     *
     * <p>Supports both:
     * { "code": "ABC123" }
     * and
     * { "data": { "code": "ABC123" } }</p>
     */
    public String extractCode(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        Object directCode = response.get("code");
        if (directCode != null) {
            return String.valueOf(directCode);
        }

        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object nestedCode = dataMap.get("code");
            if (nestedCode != null) {
                return String.valueOf(nestedCode);
            }
        }

        return null;
    }

    /**
     * Reads a GET endpoint and parses the response as a map.
     */
    private Map<String, Object> readGet(String path, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return objectMapper.readValue(
                get(path, jwtStore, state),
                new TypeReference<>() {}
        );
    }

    /**
     * Reads a POST endpoint and parses the response as a map.
     */
    private Map<String, Object> readPost(Object body, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return objectMapper.readValue(
                postJson("/api/teacher/sessions", body, jwtStore, state),
                new TypeReference<>() {}
        );
    }

    /**
     * Reads a response that may be either:
     * 1) a raw list
     * 2) an object containing a "data" list
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readWrappedList(String path, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        String json = get(path, jwtStore, state);

        if (json != null && json.trim().startsWith("[")) {
            return objectMapper.readValue(json, new TypeReference<>() {});
        }

        Map<String, Object> wrapper = objectMapper.readValue(json, new TypeReference<>() {});
        Object data = wrapper.get("data");

        if (data == null) {
            return List.of();
        }

        return (List<Map<String, Object>>) data;
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
     * Sends an authenticated POST request with JSON body and returns the response body.
     */
    private String postJson(String path, Object body, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = authorizedRequest(path, jwtStore, state)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
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