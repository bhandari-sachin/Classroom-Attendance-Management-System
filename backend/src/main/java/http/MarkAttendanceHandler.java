package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        super(jwtService);
        this.attendanceService = attendanceService;
    }

    @Override
    protected boolean supportsMethod(String method) {
        return method.equalsIgnoreCase("POST");
    }

    @Override
    protected String[] roles() {
        return new String[]{"STUDENT"};
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
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