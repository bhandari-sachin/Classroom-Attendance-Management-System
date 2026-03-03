package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.ClassSQL;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class TeacherStudentsHandler implements HttpHandler {

    private final JwtService jwtService;
    private final ClassSQL classSQL;

    public TeacherStudentsHandler(JwtService jwtService, ClassSQL classSQL) {
        this.jwtService = jwtService;
        this.classSQL = classSQL;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            var jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "TEACHER", "ADMIN");

            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                HttpUtil.send(ex, 405, "Method Not Allowed");
                return;
            }

            URI uri = ex.getRequestURI();
            String qs = uri.getQuery(); // classId=123
            Long classId = Query.getLong(qs, "classId");
            if (classId == null) {
                HttpUtil.json(ex, 400, Map.of("error", "classId is required"));
                return;
            }

            long teacherId = jwt.getClaim("id").isNull()
                    ? Long.parseLong(jwt.getSubject())
                    : jwt.getClaim("id").asLong();

            String role = jwt.getClaim("role").isNull() ? "" : jwt.getClaim("role").asString();
            if (!"ADMIN".equalsIgnoreCase(role) && !classSQL.isClassOwnedByTeacher(classId, teacherId)) {
                HttpUtil.json(ex, 403, Map.of("error", "Forbidden: not your class"));
                return;
            }

            var students = classSQL.listStudentsForClass(classId);

            HttpUtil.json(ex, 200, Map.of("data", students));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 401, Map.of("error", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }
}