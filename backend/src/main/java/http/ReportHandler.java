package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.AttendanceSQL;
import security.Auth;
import security.JwtService;
import util.CSVReportExporter;
import util.PDFReportExporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ReportHandler implements HttpHandler {

    private final JwtService jwtService;
    private final AttendanceSQL attendanceSQL;

    public ReportHandler(JwtService jwtService, AttendanceSQL attendanceSQL) {
        this.jwtService = jwtService;
        this.attendanceSQL = attendanceSQL;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        try {

            DecodedJWT jwt = Auth.requireJwt(ex, jwtService);

            String method = ex.getRequestMethod();
            String path = ex.getRequestURI().getPath();
            Map<String, String> queryParams = parseQuery(ex.getRequestURI().getQuery());
            String format = normalizeFormat(queryParams.get("format"));

            if (!"GET".equalsIgnoreCase(method)) {
                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            // /api/reports/export/student?format=pdf
            if (!"pdf".equals(format)) {
                HttpUtil.json(ex, 400, Map.of("error", "Invalid format. Only 'pdf' is supported."));
                return;
            }

            // /api/reports/export/teacher?classId=1&format=pdf|csv
            if (path.endsWith("/teacher")) {
                Auth.requireRole(jwt, "TEACHER");
                exportTeacherReport(ex, jwt, queryParams, format);
                return;
            }

            // /api/reports/export/admin?format=pdf|csv
            if (path.endsWith("/admin")) {
                Auth.requireRole(jwt, "ADMIN");
                exportAdminReport(ex, format);
                return;
            }

            HttpUtil.send(ex, 404, "Not Found");

        } catch (SecurityException sec) {
            HttpUtil.json(ex, 401, java.util.Map.of("error", sec.getMessage()));
        } catch (IllegalArgumentException badReq) {
            HttpUtil.json(ex, 400, java.util.Map.of("error", badReq.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, java.util.Map.of("error", "Server error"));
        }
    }

    private void exportStudentReport(HttpExchange ex, DecodedJWT jwt) throws Exception {

        Long studentId = ((Number) jwt.getClaim("id")).longValue();
        int year = java.time.Year.now().getValue();

        var rows = attendanceSQL.getStudentYearlyReport(studentId, year);

        String file = "student-report.pdf";

        PDFReportExporter.studentYearReport(file, year, rows);

        sendFile(ex, file, "application/pdf", "student-report.pdf");
    }

    private void exportTeacherReport(HttpExchange ex, DecodedJWT jwt, Map<String, String> queryParams, String format) throws Exception {

        Long teacherId = ((Number) jwt.getClaim("id")).longValue();
        Long classId = parseRequiredLong(queryParams, "classId");

        var rows = attendanceSQL.getTeacherClassReport(teacherId, classId);

        if ("csv".equals(format)) {
            String file = "teacher-report.csv";
            CSVReportExporter.teacherClassReport(file, rows);
            sendFile(ex, "text/csv", "teacher-report.csv", file);
            return;
        }
        String file = "teacher-report.pdf";

        PDFReportExporter.teacherClassReport(file, rows);

        sendFile(ex, file, "application/pdf", "teacher-report.pdf");
    }

    private void exportAdminReport(HttpExchange ex, String format) throws Exception {

        var rows = attendanceSQL.getAllStudentsStats();

        if ("csv".equals(format)) {
            String file = "admin-report.csv";
            CSVReportExporter.adminAllStudentsReport(file, rows);
            sendFile(ex, file, "text/csv", "admin-report.csv");
            return;
        }

        String file = "admin-report.pdf";

        PDFReportExporter.adminAllStudentsReport(file, rows);

        sendFile(ex, file, "application/pdf", "admin-report.pdf");
    }

    private Long parseRequiredLong(Map<String, String> queryParams, String key) {
        String raw = queryParams.get(key);

        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Missing required query parameter: " + key);
        }

        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value for " + key + ": " + raw);
        }
    }

    private String normalizeFormat(String raw) {
        if (raw == null || raw.isBlank()) {
            return "pdf";
        }

        String format = raw.trim().toLowerCase();

        if (!format.equals("pdf") && !format.equals("csv")) {
            throw new IllegalArgumentException("Unsupported format: " + raw);
        }

        return format;
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new HashMap<>();

        if (rawQuery == null || rawQuery.isBlank()) {
            return params;
        }

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            if (pair == null || pair.isBlank()) continue;

            String[] kv = pair.split("=", 2);
            String key = decode(kv[0]);
            String value = kv.length > 1 ? decode(kv[1]) : "";

            params.put(key, value);
        }

        return params;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private void sendFile(HttpExchange ex, String file, String contentType, String downloadName) throws IOException {
        File outFile = new File(file);

        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + downloadName + "\"");

        ex.sendResponseHeaders(200, outFile.length());

        try (FileInputStream fis = new FileInputStream(outFile);
             var os = ex.getResponseBody()) {
            fis.transferTo(os);
        }
    }
}