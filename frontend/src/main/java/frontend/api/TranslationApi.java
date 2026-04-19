package frontend.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * API client for loading UI translations from backend.
 *
 * <p>This class calls the /api/i18n/ui endpoint and returns
 * a flat map of translation keys and values for the requested language.</p>
 */
public class TranslationApi {

    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public TranslationApi(String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient(), new ObjectMapper());
    }

    public TranslationApi(String baseUrl, HttpClient client, ObjectMapper objectMapper) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.client = client;
        this.objectMapper = objectMapper;
    }

    /**
     * Loads UI translations for the given language code.
     *
     * @param languageCode language code such as "en", "fi", or "ar"
     * @return map of translation keys and localized values
     * @throws IOException if the request fails or the response cannot be parsed
     * @throws InterruptedException if the request is interrupted
     */
    public Map<String, String> getUiTranslations(String languageCode) throws IOException, InterruptedException {
        String url = baseUrl + "/api/i18n/ui?lang=" + encode(languageCode);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("Failed to load translations. HTTP "
                    + response.statusCode() + " - " + response.body());
        }

        return objectMapper.readValue(
                response.body(),
                new TypeReference<>() {
                }
        );
    }

    /**
     * Encodes a query parameter safely for use in URLs.
     */
    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
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