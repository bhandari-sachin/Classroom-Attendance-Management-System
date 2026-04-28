package http;

import backend.exception.ReportExportException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lowagie.text.DocumentException;
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
    private static final String ERROR = "error";
    private static final String CONTENT_TYPE = "content-type";
    private static final String CONTENT_DISPOSITION = "content-disposition";
    private static final String PDF_MIME = "application/pdf";

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
            String lang = queryParams.getOrDefault("lang", "en");
            String format = normalizeFormat(queryParams.get("format"));

            if (!"GET".equalsIgnoreCase(method)) {
                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            // /api/reports/export/student?format=pdf  (students: PDF only)
            if (path.endsWith("/student")) {
                Auth.requireRole(jwt, "STUDENT");
                if (!"pdf".equals(format)) {
                    HttpUtil.json(ex, 400, Map.of(ERROR, "Students may only export PDF reports."));
                    return;
                }
                exportStudentReport(ex, jwt, queryParams, lang);
                return;
            }

            // /api/reports/export/teacher?classId=1&format=pdf|csv
            if (path.endsWith("/teacher")) {
                Auth.requireRole(jwt, "TEACHER");
                exportTeacherReport(ex, jwt, queryParams, format, lang);
                return;
            }

            // /api/reports/export/admin?format=pdf|csv
            if (path.endsWith("/admin")) {
                Auth.requireRole(jwt, "ADMIN");
                exportAdminReport(ex, format, lang);
                return;
            }

            HttpUtil.send(ex, 404, "Not Found");

        } catch (SecurityException sec) {
            HttpUtil.json(ex, 401, java.util.Map.of(ERROR, sec.getMessage()));
        } catch (IllegalArgumentException badReq) {
            HttpUtil.json(ex, 400, java.util.Map.of(ERROR, badReq.getMessage()));
        } catch (Exception e) {
            HttpUtil.json(ex, 500, java.util.Map.of(ERROR, "Server error"));
        }
    }

    private void exportStudentReport(HttpExchange ex, DecodedJWT jwt, Map<String, String> queryParams, String lang) throws IOException {

        try {
            Long studentId = jwt.getClaim("id").asLong();
            int year = resolveYear(queryParams);

            var rows = attendanceSQL.getStudentYearlyReport(studentId, year);

            String file = "student-report.pdf";

            PDFReportExporter.studentYearReport(file, year, rows, lang);

            sendFile(ex, file, PDF_MIME, "student-report.pdf");
        } catch (IOException | DocumentException e) {
            throw new ReportExportException("Failed to export teacher report", e);
        }
    }

    private void exportTeacherReport(HttpExchange ex, DecodedJWT jwt, Map<String, String> queryParams, String format, String lang) throws IOException {

        try {
            Long teacherId = jwt.getClaim("id").asLong();
            Long classId = parseRequiredLong(queryParams, "classId");
            int year = resolveYear(queryParams);

            var rows = attendanceSQL.getTeacherClassReport(teacherId, classId, year);

            if ("csv".equals(format)) {
                ex.getResponseHeaders().set(CONTENT_TYPE, "text/csv; charset=UTF-8");
                ex.getResponseHeaders().set(CONTENT_DISPOSITION, "attachment; filename=\"teacher-report.csv\"");
                ex.sendResponseHeaders(200, 0);

                try (var os = ex.getResponseBody()) {
                    CSVReportExporter.teacherClassReport(os, year, rows, lang);
                }
                return;
            }
            String file = "teacher-report.pdf";

            PDFReportExporter.teacherClassReport(file, year, rows, lang);

            sendFile(ex, file, PDF_MIME, "teacher-report.pdf");
        } catch (IOException | DocumentException e) {
            throw new ReportExportException("Failed to export teacher report", e);
        }
    }

    private void exportAdminReport(HttpExchange ex, String format, String lang) throws IOException {
        try {
            var rows = attendanceSQL.getAllStudentsStats();

            if ("csv".equals(format)) {
                ex.getResponseHeaders().set(CONTENT_TYPE, "text/csv; charset=UTF-8");
                ex.getResponseHeaders().set(CONTENT_DISPOSITION, "attachment; filename=\"teacher-report.csv\"");
                ex.sendResponseHeaders(200, 0);

                try (var os = ex.getResponseBody()) {
                    CSVReportExporter.adminAllStudentsReport(os, rows, lang);
                }
                return;
            }

            String file = "admin-report.pdf";

            PDFReportExporter.adminAllStudentsReport(file, rows, lang);

            sendFile(ex, file, PDF_MIME, "admin-report.pdf");
        } catch (IOException | DocumentException e) {
            throw new ReportExportException("Failed to export teacher report", e);
        }
    }

    private int resolveYear(Map<String, String> queryParams) {
        String y = queryParams.get("year");

        if (y == null || y.isBlank()) {
            return java.time.Year.now().getValue();
        }

        try {
            return Integer.parseInt(y);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid year: " + y);
        }
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

    protected void sendFile(HttpExchange ex, String file, String contentType, String downloadName) throws IOException {
        File outFile = new File(file);

        ex.getResponseHeaders().set(CONTENT_TYPE, contentType);
        ex.getResponseHeaders().set(CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"");

        ex.sendResponseHeaders(200, outFile.length());

        try (FileInputStream fis = new FileInputStream(outFile);
             var os = ex.getResponseBody()) {
            fis.transferTo(os);
        }
    }
}