package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import security.Auth;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;
import java.util.Map;

public class TeacherMarkAttendanceHandler extends BaseHandler implements HttpHandler {

    private final AttendanceService attendanceService;
    private final ObjectMapper om = new ObjectMapper();
    private static final String STATUS = "status";

    public TeacherMarkAttendanceHandler(JwtService jwtService, AttendanceService attendanceService) {
        super(jwtService, "POST");
        this.attendanceService = attendanceService;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        requireTeacher(ex, ctx);
        Map<String, Object> body = parseBody(ex);

        Number studentIdRaw = (Number) body.get("studentId");
        Number sessionIdRaw = (Number) body.get("sessionId");
        String status = body.get(STATUS) == null
                ? null
                : String.valueOf(body.get(STATUS)).trim().toUpperCase();

        if (studentIdRaw == null || sessionIdRaw == null || isBlank(status)) {
            throw new ApiException(400, "studentId, sessionId and status are required");
        }

        Long studentId = studentIdRaw.longValue();
        Long sessionId = sessionIdRaw.longValue();

        switch (status) {
            case "PRESENT" -> attendanceService.markPresent(studentId, sessionId);
            case "ABSENT" -> attendanceService.markAbsent(studentId, sessionId);
            case "EXCUSED" -> attendanceService.markExcused(studentId, sessionId);
            default -> throw new ApiException(400, "Invalid status");
        }

        HttpUtil.json(ex, 200, Map.of(
                "message", "Attendance updated",
                "studentId", studentId,
                "sessionId", sessionId,
                STATUS, status
        ));
    }

    private Map<String, Object> parseBody(HttpExchange ex) {
        try {
            return om.readValue(ex.getRequestBody(), Map.class);
        } catch (Exception e) {
            throw new ApiException(400, "Invalid JSON");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}