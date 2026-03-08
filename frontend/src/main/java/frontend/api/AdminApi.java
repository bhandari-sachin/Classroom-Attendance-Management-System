package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.dto.AdminClassDto;
import frontend.dto.AdminUsersResponseDto;

import java.net.URI;
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
        if (s.getToken() == null || s.getToken().isBlank()) throw new RuntimeException("Missing token");
        return s.getToken();
    }

    public String getAttendanceStatsJson() throws Exception {
        AuthState s = store.load().orElseThrow(() -> new RuntimeException("No AuthState in JwtStore. Please login first."));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/attendance/stats"))
                .header("Authorization", "Bearer " + s.getToken())
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }
        return res.body();
    }

    public List<AdminClassDto> getAdminClasses() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/classes"))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());

        return om.readValue(res.body(), new TypeReference<>() {});
    }

    public AdminUsersResponseDto getAdminUsers() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/admin/users"))
                .header("Authorization", "Bearer " + tokenOrThrow())
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());

        return om.readValue(res.body(), AdminUsersResponseDto.class);
    }

    public void createClass(String classCode, String name, String teacherEmail, String semester, String academicYear, Integer maxCapacity) throws Exception {
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
        if (res.statusCode() >= 400) throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
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
    public List<Map<String, Object>> getAttendanceReport(Long classId, String period, String search) throws Exception {
        StringBuilder url = new StringBuilder(baseUrl + "/api/admin/attendance/report?classId=" + classId);

        if (period != null && !period.isBlank()) {
            url.append("&period=").append(java.net.URLEncoder.encode(period, StandardCharsets.UTF_8));
        }

        if (search != null && !search.isBlank()) {
            url.append("&search=").append(java.net.URLEncoder.encode(search, StandardCharsets.UTF_8));
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
}