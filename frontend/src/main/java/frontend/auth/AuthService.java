package frontend.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service responsible for authentication-related backend calls.
 *
 * <p>This class supports:
 * login,
 * signup,
 * and parsing authentication responses.</p>
 */
public class AuthService {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final HttpClient client;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public AuthService(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient(), new ObjectMapper());
    }

    public AuthService(String baseUrl, HttpClient client, ObjectMapper objectMapper) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.client = Objects.requireNonNull(client, "HttpClient must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper must not be null");
    }

    /**
     * Logs in a user and returns the resulting authentication state.
     *
     * @param email user email
     * @param password user password
     * @return authenticated user state
     * @throws IOException if request fails
     * @throws InterruptedException if request is interrupted
     */
    public AuthState login(String email, String password) throws IOException, InterruptedException {
        Map<String, Object> payload = Map.of(
                "email", normalizeEmail(email),
                "password", normalizePassword(password)
        );

        String responseBody = postJson("/api/auth/login", payload);
        return parseAuthState(responseBody);
    }

    /**
     * Registers a new user.
     *
     * @param firstName user's first name
     * @param lastName user's last name
     * @param email user's email
     * @param password user's password
     * @param role user role
     * @param studentCode optional student code
     * @throws IOException if request fails
     * @throws InterruptedException if request is interrupted
     */
    public void signup(String firstName,
                       String lastName,
                       String email,
                       String password,
                       Role role,
                       String studentCode) throws IOException, InterruptedException {

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", normalizeEmail(email));
        payload.put("password", normalizePassword(password));
        payload.put("firstName", normalizeText(firstName));
        payload.put("lastName", normalizeText(lastName));
        payload.put("role", role == null ? Role.STUDENT.name() : role.name());

        String normalizedStudentCode = normalizeNullableText(studentCode);
        if (normalizedStudentCode != null && !normalizedStudentCode.isBlank()) {
            payload.put("studentCode", normalizedStudentCode);
        }

        postJson("/api/auth/signup", payload);
    }

    /**
     * Sends a POST request with JSON body and returns the response body.
     */
    private String postJson(String path, Object payload) throws IOException, InterruptedException {
        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        return send(request);
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
     * Parses login response JSON into AuthState.
     */
    private AuthState parseAuthState(String json) {
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});

            String token = valueAsString(map.get("token"));
            String roleValue = valueAsString(map.get("role"));
            String name = valueAsString(map.get("name"));

            if (token == null || token.isBlank()) {
                throw new IllegalStateException("Login response is missing token.");
            }

            if (roleValue == null || roleValue.isBlank()) {
                throw new IllegalStateException("Login response is missing role.");
            }

            return new AuthState(token, Role.fromString(roleValue), name);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse login response: " + json, e);
        }
    }

    /**
     * Converts an object value to string safely.
     */
    private String valueAsString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Normalizes email by trimming and converting to lowercase.
     */
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    /**
     * Keeps password unchanged except for null protection.
     */
    private String normalizePassword(String password) {
        return password == null ? "" : password;
    }

    /**
     * Normalizes regular text input by trimming whitespace.
     */
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Normalizes nullable text input by trimming whitespace but preserving null.
     */
    private String normalizeNullableText(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Removes trailing slash from base URL to avoid malformed URLs.
     */
    private String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be null or blank.");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}