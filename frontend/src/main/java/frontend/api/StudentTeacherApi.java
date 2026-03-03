package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;

import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.Map;

public class StudentTeacherApi {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();
    private final String baseUrl;

    public StudentTeacherApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String token(JwtStore jwtStore, AuthState state) {
        return jwtStore.load().map(AuthState::getToken).orElse(state.getToken());
    }

    public List<Map<String, Object>> getTeachers(JwtStore jwtStore, AuthState state) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/student/teachers"))
                .header("Authorization", "Bearer " + token(jwtStore, state))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            throw new RuntimeException("Teachers failed: " + res.statusCode() + " " + res.body());
        }

        return om.readValue(res.body(), new TypeReference<>() {});
    }
}