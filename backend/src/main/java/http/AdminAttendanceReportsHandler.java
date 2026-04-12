package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.AttendanceSQL;
import backend.exception.ApiException;
import security.JwtService;

import java.io.IOException;
import java.util.List;

public class AdminAttendanceReportsHandler extends BaseHandler implements HttpHandler {

    private final AttendanceSQL attendanceSQL;

    public AdminAttendanceReportsHandler(JwtService jwtService, AttendanceSQL attendanceSQL) {
        super(jwtService, "GET");
        this.attendanceSQL = attendanceSQL;
    }

    @Override
    public void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {

        requireAdmin(ex, ctx);

        Long classId = ctx.getClassId();
        String period = ctx.getPeriod();
        String search = ctx.getQuery("search", "");
        String lang = ctx.getQuery("lang", "en");

        if (classId == null) {
            throw new ApiException(400, "classId is required");
        }

        List<dto.AttendanceView> rows = attendanceSQL.getAdminAttendanceReport(classId, period, search, lang);
        HttpUtil.json(ex, 200, rows);
    }
}