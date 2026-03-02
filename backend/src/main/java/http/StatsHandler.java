package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.AttendanceStats;
import service.AttendanceService;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class StatsHandler implements HttpHandler {
    private final AttendanceService attendanceService;
    private final ObjectMapper om = new ObjectMapper();

    public StatsHandler(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            URI uri = ex.getRequestURI();
            String path = uri.getPath();
            String[] parts = path.split("/");

            if ("GET".equalsIgnoreCase(method)) {
                if (parts.length == 3) {
                    String query = uri.getQuery();
                    String range = null;
                    if (query != null) {
                        for (String q : query.split("&")) {
                            if (q.startsWith("range=")) range = q.substring("range=".length());
                        }
                    }

                    AttendanceStats s;
                    if (range == null) {
                        s = attendanceService.getOverallStats();
                    } else {
                        switch (range) {
                            case "Last Month" -> s = attendanceService.getStatsLastMonth();
                            case "This Year" -> s = attendanceService.getStatsThisYear();
                            default -> s = attendanceService.getStatsThisMonth();
                        }
                    }

                    HttpUtil.json(ex, 200, s == null ? new AttendanceStats(0,0,0,0) : s);
                    return;
                }

                if (parts.length >= 5) {
                    String type = parts[3];
                    Long id = Long.parseLong(parts[4]);
                    String query = uri.getQuery();
                    String range = "This Month";
                    Long classId = null;
                    if (query != null) {
                        for (String q : query.split("&")) {
                            if (q.startsWith("range=")) range = q.substring("range=".length());
                            if (q.startsWith("classId=")) {
                                try { classId = Long.parseLong(q.substring("classId=".length())); } catch (Exception ignored) {}
                            }
                        }
                    }

                    switch (type) {
                        case "class" -> {
                            AttendanceStats out;
                            switch (range) {
                                case "Last Month" -> out = attendanceService.getClassStatsLastMonth(id);
                                case "This Year" -> out = attendanceService.getClassStatsThisYear(id);
                                default -> out = attendanceService.getClassStatsThisMonth(id);
                            }
                            HttpUtil.json(ex, 200, out == null ? new AttendanceStats(0,0,0,0) : out);
                            return;
                        }
                        case "student" -> {
                            AttendanceStats out;
                            switch (range) {
                                case "Last Month" -> out = attendanceService.getStudentStatsLastMonth(id, classId == null ? -1L : classId);
                                case "This Year" -> out = attendanceService.getStudentStatsThisYear(id, classId == null ? -1L : classId);
                                default -> out = attendanceService.getStudentStatsThisMonth(id, classId == null ? -1L : classId);
                            }
                            HttpUtil.json(ex, 200, out == null ? new AttendanceStats(0,0,0,0) : out);
                            return;
                        }
                    }
                }
            }

            HttpUtil.json(ex, 404, Map.of("error", "Not found"));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", e.getMessage()));
        }
    }
}
