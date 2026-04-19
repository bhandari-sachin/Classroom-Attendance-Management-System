package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Shared base class for frontend API clients.
 *
 * <p>This class centralizes common HTTP behavior including:
 * authenticated request creation,
 * GET and POST JSON requests,
 * response parsing,
 * JWT token resolution,
 * and base URL validation.</p>
 */
public abstract class BaseApiClient {

    /**
     * HTTP header name for authorization.
     */
    protected static final String AUTHORIZATION = "Authorization";

    /**
     * Prefix used for bearer token authentication.
     */
    protected static final String BEARER_PREFIX = "Bearer ";

    /**
     * HTTP header name for content type.
     */
    protected static final String CONTENT_TYPE = "Content-Type";

    /**
     * JSON media type.
     */
    protected static final String APPLICATION_JSON = "application/json";

    /**
     * Base URL of the backend API.
     */
    protected final String baseUrl;

    /**
     * HTTP client used for requests.
     */
    protected final HttpClient client;

    /**
     * Object mapper used for JSON serialization and deserialization.
     */
    protected final ObjectMapper objectMapper;

    /**
     * Creates the shared API client base.
     *
     * @param baseUrl backend base URL
     * @param client HTTP client instance
     * @param objectMapper object mapper instance
     */
    protected BaseApiClient(String baseUrl, HttpClient client, ObjectMapper objectMapper) {
        this.baseUrl = stripTrailingSlash(baseUrl);

        if (client == null) {
            throw new IllegalArgumentException("HttpClient must not be null.");
        }
        if (objectMapper == null) {
            throw new IllegalArgumentException("ObjectMapper must not be null.");
        }

        this.client = client;
        this.objectMapper = objectMapper;
    }

    /**
     * Builds an authenticated HTTP request using the resolved JWT token.
     *
     * @param path endpoint path relative to the base URL
     * @param jwtStore JWT store used to resolve persisted token
     * @param state fallback authentication state
     * @return configured request builder with authorization header
     */
    protected HttpRequest.Builder authorizedRequest(String path, JwtStore jwtStore, AuthState state) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header(AUTHORIZATION, BEARER_PREFIX + resolveToken(jwtStore, state));
    }

    /**
     * Sends an authenticated GET request and returns the raw response body.
     *
     * @param path endpoint path
     * @param jwtStore JWT store used to resolve persisted token
     * @param state fallback authentication state
     * @return response body as string
     * @throws IOException if request sending fails
     * @throws InterruptedException if the request is interrupted
     */
    protected String get(String path, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        HttpRequest request = authorizedRequest(path, jwtStore, state)
                .GET()
                .build();

        return send(request);
    }

    /**
     * Sends an authenticated POST request with a JSON body and returns the raw response body.
     *
     * @param path endpoint path
     * @param body request body object
     * @param jwtStore JWT store used to resolve persisted token
     * @param state fallback authentication state
     * @return response body as string
     * @throws IOException if request sending or serialization fails
     * @throws InterruptedException if the request is interrupted
     */
    protected String postJson(String path, Object body, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = authorizedRequest(path, jwtStore, state)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        return send(request);
    }

    /**
     * Sends a GET request and parses the JSON response as a map.
     *
     * @param path endpoint path
     * @param jwtStore JWT store used to resolve persisted token
     * @param state fallback authentication state
     * @return parsed response map
     * @throws IOException if parsing or request fails
     * @throws InterruptedException if the request is interrupted
     */
    protected Map<String, Object> readGet(String path, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return objectMapper.readValue(get(path, jwtStore, state), new TypeReference<>() {});
    }

    /**
     * Sends a POST request and parses the JSON response as a map.
     *
     * @param path endpoint path
     * @param body request body object
     * @param jwtStore JWT store used to resolve persisted token
     * @param state fallback authentication state
     * @return parsed response map
     * @throws IOException if parsing or request fails
     * @throws InterruptedException if the request is interrupted
     */
    protected Map<String, Object> readPost(String path, Object body, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return objectMapper.readValue(postJson(path, body, jwtStore, state), new TypeReference<>() {});
    }

    /**
     * Sends a GET request and parses the JSON response as a list of maps.
     *
     * @param path endpoint path
     * @param jwtStore JWT store used to resolve persisted token
     * @param state fallback authentication state
     * @return parsed list response
     * @throws IOException if parsing or request fails
     * @throws InterruptedException if the request is interrupted
     */
    protected List<Map<String, Object>> readList(String path, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {
        return objectMapper.readValue(get(path, jwtStore, state), new TypeReference<>() {});
    }

    /**
     * Sends a GET request and parses a list response that may come in two formats:
     * a raw JSON array, or an object containing the array under a {@code data} field.
     *
     * @param path endpoint path
     * @param jwtStore JWT store used to resolve persisted token
     * @param state fallback authentication state
     * @return parsed list response, or empty list if no data field exists
     * @throws IOException if parsing or request fails
     * @throws InterruptedException if the request is interrupted
     */
    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> readWrappedList(String path, JwtStore jwtStore, AuthState state)
            throws IOException, InterruptedException {

        String json = get(path, jwtStore, state);

        if (json != null && json.trim().startsWith("[")) {
            return objectMapper.readValue(json, new TypeReference<>() {});
        }

        Map<String, Object> wrapper = objectMapper.readValue(json, new TypeReference<>() {});
        Object data = wrapper.get("data");

        if (data == null) {
            return List.of();
        }

        return (List<Map<String, Object>>) data;
    }

    /**
     * Sends the provided HTTP request and returns the response body.
     *
     * <p>If the backend returns an HTTP error status code,
     * an {@link ApiException} is thrown.</p>
     *
     * @param request HTTP request to send
     * @return response body as string
     * @throws IOException if request sending fails
     * @throws InterruptedException if the request is interrupted
     */
    protected String send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new ApiException("HTTP " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    /**
     * Resolves the JWT token from the store first, then falls back to the provided auth state.
     *
     * @param jwtStore JWT store used to load persisted state
     * @param state fallback auth state
     * @return resolved JWT token
     */
    protected String resolveToken(JwtStore jwtStore, AuthState state) {
        String token = jwtStore.load()
                .map(AuthState::getToken)
                .orElse(state != null ? state.getToken() : null);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("JWT token is missing. Please log in again.");
        }

        return token;
    }

    /**
     * Removes a trailing slash from the base URL to avoid malformed request URLs.
     *
     * @param url base URL
     * @return normalized URL without trailing slash
     */
    protected static String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be null or blank.");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Validates that a configured path is not null or blank.
     *
     * @param value path value to validate
     * @param fieldName field name used in the exception message
     */
    protected static void requirePath(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
    }
}