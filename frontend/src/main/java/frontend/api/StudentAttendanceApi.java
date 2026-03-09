package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class StudentAttendanceApi {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();
    private final String baseUrl;

    public StudentAttendanceApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String token(JwtStore jwtStore, AuthState state) {
        return jwtStore.load().map(AuthState::getToken).orElse(state.getToken());
    }

    public Map<String, Object> getSummary(JwtStore jwtStore, AuthState state) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/student/attendance/summary"))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            throw new RuntimeException("Summary failed: " + res.statusCode() + " " + res.body());
        }

        return om.readValue(res.body(), new TypeReference<>() {});
    }

    public List<Map<String, Object>> getRecords(JwtStore jwtStore, AuthState state) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/student/attendance/records"))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            throw new RuntimeException("Records failed: " + res.statusCode() + " " + res.body());
        }

        return om.readValue(res.body(), new TypeReference<>() {});
    }
    public void submitCode(String code, JwtStore jwtStore, AuthState state) throws Exception {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Attendance code required");
        }

        String jsonBody = om.writeValueAsString(Map.of("code", code.trim()));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/attendance/mark"))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            throw new RuntimeException("Mark failed: " + res.statusCode() + " " + res.body());
        }
    }
}