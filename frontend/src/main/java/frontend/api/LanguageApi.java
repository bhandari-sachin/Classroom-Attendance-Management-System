package frontend.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class LanguageApi {

    private final String baseUrl;
    private final HttpClient client;

    public LanguageApi(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newHttpClient();
    }

    public List<LanguageItem> getActiveLanguages() throws IOException, InterruptedException {
        String url = baseUrl + "/api/i18n/languages";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to load languages. HTTP " + response.statusCode());
        }

        return parseLanguages(response.body());
    }

    private List<LanguageItem> parseLanguages(String json) {
        List<LanguageItem> list = new ArrayList<>();

        if (json == null) {
            return list;
        }

        String trimmed = json.trim();
        if (trimmed.isEmpty() || trimmed.equals("[]")) {
            return list;
        }

        if (trimmed.startsWith("[")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("]")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        List<String> objects = splitObjects(trimmed);

        for (String obj : objects) {
            String code = extractJsonString(obj, "code");
            String name = extractJsonString(obj, "name");
            boolean isDefault = extractJsonBoolean(obj, "isDefault");
            boolean isActive = extractJsonBoolean(obj, "isActive");

            if (code != null && name != null) {
                list.add(new LanguageItem(code, name, isDefault, isActive));
            }
        }

        return list;
    }

    private List<String> splitObjects(String s) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int braceDepth = 0;
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

            if (!inQuotes) {
                if (c == '{') {
                    braceDepth++;
                } else if (c == '}') {
                    braceDepth--;
                }

                if (c == ',' && braceDepth == 0) {
                    String part = current.toString().trim();
                    if (!part.isEmpty()) {
                        result.add(part);
                    }
                    current.setLength(0);
                    continue;
                }
            }

            current.append(c);
        }

        String part = current.toString().trim();
        if (!part.isEmpty()) {
            result.add(part);
        }

        return result;
    }

    private String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return null;
        }

        int colonIndex = json.indexOf(':', keyIndex);
        if (colonIndex < 0) {
            return null;
        }

        int firstQuote = json.indexOf('"', colonIndex + 1);
        if (firstQuote < 0) {
            return null;
        }

        StringBuilder value = new StringBuilder();
        boolean escaped = false;

        for (int i = firstQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                value.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                return value.toString();
            }

            value.append(c);
        }

        return null;
    }

    private boolean extractJsonBoolean(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return false;
        }

        int colonIndex = json.indexOf(':', keyIndex);
        if (colonIndex < 0) {
            return false;
        }

        String tail = json.substring(colonIndex + 1).trim();

        if (tail.startsWith("true")) {
            return true;
        }
        if (tail.startsWith("false")) {
            return false;
        }

        return false;
    }

    public record LanguageItem(String code, String name, boolean isDefault, boolean isActive) {
    }
}