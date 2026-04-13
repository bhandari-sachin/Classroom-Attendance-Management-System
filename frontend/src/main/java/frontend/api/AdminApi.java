package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.dto.AdminClassDto;
import frontend.dto.AdminStudentDto;
import frontend.dto.AdminUsersResponseDto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * API client for admin-related frontend requests.
 *
 * <p>This class handles authenticated HTTP calls to admin endpoints such as:
 * attendance statistics, class management, user management, reports,
 * and student enrollment.</p>
 */
public class AdminApi {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final String baseUrl;
    private final JwtStore store;
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public AdminApi(String baseUrl, JwtStore store) {
        this(baseUrl, store, HttpClient.newHttpClient(), new ObjectMapper());
    }

    public AdminApi(String baseUrl, JwtStore store, HttpClient client, ObjectMapper objectMapper) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.store = store;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns the current JWT token or throws an exception if the user is not authenticated.
     */
    private String tokenOrThrow() {
        AuthState authState = store.load()
                .orElseThrow(() -> new IllegalStateException("No authentication state found. Please log in first."));

        String token = authState.getToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("JWT token is missing or empty. Please log in again.");
        }

        return token;
    }

    /**
     * Sends an authenticated GET request and returns the response body as plain text.
     */
    private String get(String path) throws IOException, InterruptedException {
        HttpRequest request = authorizedRequest(path)
                .GET()
                .build();

        return send(request);
    }

    /**
     * Sends an authenticated POST request with JSON body and returns the response body.
     */
    private void postJson(String path, Object body) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(body);

        HttpRequest request = authorizedRequest(path)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        send(request);
    }

    /**
     * Sends the request and returns the body.
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
     * Creates a request builder with the Authorization header already set.
     */
    private HttpRequest.Builder authorizedRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header(AUTHORIZATION, BEARER_PREFIX + tokenOrThrow());
    }

    /**
     * Encodes a URL path segment safely.
     */
    private String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Removes a trailing slash from the base URL to avoid double slashes in paths.
     */
    private String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be null or blank.");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Returns attendance statistics as raw JSON.
     */
    public String getAttendanceStatsJson() throws IOException, InterruptedException {
        return get("/api/admin/attendance/stats");
    }

    /**
     * Returns attendance statistics as a generic map.
     */
    public Map<String, Object> getAttendanceStats() throws IOException, InterruptedException {
        return objectMapper.readValue(
                get("/api/admin/attendance/stats"),
                new TypeReference<>() {}
        );
    }

    /**
     * Returns the list of admin classes as typed DTOs.
     */
    public List<AdminClassDto> getAdminClasses() throws IOException, InterruptedException {
        return objectMapper.readValue(
                get("/api/admin/classes"),
                new TypeReference<>() {}
        );
    }

    /**
     * Returns the list of admin classes as raw maps.
     */
    public List<Map<String, Object>> getAdminClassesRaw() throws IOException, InterruptedException {
        return objectMapper.readValue(
                get("/api/admin/classes"),
                new TypeReference<>() {}
        );
    }

    /**
     * Returns admin users in a typed response DTO.
     */
    public AdminUsersResponseDto getAdminUsers() throws IOException, InterruptedException {
        return objectMapper.readValue(
                get("/api/admin/users"),
                AdminUsersResponseDto.class
        );
    }

    /**
     * Returns admin users as a raw map.
     */
    public Map<String, Object> getAdminUsersRaw() throws IOException, InterruptedException {
        return objectMapper.readValue(
                get("/api/admin/users"),
                new TypeReference<>() {}
        );
    }

    /**
     * Creates a new class.
     */
    public void createClass(String classCode,
                            String name,
                            String teacherEmail,
                            String semester,
                            String academicYear,
                            Integer maxCapacity) throws IOException, InterruptedException {

        Map<String, Object> requestBody = Map.of(
                "classCode", classCode,
                "name", name,
                "teacherEmail", teacherEmail,
                "semester", semester == null ? "" : semester,
                "academicYear", academicYear == null ? "" : academicYear,
                "maxCapacity", maxCapacity == null ? 0 : maxCapacity
        );

        postJson("/api/admin/classes", requestBody);
    }

    /**
     * Returns the attendance report for a class with optional period and search filters.
     */
    public List<Map<String, Object>> getAttendanceReport(Long classId, String period, String search)
            throws IOException, InterruptedException {

        StringBuilder path = new StringBuilder("/api/admin/attendance/report?classId=" + classId);

        if (period != null && !period.isBlank()) {
            path.append("&period=").append(URLEncoder.encode(period, StandardCharsets.UTF_8));
        }

        if (search != null && !search.isBlank()) {
            path.append("&search=").append(URLEncoder.encode(search, StandardCharsets.UTF_8));
        }

        return objectMapper.readValue(
                get(path.toString()),
                new TypeReference<>() {}
        );
    }

    /**
     * Returns all students who are not yet enrolled in the given class.
     */
    public List<AdminStudentDto> getAllStudentsNotInClass(String classCode)
            throws IOException, InterruptedException {

        String path = "/api/admin/classes/" + encodePathSegment(classCode) + "/available-students";

        return objectMapper.readValue(
                get(path),
                objectMapper.getTypeFactory().constructCollectionType(List.class, AdminStudentDto.class)
        );
    }

    /**
     * Enrolls the given students into the specified class.
     */
    public void enrollStudentsToClass(String classCode, List<String> studentEmails)
            throws IOException, InterruptedException {

        String path = "/api/admin/classes/" + encodePathSegment(classCode) + "/enroll";
        postJson(path, studentEmails);
    }
}