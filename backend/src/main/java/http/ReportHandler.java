package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.AttendanceSQL;
import security.Auth;
import security.JwtService;
import util.PDFReportExporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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

            if (!"GET".equalsIgnoreCase(method)) {
                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            // /api/reports/export/student
            if (path.endsWith("/student")) {
                Auth.requireRole(jwt, "STUDENT");
                exportStudentReport(ex, jwt);
                return;
            }

            // /api/reports/export/teacher
            if (path.endsWith("/teacher")) {
                Auth.requireRole(jwt, "TEACHER");
                exportTeacherReport(ex, jwt);
                return;
            }

            // /api/reports/export/admin
            if (path.endsWith("/admin")) {
                Auth.requireRole(jwt, "ADMIN");
                exportAdminReport(ex);
                return;
            }

            HttpUtil.send(ex, 404, "Not Found");

        } catch (SecurityException sec) {
            HttpUtil.json(ex, 401, java.util.Map.of("error", sec.getMessage()));
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

        sendPdf(ex, file);
    }

    private void exportTeacherReport(HttpExchange ex, DecodedJWT jwt) throws Exception {

        Long teacherId = ((Number) jwt.getClaim("id")).longValue();

        String query = ex.getRequestURI().getQuery();
        Long classId = Long.parseLong(query.split("=")[1]);

        var rows = attendanceSQL.getTeacherClassReport(teacherId, classId);

        String file = "teacher-report.pdf";

        PDFReportExporter.teacherClassReport(file, rows);

        sendPdf(ex, file);
    }

    private void exportAdminReport(HttpExchange ex) throws Exception {

        var rows = attendanceSQL.getAllStudentsStats();

        String file = "admin-report.pdf";

        PDFReportExporter.adminAllStudentsReport(file, rows);

        sendPdf(ex, file);
    }

    private void sendPdf(HttpExchange ex, String file) throws IOException {

        File pdf = new File(file);

        ex.getResponseHeaders().add("Content-Type", "application/pdf");
        ex.getResponseHeaders().add("Content-Disposition", "attachment; filename=report.pdf");

        ex.sendResponseHeaders(200, pdf.length());

        try (FileInputStream fis = new FileInputStream(pdf);
             var os = ex.getResponseBody()) {

            fis.transferTo(os);
        }
    }
}