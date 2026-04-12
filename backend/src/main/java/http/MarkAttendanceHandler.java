package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import backend.exception.ApiException;
import security.JwtService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.AttendanceService;

import java.io.IOException;
import java.util.Map;

public class MarkAttendanceHandler extends BaseHandler implements HttpHandler {

    private final AttendanceService attendanceService;
    private final ObjectMapper om = new ObjectMapper();

    public MarkAttendanceHandler(JwtService jwtService, AttendanceService attendanceService) {
        super(jwtService, "POST");
        this.attendanceService = attendanceService;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        requireStudent(ex, ctx);
        Map<String, Object> body = parseBody(ex);

        String code = (String) body.get("code");

        if (code == null || code.isBlank()) {
            throw new ApiException(400, "Attendance code required");
        }

        Long studentId = ctx.getUserId();

        attendanceService.markByCode(studentId, code);

        HttpUtil.json(ex, 200, Map.of("status", "attendance marked"));
    }

    private Map<String, Object> parseBody(HttpExchange ex) {
        try {
            return om.readValue(ex.getRequestBody(), Map.class);
        } catch (Exception e) {
            throw new ApiException(400, "Invalid JSON");
        }
    }
}