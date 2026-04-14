package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * API client for retrieving available UI languages from backend.
 *
 * <p>This class communicates with the /api/i18n/languages endpoint
 * and maps the JSON response into LanguageItem objects.</p>
 */
public class LanguageApi {

    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public LanguageApi(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient(), new ObjectMapper());
    }

    public LanguageApi(String baseUrl, HttpClient client, ObjectMapper objectMapper) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.client = client;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches all active languages from backend.
     *
     * @return list of available languages
     * @throws IOException if request fails
     * @throws InterruptedException if request is interrupted
     */
    public List<LanguageItem> getActiveLanguages() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/i18n/languages"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("Failed to load languages. HTTP " + response.statusCode()
                    + " - " + response.body());
        }

        return objectMapper.readValue(
                response.body(),
                new TypeReference<>() {}
        );
    }

    /**
     * Removes trailing slash to avoid double slashes in URLs.
     */
    private String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be null or empty");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * DTO representing a language entry returned from backend.
     */
    public record LanguageItem(
            String code,
            String name,
            boolean isDefault,
            boolean isActive
    ) {}
}