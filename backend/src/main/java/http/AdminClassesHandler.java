package http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.ClassSQL;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminClassesHandler implements HttpHandler {

    private final JwtService jwtService;
    private final ClassSQL classSQL;
    private final ObjectMapper om = new ObjectMapper();

    public AdminClassesHandler(JwtService jwtService, ClassSQL classSQL) {
        this.jwtService = jwtService;
        this.classSQL = classSQL;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            var jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "ADMIN");

            String method = ex.getRequestMethod();
            String path = ex.getRequestURI().getPath();

            // /api/admin/classes
            if ("/api/admin/classes".equals(path)) {
                if ("GET".equalsIgnoreCase(method)) {
                    handleGetAllClasses(ex);
                    return;
                }

                if ("POST".equalsIgnoreCase(method)) {
                    handleCreateClass(ex);
                    return;
                }

                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            // /api/admin/classes/{classCode}/available-students
            if (path.startsWith("/api/admin/classes/") && path.endsWith("/available-students")) {
                if ("GET".equalsIgnoreCase(method)) {
                    handleAvailableStudents(ex, path);
                    return;
                }

                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            // /api/admin/classes/{classCode}/enroll
            if (path.startsWith("/api/admin/classes/") && path.endsWith("/enroll")) {
                if ("POST".equalsIgnoreCase(method)) {
                    handleEnrollStudents(ex, path);
                    return;
                }

                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            HttpUtil.send(ex, 404, "Not Found");

        } catch (SecurityException sec) {
            HttpUtil.json(ex, 401, Map.of("error", sec.getMessage()));
        } catch (IllegalArgumentException bad) {
            HttpUtil.json(ex, 400, Map.of("error", bad.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }

    private void handleGetAllClasses(HttpExchange ex) throws IOException {
        List<ClassSQL.ClassView> list = classSQL.listAllForAdmin();

        List<Map<String, Object>> payload = new ArrayList<>();
        for (var c : list) {
            payload.add(Map.of(
                    "id", c.id,
                    "classCode", n(c.classCode),
                    "name", n(c.name),
                    "teacherEmail", n(c.teacherEmail),
                    "semester", n(c.semester),
                    "academicYear", n(c.academicYear),
                    "students", c.studentsCount
            ));
        }

        HttpUtil.json(ex, 200, payload);
    }

    private void handleCreateClass(HttpExchange ex) throws IOException {
        Map<String, Object> body;
        try {
            body = om.readValue(ex.getRequestBody(), new TypeReference<>() {});
        } catch (Exception parseErr) {
            HttpUtil.json(ex, 400, Map.of("error", "Invalid JSON"));
            return;
        }

        String classCode = str(body.get("classCode"));
        String name = str(body.get("name"));
        String teacherEmail = str(body.get("teacherEmail"));
        String semester = optStr(body.get("semester"));
        String academicYear = optStr(body.get("academicYear"));
        Integer maxCapacity = optInt(body.get("maxCapacity"));

        if (isBlank(classCode) || isBlank(name) || isBlank(teacherEmail)) {
            throw new IllegalArgumentException("classCode, name, and teacherEmail are required");
        }

        Long teacherId = classSQL.findTeacherIdByEmail(teacherEmail);
        if (teacherId == null) {
            throw new IllegalArgumentException("Teacher not found (or not TEACHER): " + teacherEmail);
        }

        long newId = classSQL.createClass(classCode, name, teacherId, semester, academicYear, maxCapacity);

        HttpUtil.json(ex, 201, Map.of(
                "status", "created",
                "id", newId
        ));
    }

    private void handleAvailableStudents(HttpExchange ex, String path) throws IOException {
        String classCode = extractClassCode(path, "/available-students");
        if (isBlank(classCode)) {
            HttpUtil.json(ex, 400, Map.of("error", "Missing class code"));
            return;
        }

        List<Map<String, Object>> students = classSQL.listStudentsNotEnrolledInClass(classCode);
        HttpUtil.json(ex, 200, students);
    }

    private void handleEnrollStudents(HttpExchange ex, String path) throws IOException {
        String classCode = extractClassCode(path, "/enroll");
        if (isBlank(classCode)) {
            HttpUtil.json(ex, 400, Map.of("error", "Missing class code"));
            return;
        }

        List<String> studentEmails;
        try {
            studentEmails = om.readValue(ex.getRequestBody(), new TypeReference<List<String>>() {});
        } catch (Exception parseErr) {
            HttpUtil.json(ex, 400, Map.of("error", "Invalid JSON body, expected array of student emails"));
            return;
        }

        classSQL.enrollStudentsByEmails(classCode, studentEmails);

        HttpUtil.json(ex, 200, Map.of(
                "status", "ok",
                "message", "Students enrolled successfully"
        ));
    }

    private String extractClassCode(String path, String suffix) {
        String prefix = "/api/admin/classes/";
        if (!path.startsWith(prefix) || !path.endsWith(suffix)) return null;

        String middle = path.substring(prefix.length(), path.length() - suffix.length());
        if (middle.endsWith("/")) {
            middle = middle.substring(0, middle.length() - 1);
        }
        return middle.trim();
    }

    private static String n(String s) {
        return s == null ? "" : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isBlank();
    }

    private static String str(Object v) {
        if (v == null) return null;
        return String.valueOf(v).trim();
    }

    private static String optStr(Object v) {
        String s = str(v);
        return (s == null || s.isBlank()) ? null : s;
    }

    private static Integer optInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try {
            String s = String.valueOf(v).trim();
            if (s.isBlank()) return null;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }
}