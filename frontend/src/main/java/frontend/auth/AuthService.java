package frontend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl;
    private final ObjectMapper om = new ObjectMapper();

    public AuthService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public AuthState login(String email, String password) throws IOException, InterruptedException {
        // Normalize email to avoid whitespace/case issues
        email = email == null ? "" : email.trim().toLowerCase();
        password = password == null ? "" : password; // keep password exact

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);

        String jsonBody = om.writeValueAsString(payload);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("Login failed: " + res.statusCode() + " " + res.body());
        }

        return parseAuthState(res.body());
    }

    public void signup(String firstName, String lastName, String email, String password, Role role, String studentCode)
            throws IOException, InterruptedException {

        // Normalize inputs
        firstName = firstName == null ? "" : firstName.trim();
        lastName = lastName == null ? "" : lastName.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        password = password == null ? "" : password; // keep password exact
        studentCode = studentCode == null ? null : studentCode.trim();

        String roleStr = (role == null ? "STUDENT" : role.name());

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);
        payload.put("role", roleStr);

        // Only include studentCode if it actually exists (cleaner + avoids weird empty-string cases)
        if (studentCode != null && !studentCode.isBlank()) {
            payload.put("studentCode", studentCode);
        }

        String jsonBody = om.writeValueAsString(payload);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/signup"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {
            throw new RuntimeException("Signup failed: " + res.statusCode() + " " + res.body());
        }
    }

    @SuppressWarnings("unchecked")
    private AuthState parseAuthState(String json) {
        try {
            Map<String, Object> map = om.readValue(json, Map.class);

            String token = (String) map.get("token");
            String role  = (String) map.get("role");
            String name  = (String) map.get("name");
            String language = (String) map.get("language");

            if (token == null || token.isBlank() || role == null || role.isBlank()) {
                throw new RuntimeException("Invalid login response JSON: " + json);
            }

            return new AuthState(token, Role.fromString(role), name, language);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse login response: " + json, e);
        }
    }
}