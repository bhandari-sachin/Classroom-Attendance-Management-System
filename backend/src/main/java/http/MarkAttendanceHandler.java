package http;

import backend.security.Auth;
import backend.security.JwtService;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class MarkAttendanceHandler implements HttpHandler {

    private final JwtService jwtService;

    public MarkAttendanceHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            DecodedJWT jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "TEACHER", "ADMIN");

            // read body JSON, do DB work...
            HttpUtil.json(ex, 200, java.util.Map.of("status", "ok"));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 403, java.util.Map.of("error", se.getMessage()));
        }
    }
}