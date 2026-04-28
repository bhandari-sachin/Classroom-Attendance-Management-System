package frontend.api;

import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.i18n.FrontendI18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * API client for exporting report files from the backend.
 *
 * <p>This class supports exporting:
 * student reports,
 * teacher reports,
 * and admin reports.</p>
 */
public class ReportApi {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final HttpClient client;
    private final String baseUrl;

    public ReportApi(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient());
    }

    public ReportApi(String baseUrl, HttpClient client) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.client = client;
    }

    /**
     * Exports the current student's report as PDF.
     *
     * @param store JWT storage
     * @param state fallback auth state
     * @param destinationPath local file path where the report will be saved
     */
    public void exportStudentPdf(JwtStore store, AuthState state, String destinationPath)
            throws IOException, InterruptedException {

        String path = "/api/reports/export/student"
                + "?format=pdf"
                + "&lang=" + encode(FrontendI18n.getLanguage());

        downloadToFile(path, resolveToken(store, state), destinationPath);
    }

    /**
     * Exports a teacher report for a specific class.
     *
     * @param store JWT storage
     * @param state fallback auth state
     * @param classId target class id
     * @param format export format such as pdf or csv
     * @param destinationPath local file path where the report will be saved
     */
    public void exportTeacherReport(JwtStore store, AuthState state, long classId, String format, String destinationPath)
            throws IOException, InterruptedException {

        String path = "/api/reports/export/teacher"
                + "?classId=" + classId
                + "&format=" + encode(format)
                + "&lang=" + encode(FrontendI18n.getLanguage());

        downloadToFile(path, resolveToken(store, state), destinationPath);
    }

    /**
     * Exports an admin report.
     *
     * @param store JWT storage
     * @param state fallback auth state
     * @param format export format such as pdf or csv
     * @param destinationPath local file path where the report will be saved
     */
    public void exportAdminReport(JwtStore store, AuthState state, String format, String destinationPath)
            throws IOException, InterruptedException {

        String path = "/api/reports/export/admin"
                + "?format=" + encode(format)
                + "&lang=" + encode(FrontendI18n.getLanguage());

        downloadToFile(path, resolveToken(store, state), destinationPath);
    }

    /**
     * Resolves the JWT token from the store first, then falls back to the provided auth state.
     */
    private String resolveToken(JwtStore store, AuthState state) {
        String token = store.load()
                .map(AuthState::token)
                .orElse(state != null ? state.token() : null);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("JWT token is missing. Please log in again.");
        }

        return token;
    }

    /**
     * Downloads a file from the given API path and saves it to the provided local path.
     */
    private void downloadToFile(String path, String token, String destinationPath)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header(AUTHORIZATION, BEARER_PREFIX + token)
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() >= 400) {
            try (InputStream errorStream = response.body()) {
                String errorBody = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                throw new ApiException("Export failed (HTTP " + response.statusCode() + "): " + errorBody);
            }
        }

        try (InputStream inputStream = response.body();
             OutputStream outputStream = java.nio.file.Files.newOutputStream(java.nio.file.Path.of(destinationPath))) {
            inputStream.transferTo(outputStream);
        }
    }

    /**
     * Encodes a query parameter value safely for URLs.
     */
    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    /**
     * Removes trailing slash from base URL to avoid double slashes.
     */
    private String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be null or blank.");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}