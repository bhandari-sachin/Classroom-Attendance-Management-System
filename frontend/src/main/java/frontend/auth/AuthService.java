package frontend.auth;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class AuthService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl;

    public AuthService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public AuthState login(String email, String password) throws IOException, InterruptedException {
        String jsonBody = """
        {"email": "%s", "password": "%s"}
        """.formatted(escape(email), escape(password));

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

        String jsonBody = """
    {
      "email": "%s",
      "password": "%s",
      "firstName": "%s",
      "lastName": "%s",
      "role": "%s",
      "studentCode": "%s"
    }
    """.formatted(
                escape(email),
                escape(password),
                escape(firstName),
                escape(lastName),
                escape(role == null ? "STUDENT" : role.name()),
                escape(studentCode == null ? "" : studentCode)
        );

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

    // --- tiny JSON parsing without extra libs ---
    // Expected keys: token, role, name
    private AuthState parseAuthState(String json) {
        String token = extract(json, "token");
        String role  = extract(json, "role");
        String name  = extract(json, "name");
        return new AuthState(token, Role.fromString(role), name);
    }

    private String extract(String json, String key) {
        // Very simple extraction for: "key":"value"
        String needle = "\"" + key + "\":";
        int i = json.indexOf(needle);
        if (i < 0) return "";
        int start = json.indexOf('"', i + needle.length());
        int end = json.indexOf('"', start + 1);
        if (start < 0 || end < 0) return "";
        return json.substring(start + 1, end);
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}