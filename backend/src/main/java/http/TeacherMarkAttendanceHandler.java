package http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import security.Auth;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;
import java.util.Map;

public class TeacherMarkAttendanceHandler implements HttpHandler {

    private final JwtService jwtService;
    private final AttendanceService attendanceService;
    private final ObjectMapper om = new ObjectMapper();
    private static final String ERROR = "error";
    private static final String STATUS = "status";

    public TeacherMarkAttendanceHandler(JwtService jwtService, AttendanceService attendanceService) {
        this.jwtService = jwtService;
        this.attendanceService = attendanceService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            var jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "TEACHER");

            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                HttpUtil.json(ex, 405, Map.of(ERROR, "Method Not Allowed"));
                return;
            }

            Map<String, Object> body = parseJsonBody(ex);
            if (body.isEmpty()) return;

            Number studentIdRaw = (Number) body.get("studentId");
            Number sessionIdRaw = (Number) body.get("sessionId");
            String status = body.get(STATUS) == null ? null : String.valueOf(body.get(STATUS)).trim().toUpperCase();

            if (studentIdRaw == null || sessionIdRaw == null || status == null || status.isBlank()) {
                HttpUtil.json(ex, 400, Map.of(ERROR, "studentId, sessionId and status are required"));
                return;
            }

            Long studentId = studentIdRaw.longValue();
            Long sessionId = sessionIdRaw.longValue();

            switch (status) {
                case "PRESENT" -> attendanceService.markPresent(studentId, sessionId);
                case "ABSENT" -> attendanceService.markAbsent(studentId, sessionId);
                case "EXCUSED" -> attendanceService.markExcused(studentId, sessionId);
                default -> {
                    HttpUtil.json(ex, 400, Map.of(ERROR, "Invalid status"));
                    return;
                }
            }

            HttpUtil.json(ex, 200, Map.of(
                    "message", "Attendance updated",
                    "studentId", studentId,
                    "sessionId", sessionId,
                    STATUS, status
            ));

        } catch (SecurityException sec) {
            HttpUtil.json(ex, 401, Map.of(ERROR, sec.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of(ERROR, "Server error"));
        }
    }

    private Map<String, Object> parseJsonBody(HttpExchange ex) throws IOException {
        try {
            return om.readValue(ex.getRequestBody(), new TypeReference<>() {});
        } catch (Exception e) {
            HttpUtil.json(ex, 400, Map.of(ERROR, "Invalid JSON"));
            return Map.of();
        }
    }
}