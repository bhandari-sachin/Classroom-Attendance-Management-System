package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import security.Auth;
import security.JwtService;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.AttendanceService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MarkAttendanceHandler implements HttpHandler {

    private final JwtService jwtService;
    private final AttendanceService attendanceService;
    private final ObjectMapper om = new ObjectMapper();

    public MarkAttendanceHandler(JwtService jwtService, AttendanceService attendanceService) {
        this.jwtService = jwtService;
        this.attendanceService = attendanceService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            DecodedJWT jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "TEACHER", "ADMIN");

            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                HttpUtil.json(ex, 405, Map.of("error", "Method not allowed"));
                return;
            }

            InputStream in = ex.getRequestBody();
            Map body = om.readValue(in, Map.class);
            Long studentId = body.get("studentId") == null ? null : ((Number) body.get("studentId")).longValue();
            Long sessionId = body.get("sessionId") == null ? null : ((Number) body.get("sessionId")).longValue();
            String status = (String) body.get("status");
            String reason = (String) body.get("reason");

            if (studentId == null || sessionId == null || status == null) {
                HttpUtil.json(ex, 400, Map.of("error", "studentId, sessionId and status required"));
                return;
            }

            switch (status.toUpperCase()) {
                case "PRESENT" -> attendanceService.markPresent(studentId, sessionId);
                case "ABSENT" -> attendanceService.markAbsent(studentId, sessionId);
                case "EXCUSED" -> attendanceService.markExcused(studentId, sessionId, reason);
                default -> {
                    HttpUtil.json(ex, 400, Map.of("error", "unknown status"));
                    return;
                }
            }

            HttpUtil.json(ex, 200, Map.of("status", "ok"));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 403, Map.of("error", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", e.getMessage()));
        }
    }
}