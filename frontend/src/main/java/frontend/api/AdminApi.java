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
 */
public record AdminApi(
        String baseUrl,
        JwtStore store,
        HttpClient client,
        ObjectMapper objectMapper,
        AdminApi.Paths paths
) {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String PATH_SEPARATOR = "/";
    private static final String QUERY_PREFIX = "?";
    private static final String PARAM_SEPARATOR = "&";
    private static final String CLASS_ID_PARAM = "classId=";
    private static final String PERIOD_PARAM = "period=";
    private static final String SEARCH_PARAM = "search=";
    private static final String AVAILABLE_STUDENTS_SUFFIX = "available-students";
    private static final String ENROLL_SUFFIX = "enroll";

    public AdminApi(String baseUrl, JwtStore store) {
        this(baseUrl, store, HttpClient.newHttpClient(), new ObjectMapper(), Paths.defaults());
    }

    public AdminApi {
        baseUrl = stripTrailingSlash(baseUrl);
        if (store == null) {
            throw new IllegalArgumentException("JwtStore must not be null.");
        }
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
     * Customizable endpoint paths for AdminApi.
     */
    public record Paths(
            String classesPath,
            String usersPath,
            String attendanceStatsPath,
            String attendanceReportPath
    ) {
        public Paths {
            classesPath = requirePath(classesPath, "classesPath");
            usersPath = requirePath(usersPath, "usersPath");
            attendanceStatsPath = requirePath(attendanceStatsPath, "attendanceStatsPath");
            attendanceReportPath = requirePath(attendanceReportPath, "attendanceReportPath");
        }

        public static Paths defaults() {
            return new Paths(
                    "/api/admin/classes",
                    "/api/admin/users",
                    "/api/admin/attendance/stats",
                    "/api/admin/attendance/report"
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
     * Sends an authenticated POST request with JSON body.
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
    private static String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Encodes a query parameter value safely.
     */
    private static String encodeQueryParam(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Removes a trailing slash from the base URL to avoid double slashes in paths.
     */
    private static String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be null or blank.");
        }
        return url.endsWith(PATH_SEPARATOR) ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Returns attendance statistics as a generic map.
     */
    public Map<String, Object> getAttendanceStats() throws IOException, InterruptedException {
        return objectMapper.readValue(
                get(paths.attendanceStatsPath()),
                new TypeReference<>() {}
        );
    }

    /**
     * Returns the list of admin classes as typed DTOs.
     */
    public List<AdminClassDto> getAdminClasses() throws IOException, InterruptedException {
        return objectMapper.readValue(
                get(paths.classesPath()),
                new TypeReference<>() {}
        );
    }

    /**
     * Returns the list of admin classes as raw maps.
     */
    public List<Map<String, Object>> getAdminClassesRaw() throws IOException, InterruptedException {
        return objectMapper.readValue(
                get(paths.classesPath()),
                new TypeReference<>() {}
        );
    }

    /**
     * Returns admin users in a typed response DTO.
     */
    public AdminUsersResponseDto getAdminUsers() throws IOException, InterruptedException {
        return objectMapper.readValue(
                get(paths.usersPath()),
                AdminUsersResponseDto.class
        );
    }

    /**
     * Returns admin users as a raw map.
     */
    public Map<String, Object> getAdminUsersRaw() throws IOException, InterruptedException {
        return objectMapper.readValue(
                get(paths.usersPath()),
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

        postJson(paths.classesPath(), requestBody);
    }

    /**
     * Returns the attendance report for a class with optional period and search filters.
     */
    public List<Map<String, Object>> getAttendanceReport(Long classId, String period, String search)
            throws IOException, InterruptedException {

        StringBuilder path = new StringBuilder(paths.attendanceReportPath())
                .append(QUERY_PREFIX)
                .append(CLASS_ID_PARAM)
                .append(classId);

        if (period != null && !period.isBlank()) {
            path.append(PARAM_SEPARATOR)
                    .append(PERIOD_PARAM)
                    .append(encodeQueryParam(period));
        }

        if (search != null && !search.isBlank()) {
            path.append(PARAM_SEPARATOR)
                    .append(SEARCH_PARAM)
                    .append(encodeQueryParam(search));
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

        String path = paths.classesPath()
                + PATH_SEPARATOR
                + encodePathSegment(classCode)
                + PATH_SEPARATOR
                + AVAILABLE_STUDENTS_SUFFIX;

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

        String path = paths.classesPath()
                + PATH_SEPARATOR
                + encodePathSegment(classCode)
                + PATH_SEPARATOR
                + ENROLL_SUFFIX;

        postJson(path, studentEmails);
    }
}