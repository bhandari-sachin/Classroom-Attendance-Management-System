package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.AttendanceView;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;
import java.util.List;

public class StudentAttendanceRecordsHandler extends BaseHandler implements HttpHandler {

    private final AttendanceService attendanceService;

    public StudentAttendanceRecordsHandler(JwtService jwtService, AttendanceService attendanceService) {
        super(jwtService, "GET");
        this.attendanceService = attendanceService;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        requireStudent(ex, ctx);
        Long studentId = ctx.getUserId();
        Long classId = ctx.getClassId();
        String period = ctx.getPeriod();

        String lang = ctx.getQuery("lang", "en"); // improve RequestContext 👇

        List<AttendanceView> records =
                attendanceService.getStudentAttendanceViews(studentId, classId, period, lang);

        HttpUtil.json(ex, 200, records);
    }
}