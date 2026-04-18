package frontend.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TranslationApi {

    private final String baseUrl;
    private final HttpClient client;

    public TranslationApi(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newHttpClient();
    }

    public Map<String, String> getUiTranslations(String languageCode) throws IOException, InterruptedException {
        String url = baseUrl + "/api/i18n/ui?lang=" +
                URLEncoder.encode(languageCode, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to load translations. HTTP " + response.statusCode());
        }

        return parseFlatJsonObject(response.body());
    }

    private Map<String, String> parseFlatJsonObject(String json) {
        Map<String, String> map = new HashMap<>();

        String trimmed = json == null ? "" : json.trim();
        if (trimmed.length() < 2 || trimmed.equals("{}")) {
            return map;
        }

        trimmed = trimmed.substring(1, trimmed.length() - 1).trim();

        if (trimmed.isBlank()) return map;

        String[] pairs = splitTopLevel(trimmed);
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) continue;

            String key = unquote(kv[0].trim());
            String value = unquote(kv[1].trim());
            map.put(key, value);
        }

        return map;
    }

    private String[] splitTopLevel(String s) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean escaped = false;

        for (char c : s.toCharArray()) {
            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                current.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                current.append(c);
                inQuotes = !inQuotes;
                continue;
            }

            if (c == ',' && !inQuotes) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        if (!current.isEmpty()) {
            parts.add(current.toString());
        }

        return parts.toArray(new String[0]);
    }

    private String unquote(String s) {
        String result = s;
        if (result.startsWith("\"") && result.endsWith("\"") && result.length() >= 2) {
            result = result.substring(1, result.length() - 1);
        }

        return result.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\\", "\\");
    }
}