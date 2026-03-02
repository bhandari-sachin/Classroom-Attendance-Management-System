package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpUtil {
    private static final ObjectMapper om = new ObjectMapper();

    static {
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void send(HttpExchange ex, int code, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
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
}