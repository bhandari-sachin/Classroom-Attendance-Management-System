package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.LocalizationSQL;
import service.TranslationService;

import java.io.OutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class I18nHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(I18nHandler.class.getName());

    private static final String METHOD_GET = "GET";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final String ERROR_METHOD_NOT_ALLOWED = "{\"error\":\"Method not allowed\"}";
    private static final String ERROR_NOT_FOUND = "{\"error\":\"Not found\"}";
    private static final String ERROR_SERVER = "{\"error\":\"Server error\"}";

    private final TranslationService service = new TranslationService();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!METHOD_GET.equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, ERROR_METHOD_NOT_ALLOWED);
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

            sendJson(exchange, 404, ERROR_NOT_FOUND);

        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Failed to handle i18n request", e);
            try {
                sendJson(exchange, 500, ERROR_SERVER);
            } catch (IOException ignored) {
                LOGGER.fine("Fallback error response could not be sent");
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

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
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