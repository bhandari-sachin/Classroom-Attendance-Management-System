package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.ClassSQL;
import config.SessionSQL;
import model.Session;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TeacherSessionsListHandler implements HttpHandler {

    private final JwtService jwtService;
    private final ClassSQL classSQL;
    private final SessionSQL sessionSQL;

    public TeacherSessionsListHandler(JwtService jwtService, ClassSQL classSQL, SessionSQL sessionSQL) {
        this.jwtService = jwtService;
        this.classSQL = classSQL;
        this.sessionSQL = sessionSQL;
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

            String query = ex.getRequestURI().getQuery(); // classId=123
            Long classId = parseLongQueryParam(query, "classId");
            if (classId == null) {
                HttpUtil.json(ex, 400, Map.of("error", "classId is required"));
                return;
            }

            long teacherId = jwt.getClaim("id").isNull()
                    ? Long.parseLong(jwt.getSubject())
                    : jwt.getClaim("id").asLong();

            String role = jwt.getClaim("role").isNull() ? "" : jwt.getClaim("role").asString();

            // Teacher can only list sessions for their own class (admin can list any)
            if (!"ADMIN".equalsIgnoreCase(role) && !classSQL.isClassOwnedByTeacher(classId, teacherId)) {
                HttpUtil.json(ex, 403, Map.of("error", "Forbidden: not your class"));
                return;
            }

            List<Session> sessions = Collections.singletonList(sessionSQL.findById(classId));

            List<Map<String, Object>> payload = new ArrayList<>();
            for (Session s : sessions) {
                payload.add(Map.of(
                        "id", s.getId(),
                        "classId", s.getClassId(),
                        "date", s.getSessionDate().toString(),
                        "code", s.getQrCode()
                ));
            }

            HttpUtil.json(ex, 200, Map.of("data", payload));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 401, Map.of("error", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }

    private static Long parseLongQueryParam(String query, String key) {
        if (query == null || query.isBlank()) return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                try { return Long.parseLong(kv[1]); } catch (Exception ignored) {}
            }
        }
        return null;
    }
}