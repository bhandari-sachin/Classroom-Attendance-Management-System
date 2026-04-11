package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import dto.AttendanceStats;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;

public class AdminStatsHandler extends BaseHandler {

    private final AttendanceService attendanceService;

    public AdminStatsHandler(JwtService jwtService, AttendanceService attendanceService) {
        super(jwtService);
        this.attendanceService = attendanceService;
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
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        AttendanceStats stats = attendanceService.getOverallStats();
        HttpUtil.json(ex, 200, stats);
    }
}