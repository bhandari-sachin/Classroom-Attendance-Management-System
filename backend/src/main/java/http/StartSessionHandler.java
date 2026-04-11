package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import security.Auth;
import security.JwtService;
import service.SessionService;

import java.io.IOException;
import java.util.Map;

public class StartSessionHandler extends BaseHandler implements HttpHandler {

    private static final ObjectMapper om = new ObjectMapper();
    private final SessionService sessionService;

    public StartSessionHandler(JwtService jwtService, SessionService sessionService) {
        super(jwtService, "POST");
        this.sessionService = sessionService;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {

        requireTeacher(ex, ctx);
        Map<String, Object> body = parseBody(ex);

        Number sessionIdRaw = (Number) body.get("sessionId");

        if (sessionIdRaw == null) {
            throw new ApiException(400, "sessionId is required");
        }

        Long sessionId = sessionIdRaw.longValue();

        String code = sessionService.startSession(sessionId);

        HttpUtil.json(ex, 200, Map.of(
                "message", "Session started successfully",
                "sessionId", sessionId,
                "code", code
        ));
    }

    private Map<String, Object> parseBody(HttpExchange ex) {
        try {
            return om.readValue(ex.getRequestBody(), Map.class);
        } catch (Exception e) {
            throw new ApiException(400, "Invalid JSON");
        }
    }
}