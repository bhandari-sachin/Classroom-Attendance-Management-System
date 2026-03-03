package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.ClassSQL;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.Map;

public class TeacherClassesHandler implements HttpHandler {

    private final JwtService jwtService;
    private final ClassSQL classSQL;

    public TeacherClassesHandler(JwtService jwtService, ClassSQL classSQL) {
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

            long teacherId = jwt.getClaim("id").isNull()
                    ? Long.parseLong(jwt.getSubject())
                    : jwt.getClaim("id").asLong();

            var list = classSQL.listForTeacher(teacherId);

            HttpUtil.json(ex, 200, Map.of("data", list));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 401, Map.of("error", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }
}