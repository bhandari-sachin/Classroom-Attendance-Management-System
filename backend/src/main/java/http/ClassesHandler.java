package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.CourseClass;
import model.Student;
import service.ClassService;
import service.SessionService;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class ClassesHandler implements HttpHandler {
    private final ClassService classService;
    private final SessionService sessionService;
    private final ObjectMapper om = new ObjectMapper();

    public ClassesHandler(ClassService classService, SessionService sessionService) {
        this.classService = classService;
        this.sessionService = sessionService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            URI uri = ex.getRequestURI();
            String path = uri.getPath();

            String[] parts = path.split("/");

            if ("GET".equalsIgnoreCase(method)) {
                // /api/classes
                if (parts.length == 3) {
                    String query = uri.getQuery();
                    Long teacherId = null;
                    Long studentId = null;
                    if (query != null) {
                        for (String q : query.split("&")) {
                            if (q.startsWith("teacherId=")) {
                                try { teacherId = Long.parseLong(q.substring("teacherId=".length())); } catch (NumberFormatException ignored) {}
                            }
                            if (q.startsWith("studentId=")) {
                                try { studentId = Long.parseLong(q.substring("studentId=".length())); } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                    List<CourseClass> classes;
                    if (studentId != null) {
                        classes = classService.getClassesForStudent(studentId);
                    } else if (teacherId != null) {
                        classes = classService.getClassesByTeacher(teacherId);
                    } else {
                        classes = classService.getAllClasses();
                    }
                    HttpUtil.json(ex, 200, classes);
                    return;
                }

                // /api/classes/{id}/students or /api/classes/{id}/sessions or /api/classes/{id}/count
                if (parts.length >= 5) {
                    Long classId = Long.parseLong(parts[3]);
                    String sub = parts[4];
                    if ("students".equals(sub)) {
                        List<Student> students = classService.getStudentsInClass(classId);
                        HttpUtil.json(ex, 200, students);
                        return;
                    }
                    if ("sessions".equals(sub)) {
                        var sessions = sessionService.getSessionsByClassId(classId);
                        HttpUtil.json(ex, 200, sessions);
                        return;
                    }
                    if ("count".equals(sub)) {
                        int count = classService.getEnrollmentCount(classId);
                        HttpUtil.json(ex, 200, Map.of("count", count));
                        return;
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

