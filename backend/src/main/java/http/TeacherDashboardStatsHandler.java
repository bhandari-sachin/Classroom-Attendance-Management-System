package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.AttendanceSQL;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.Map;

public class TeacherDashboardStatsHandler implements HttpHandler {

    private final JwtService jwtService;
    private final ClassSQL classSQL;
    private final AttendanceSQL attendanceSQL;

    public TeacherDashboardStatsHandler(JwtService jwtService, ClassSQL classSQL, AttendanceSQL attendanceSQL) {
        this.jwtService = jwtService;
        this.classSQL = classSQL;
        this.attendanceSQL = attendanceSQL;
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

            long teacherId = jwt.getClaim("id").isNull()
                    ? Long.parseLong(jwt.getSubject())
                    : jwt.getClaim("id").asLong();

            int totalClasses  = classSQL.countForTeacher(teacherId);
            int totalStudents = classSQL.countStudentsForTeacher(teacherId);

            int presentToday = attendanceSQL.countTodayForTeacher(teacherId, "PRESENT");
            int absentToday  = attendanceSQL.countTodayForTeacher(teacherId, "ABSENT");

            HttpUtil.json(ex, 200, Map.of(
                    "totalClasses", totalClasses,
                    "totalStudents", totalStudents,
                    "presentToday", presentToday,
                    "absentToday", absentToday
            ));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 401, Map.of("error", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }
}