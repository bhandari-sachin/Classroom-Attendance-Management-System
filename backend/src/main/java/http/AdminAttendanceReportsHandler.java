package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.AttendanceSQL;
import security.JwtService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminAttendanceReportsHandler extends BaseHandler implements HttpHandler {

    private final AttendanceSQL attendanceSQL;

    public AdminAttendanceReportsHandler(JwtService jwtService, AttendanceSQL attendanceSQL) {
        super(jwtService);
        this.attendanceSQL = attendanceSQL;
    }

    @Override
    protected boolean supportsMethod(String method) {
        return method.equalsIgnoreCase("GET");
    }

    @Override
    protected String[] roles() {
        return new String[]{"ADMIN"};
    }


    @Override
    public void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {

        Long classId = ctx.getClassId();
        String period = ctx.getPeriod();
        String search = ctx.getQuery("search", "");
        String lang = ctx.getQuery("lang", "en");

        if (classId == null) {
            throw new ApiException(400, "classId is required");
        }

        if (lang == null) {
            lang = "en";
        }

        List<dto.AttendanceView> rows = attendanceSQL.getAdminAttendanceReport(classId, period, search, lang);
        HttpUtil.json(ex, 200, rows);
    }
}