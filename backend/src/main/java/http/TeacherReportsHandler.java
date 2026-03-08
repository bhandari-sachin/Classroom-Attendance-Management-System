package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.AttendanceSQL;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.Map;

public class TeacherReportsHandler implements HttpHandler {

    private final JwtService jwtService;
    private final AttendanceSQL attendanceSQL;
    private final ClassSQL classSQL;

    public TeacherReportsHandler(JwtService jwtService, AttendanceSQL attendanceSQL, ClassSQL classSQL) {
        this.jwtService = jwtService;
        this.attendanceSQL = attendanceSQL;
        this.classSQL = classSQL;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            var jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "TEACHER", "ADMIN");

            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            String q = ex.getRequestURI().getQuery(); // e.g. classId=3
            Long classId = null;
            if (q != null) {
                for (String part : q.split("&")) {
                    String[] kv = part.split("=");
                    if (kv.length == 2 && "classId".equals(kv[0])) {
                        classId = Long.parseLong(kv[1]);
                    }
                }
            }

            if (classId == null) {
                HttpUtil.json(ex, 400, Map.of("error", "classId query param is required"));
                return;
            }

            long teacherId = jwt.getClaim("id").isNull()
                    ? Long.parseLong(jwt.getSubject())
                    : jwt.getClaim("id").asLong();

            String role = jwt.getClaim("role").isNull() ? "" : jwt.getClaim("role").asString();

            if (!"ADMIN".equalsIgnoreCase(role) && !classSQL.isClassOwnedByTeacher(classId, teacherId)) {
                HttpUtil.json(ex, 403, Map.of("error", "Forbidden: not your class"));
                return;
            }

            var reportRows = attendanceSQL.reportByClass(classId);

            HttpUtil.json(ex, 200, Map.of(
                    "classId", classId,
                    "data", reportRows
            ));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 401, Map.of("error", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }
}