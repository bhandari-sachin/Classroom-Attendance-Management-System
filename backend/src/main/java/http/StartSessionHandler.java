package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import security.Auth;
import security.JwtService;
import service.SessionService;

import java.io.IOException;
import java.util.Map;

public class StartSessionHandler implements HttpHandler {

    private static final ObjectMapper om = new ObjectMapper();

    private final JwtService jwtService;
    private final SessionService sessionService;
    private static final String ERROR = "error";

    public StartSessionHandler(JwtService jwtService, SessionService sessionService) {
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            var jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "TEACHER");

            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                HttpUtil.json(ex, 405, Map.of(ERROR, "Method not allowed"));
                return;
            }

            Map<String, Object> body = om.readValue(ex.getRequestBody(), Map.class);

            Number sessionIdRaw = (Number) body.get("sessionId");
            if (sessionIdRaw == null) {
                HttpUtil.json(ex, 400, Map.of(ERROR, "sessionId is required"));
                return;
            }

            Long sessionId = sessionIdRaw.longValue();
            String code = sessionService.startSession(sessionId);

            HttpUtil.json(ex, 200, Map.of(
                    "message", "Session started successfully",
                    "sessionId", sessionId,
                    "code", code
            ));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 403, Map.of(ERROR, se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of(ERROR, "Server error"));
        }
    }
}