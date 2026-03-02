package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.AttendanceService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class AttendanceCodeHandler implements HttpHandler {
    private final AttendanceService attendanceService;
    private final ObjectMapper om = new ObjectMapper();

    public AttendanceCodeHandler(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                HttpUtil.json(ex, 405, Map.of("error", "Method not allowed"));
                return;
            }

            InputStream in = ex.getRequestBody();
            Map body = om.readValue(in, Map.class);
            Long studentId = body.get("studentId") == null ? null : ((Number) body.get("studentId")).longValue();
            String code = (String) body.get("code");

            if (studentId == null || code == null) {
                HttpUtil.json(ex, 400, Map.of("success", false, "error", "studentId and code required"));
                return;
            }

            boolean ok = attendanceService.submitAttendanceByCode(studentId, code);
            HttpUtil.json(ex, 200, Map.of("success", ok));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("success", false, "error", e.getMessage()));
        }
    }
}

