package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.AttendanceStats;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;
import java.util.Map;

public class StudentAttendanceSummaryHandler extends BaseHandler implements HttpHandler {

    private final AttendanceService attendanceService;

    public StudentAttendanceSummaryHandler(JwtService jwtService, AttendanceService attendanceService) {
        super(jwtService, "GET");
        this.attendanceService = attendanceService;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        requireStudent(ex, ctx);
        Long studentId = ctx.getUserId();
        Long classId = ctx.getClassId();
        String period = ctx.getPeriod();

        AttendanceStats s = attendanceService.getStudentStats(studentId, classId, period);

        HttpUtil.json(ex, 200, Map.of(
                "presentCount", s.getPresentCount(),
                "absentCount", s.getAbsentCount(),
                "excusedCount", s.getExcusedCount(),
                "totalDays", s.getTotalDays(),
                "attendanceRate", s.getAttendanceRate()
        ));
    }
}