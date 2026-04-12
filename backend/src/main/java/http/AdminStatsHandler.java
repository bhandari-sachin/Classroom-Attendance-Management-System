package http;

import com.sun.net.httpserver.HttpExchange;
import dto.AttendanceStats;
import security.JwtService;
import service.AttendanceService;

import java.io.IOException;

public class AdminStatsHandler extends BaseHandler {

    private final AttendanceService attendanceService;

    public AdminStatsHandler(JwtService jwtService, AttendanceService attendanceService) {
        super(jwtService, "GET");
        this.attendanceService = attendanceService;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        requireAdmin(ex, ctx);
        AttendanceStats stats = attendanceService.getOverallStats();
        HttpUtil.json(ex, 200, stats);
    }
}