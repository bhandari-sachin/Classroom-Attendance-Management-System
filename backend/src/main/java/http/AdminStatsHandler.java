package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.AttendanceStats;
import security.Auth;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;

public class AdminStatsHandler implements HttpHandler {

    private final JwtService jwtService;
    private final AttendanceService attendanceService;

    public AdminStatsHandler(JwtService jwtService, AttendanceService attendanceService) {
        this.jwtService = jwtService;
        this.attendanceService = attendanceService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            DecodedJWT jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "ADMIN");

            AttendanceStats stats = attendanceService.getOverallStats();
            HttpUtil.json(ex, 200, stats);

        } catch (SecurityException se) {
            HttpUtil.json(ex, 403, java.util.Map.of("error", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, java.util.Map.of("error", "Server error"));
        }
    }
}