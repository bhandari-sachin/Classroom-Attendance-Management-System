package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.LocalizationSQL;
import service.TranslationService;

import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class I18nHandler implements HttpHandler {

    private final TranslationService service = new TranslationService();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            String lang = query.getOrDefault("lang", "en");

            if (path.endsWith("/ui")) {
                Map<String, String> translations = service.getUiTranslations(lang);
                sendJson(exchange, 200, toJsonObject(translations));
                return;
            }

            if (path.endsWith("/languages")) {
                List<LocalizationSQL.LanguageItem> languages = service.getActiveLanguages();
                sendJson(exchange, 200, toJsonArray(languages));
                return;
            }

            sendJson(exchange, 404, "{\"error\":\"Not found\"}");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                sendJson(exchange, 500, "{\"error\":\"Server error\"}");
            } catch (Exception ignored) {
            }
        }
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) return map;

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = decode(kv[0]);
            String value = kv.length > 1 ? decode(kv[1]) : "";
            map.put(key, value);
        }
        return map;
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws Exception {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String toJsonObject(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;

            sb.append("\"")
                    .append(escapeJson(e.getKey()))
                    .append("\":\"")
                    .append(escapeJson(e.getValue()))
                    .append("\"");
        }

        sb.append("}");
        return sb.toString();
    }

    private static String toJsonArray(List<LocalizationSQL.LanguageItem> languages) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean first = true;
        for (LocalizationSQL.LanguageItem lang : languages) {
            if (!first) sb.append(",");
            first = false;

            sb.append("{")
                    .append("\"code\":\"").append(escapeJson(lang.code())).append("\",")
                    .append("\"name\":\"").append(escapeJson(lang.name())).append("\",")
                    .append("\"isDefault\":").append(lang.isDefault()).append(",")
                    .append("\"isActive\":").append(lang.isActive())
                    .append("}");
        }

        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}