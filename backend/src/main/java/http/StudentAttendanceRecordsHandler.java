package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.AttendanceView;
import security.Auth;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;
import java.util.List;

public class StudentAttendanceRecordsHandler implements HttpHandler {

    private final JwtService jwtService;
    private final AttendanceService attendanceService;

    public StudentAttendanceRecordsHandler(JwtService jwtService, AttendanceService attendanceService) {
        this.jwtService = jwtService;
        this.attendanceService = attendanceService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            DecodedJWT jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "STUDENT");

            Long studentId = Long.parseLong(jwt.getSubject());

            List<AttendanceView> rows = attendanceService.getStudentAttendanceViews(studentId);
            HttpUtil.json(ex, 200, rows);

        } catch (SecurityException se) {
            HttpUtil.json(ex, 401, java.util.Map.of("error", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, java.util.Map.of("error", "Server error"));
        }
    }
}
