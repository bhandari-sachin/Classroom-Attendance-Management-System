package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.SessionService;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class SessionsHandler implements HttpHandler {
    private final SessionService sessionService;
    private final ObjectMapper om = new ObjectMapper();

    public SessionsHandler(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            URI uri = ex.getRequestURI();
            String path = uri.getPath();
            String[] parts = path.split("/");

            if ("POST".equalsIgnoreCase(method)) {
                if (parts.length >= 5) {
                    Long sessionId = Long.parseLong(parts[3]);
                    String action = parts[4];
                    if ("start".equalsIgnoreCase(action)) {
                        String code = sessionService.startSession(sessionId);
                        HttpUtil.json(ex, 200, Map.of("code", code));
                        return;
                    }
                    if ("end".equalsIgnoreCase(action)) {
                        sessionService.endSession(sessionId);
                        HttpUtil.json(ex, 200, Map.of("status", "ok"));
                        return;
                    }
                }
            }

            HttpUtil.json(ex, 404, Map.of("error", "Not found"));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", e.getMessage()));
        }
    }
}

