package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherApi {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();
    private final String baseUrl;

    public TeacherApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String token(JwtStore jwtStore, AuthState state) {
        return jwtStore.load().map(AuthState::getToken).orElse(state.getToken());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> unwrapList(String json) throws Exception {
        // supports either:
        // 1) { data: [ ... ] }
        // 2) [ ... ]
        if (json != null && json.trim().startsWith("[")) {
            return om.readValue(json, new TypeReference<>() {});
        }

        Map<String, Object> map = om.readValue(json, new TypeReference<>() {});
        Object data = map.get("data");
        if (data == null) return List.of();
        return (List<Map<String, Object>>) data;
    }

    public String extractCode(Map<String, Object> res) {
        // supports either:
        // 1) { code: "ABC" }
        // 2) { data: { code: "ABC" } }
        if (res == null) return null;

        Object direct = res.get("code");
        if (direct != null) return String.valueOf(direct);

        Object data = res.get("data");
        if (data instanceof Map<?, ?> m) {
            Object c = m.get("code");
            if (c != null) return String.valueOf(c);
        }
        return null;
    }

    public List<Map<String, Object>> getMyClasses(JwtStore jwtStore, AuthState state) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/teacher/classes"))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.statusCode() + " " + res.body());
        return unwrapList(res.body());
    }

    public List<Map<String, Object>> getStudentsForClass(JwtStore jwtStore, AuthState state, long classId) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/teacher/students?classId=" + classId))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.statusCode() + " " + res.body());
        return unwrapList(res.body());
    }

    public List<Map<String, Object>> getSessionsForClass(JwtStore jwtStore, AuthState state, long classId) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/teacher/sessions?classId=" + classId))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.statusCode() + " " + res.body());
        return unwrapList(res.body());
    }

    public Map<String, Object> createSession(JwtStore jwtStore, AuthState state, long classId) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("classId", classId);

        String jsonBody = om.writeValueAsString(payload);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/teacher/sessions"))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.statusCode() + " " + res.body());

        return om.readValue(res.body(), new TypeReference<>() {});
    }

    public Map<String, Object> getSessionReport(JwtStore jwtStore, AuthState state, long sessionId) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/teacher/reports/session?sessionId=" + sessionId))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.statusCode() + " " + res.body());

        return om.readValue(res.body(), new TypeReference<>() {});
    }
    public Map<String, Object> getDashboardStats(JwtStore jwtStore, AuthState state) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/teacher/dashboard/stats"))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.body());

        return om.readValue(res.body(), new TypeReference<>() {});
    }
    public void markAttendance(JwtStore jwtStore, AuthState state, long studentId, long sessionId, String status) throws Exception {
        Map<String, Object> body = Map.of(
                "studentId", studentId,
                "sessionId", sessionId,
                "status", status
        );

        String json = om.writeValueAsString(body);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/teacher/attendance/mark"))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException(res.statusCode() + " " + res.body());
        }
    }

}