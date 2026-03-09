package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class HttpUtil {

    private static final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    static {
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void send(HttpExchange ex, int code, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    public static void json(HttpExchange ex, int code, Object obj) throws IOException {
        byte[] bytes = om.writeValueAsBytes(obj);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    public static long jwtUserId(com.auth0.jwt.interfaces.DecodedJWT jwt) {
        Long idClaim = jwt.getClaim("id").asLong();
        return (idClaim != null) ? idClaim : Long.parseLong(jwt.getSubject());
    }

    public static Long queryLong(String query, String key) {
        if (query == null || query.isBlank()) return null;

        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                String val = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                if (val.isBlank()) return null;
                return Long.parseLong(val);
            }
        }
        return null;
    }
    public static String queryString(String query, String key) {
        if (query == null || query.isBlank()) return null;

        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                String val = java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
                return val.isBlank() ? null : val;
            }
        }
        return null;
    }
}