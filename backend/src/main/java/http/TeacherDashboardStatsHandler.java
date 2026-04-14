package http;

import com.sun.net.httpserver.HttpExchange;
import security.JwtService;
import config.ClassSQL;
import config.AttendanceSQL;

import java.io.IOException;

public class TeacherDashboardStatsHandler extends BaseHandler {

    private final ClassSQL classSQL;
    private final AttendanceSQL attendanceSQL;

    public TeacherDashboardStatsHandler(
            JwtService jwtService,
            ClassSQL classSQL,
            AttendanceSQL attendanceSQL
    ) {
        super(jwtService, "GET");
        this.classSQL = classSQL;
        this.attendanceSQL = attendanceSQL;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        HttpUtil.json(ex, 200, "TODO dashboard stats");
    }
}