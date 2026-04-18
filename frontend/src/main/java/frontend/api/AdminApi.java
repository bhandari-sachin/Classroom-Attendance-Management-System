package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.dto.AdminClassDto;
import frontend.dto.AdminStudentDto;
import frontend.dto.AdminUsersResponseDto;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class AdminApi {

    private final String baseUrl;
    private final JwtStore store;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    public AdminApi(String baseUrl, JwtStore store) {
        this.baseUrl = baseUrl;
        this.store = store;
    }

    private String tokenOrThrow() {
        AuthState s = store.load().orElseThrow(() -> new RuntimeException("Not logged in"));
        if (s.getToken() == null || s.getToken().isBlank())
            throw new RuntimeException("Missing token");
        return s.getToken();
    }

    public String getAttendanceStatsJson() throws Exception {
        AuthState s = store.load()
                .orElseThrow(() -> new RuntimeException("No AuthState in JwtStore. Please login first."));

        String token = s.getToken();
        if (token == null || token.isBlank()) {
            throw new RuntimeException("JWT token is missing or empty. Please login again.");
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/attendance/stats"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }

        return res.body();
    }

    public Map<String, Object> getAttendanceStats() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/attendance/stats"))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }

        return om.readValue(res.body(), new TypeReference<>() {});
    }

    public List<AdminClassDto> getAdminClasses() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/classes"))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }

        return om.readValue(res.body(), new TypeReference<>() {});
    }

    public List<Map<String, Object>> getAdminClassesRaw() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/classes"))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }

        return om.readValue(res.body(), new TypeReference<>() {});
    }

    public AdminUsersResponseDto getAdminUsers() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/users"))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }

        return om.readValue(res.body(), AdminUsersResponseDto.class);
    }

    public Map<String, Object> getAdminUsersRaw() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/users"))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }

        return om.readValue(res.body(), new TypeReference<>() {});
    }

    public void createClass(String classCode,
                            String name,
                            String teacherEmail,
                            String semester,
                            String academicYear,
                            Integer maxCapacity) throws Exception {

        Map<String, Object> body = Map.of(
                "classCode", classCode,
                "name", name,
                "teacherEmail", teacherEmail,
                "semester", semester == null ? "" : semester,
                "academicYear", academicYear == null ? "" : academicYear,
                "maxCapacity", maxCapacity == null ? 0 : maxCapacity
        );

        String json = om.writeValueAsString(body);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/classes"))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }
    }

    public List<Map<String, Object>> getAttendanceReport(Long classId, String period, String search) throws Exception {

        StringBuilder url = new StringBuilder(baseUrl + "/api/admin/attendance/report?classId=" + classId);

        if (period != null && !period.isBlank()) {
            url.append("&period=").append(URLEncoder.encode(period, StandardCharsets.UTF_8));
        }

        if (search != null && !search.isBlank()) {
            url.append("&search=").append(URLEncoder.encode(search, StandardCharsets.UTF_8));
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }

        return om.readValue(res.body(), new TypeReference<>() {});
    }

    public List<AdminStudentDto> getAllStudentsNotInClass(String classCode) throws Exception {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/classes/" + classCode + "/available-students"))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }

        return om.readValue(
                res.body(),
                om.getTypeFactory().constructCollectionType(List.class, AdminStudentDto.class)
        );
    }

    public void enrollStudentsToClass(String classCode, List<String> studentEmails) throws Exception {

        String json = om.writeValueAsString(studentEmails);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/classes/" + classCode + "/enroll"))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }
    }
}