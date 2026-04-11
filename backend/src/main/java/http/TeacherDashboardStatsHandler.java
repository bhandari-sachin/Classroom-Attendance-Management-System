package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.AttendanceSQL;
import config.ClassSQL;
import security.JwtService;

import java.io.IOException;
import java.util.Map;

public class TeacherDashboardStatsHandler extends BaseHandler implements HttpHandler {

    private final ClassSQL classSQL;
    private final AttendanceSQL attendanceSQL;

    public TeacherDashboardStatsHandler(JwtService jwtService, ClassSQL classSQL, AttendanceSQL attendanceSQL) {
        super(jwtService);
        this.classSQL = classSQL;
        this.attendanceSQL = attendanceSQL;
    }

    @Override
    protected boolean supportsMethod(String method) {
        return method.equalsIgnoreCase("GET");
    }

    @Override
    protected String[] roles() {
        return new String[]{"ADMIN", "TEACHER"};
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        Long teacherId = ctx.getUserId();

        int totalClasses = classSQL.countForTeacher(teacherId);
        int totalStudents = classSQL.countStudentsForTeacher(teacherId);

        int presentToday = attendanceSQL.countTodayForTeacher(teacherId, "PRESENT");
        int absentToday = attendanceSQL.countTodayForTeacher(teacherId, "ABSENT");

        HttpUtil.json(ex, 200, Map.of(
                "totalClasses", totalClasses,
                "totalStudents", totalStudents,
                "presentToday", presentToday,
                "absentToday", absentToday
        ));
    }
}