package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.AttendanceStats;
import security.Auth;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;
import java.util.Map;

public class StudentAttendanceSummaryHandler implements HttpHandler {

    private final JwtService jwtService;
    private final AttendanceService attendanceService;

    public StudentAttendanceSummaryHandler(JwtService jwtService, AttendanceService attendanceService) {
        this.jwtService = jwtService;
        this.attendanceService = attendanceService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            DecodedJWT jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "STUDENT");

            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                HttpUtil.json(ex, 405, Map.of("error", "Method Not Allowed"));
                return;
            }

            Long studentId = HttpUtil.jwtUserId(jwt);
            Long classId = HttpUtil.queryLong(ex.getRequestURI().getQuery(), "classId");
            String period = HttpUtil.queryString(ex.getRequestURI().getQuery(), "period");

            AttendanceStats s = attendanceService.getStudentStats(studentId, classId, period);

            HttpUtil.json(ex, 200, Map.of(
                    "presentCount", s.getPresentCount(),
                    "absentCount", s.getAbsentCount(),
                    "excusedCount", s.getExcusedCount(),
                    "totalDays", s.getTotalDays(),
                    "attendanceRate", s.getAttendanceRate()
            ));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 403, Map.of("error", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }
}