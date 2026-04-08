package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.AttendanceSQL;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminAttendanceReportsHandler implements HttpHandler {

    private final JwtService jwtService;
    private final AttendanceSQL attendanceSQL;

    public AdminAttendanceReportsHandler(JwtService jwtService, AttendanceSQL attendanceSQL) {
        this.jwtService = jwtService;
        this.attendanceSQL = attendanceSQL;
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

            String query = ex.getRequestURI().getQuery();

            Long classId = HttpUtil.queryLong(query, "classId");
            String period = HttpUtil.queryString(query, "period");
            String search = HttpUtil.queryString(query, "search");
            String lang = HttpUtil.queryString(query, "lang");

            if (search == null) {
                search = "";
            }

            if (classId == null) {
                HttpUtil.json(ex, 400, Map.of("error", "classId is required"));
                return;
            }

            if (lang == null) {
                lang = "en";
            }

            List<dto.AttendanceView> rows = attendanceSQL.getAdminAttendanceReport(classId, period, search, lang);
            HttpUtil.json(ex, 200, rows);

        } catch (SecurityException sec) {
            HttpUtil.json(ex, 401, Map.of("error", sec.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }
}