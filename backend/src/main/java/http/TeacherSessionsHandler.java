package http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.SessionSQL;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

public class TeacherSessionsHandler implements HttpHandler {

    private final JwtService jwtService;
    private final ClassSQL classSQL;
    private final SessionSQL sessionSQL;
    private final ObjectMapper om = new ObjectMapper();

    public TeacherSessionsHandler(JwtService jwtService, ClassSQL classSQL, SessionSQL sessionSQL) {
        this.jwtService = jwtService;
        this.classSQL = classSQL;
        this.sessionSQL = sessionSQL;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            var jwt = Auth.requireJwt(ex, jwtService);
            Auth.requireRole(jwt, "TEACHER", "ADMIN");

            String method = ex.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                handleList(ex, jwt);
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                handleCreate(ex, jwt);
                return;
            }

            HttpUtil.send(ex, 405, "Method Not Allowed");

        } catch (SecurityException se) {
            HttpUtil.json(ex, 401, Map.of("error", se.getMessage()));
        } catch (IllegalArgumentException bad) {
            HttpUtil.json(ex, 400, Map.of("error", bad.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of("error", "Server error"));
        }
    }

    private void handleList(HttpExchange ex, com.auth0.jwt.interfaces.DecodedJWT jwt) throws Exception {
        URI uri = ex.getRequestURI();
        String qs = uri.getQuery(); // classId=123

        Long classId = Query.getLong(qs, "classId");
        if (classId == null) {
            HttpUtil.json(ex, 400, Map.of("error", "classId is required"));
            return;
        }

        long teacherId = (jwt.getClaim("id").isNull() || jwt.getClaim("id").asLong() == null)
                ? Long.parseLong(jwt.getSubject())
                : jwt.getClaim("id").asLong();

        String role = jwt.getClaim("role").isNull() ? "" : jwt.getClaim("role").asString();
        if (!"ADMIN".equalsIgnoreCase(role) && !classSQL.isClassOwnedByTeacher(classId, teacherId)) {
            HttpUtil.json(ex, 403, Map.of("error", "Forbidden: not your class"));
            return;
        }

        // ✅ Your existing method returns List<Map<String,Object>>
        var sessions = sessionSQL.listForClass(classId);

        HttpUtil.json(ex, 200, Map.of("data", sessions));
    }

    private void handleCreate(HttpExchange ex, com.auth0.jwt.interfaces.DecodedJWT jwt) throws Exception {

        Map<String, Object> body = om.readValue(ex.getRequestBody(), new TypeReference<>() {});

        Object classIdRaw = body.get("classId");
        if (classIdRaw == null) {
            HttpUtil.json(ex, 400, Map.of("error", "classId is required"));
            return;
        }

        long classId = (classIdRaw instanceof Number n)
                ? n.longValue()
                : Long.parseLong(String.valueOf(classIdRaw));

        long teacherId = (jwt.getClaim("id").isNull() || jwt.getClaim("id").asLong() == null)
                ? Long.parseLong(jwt.getSubject())
                : jwt.getClaim("id").asLong();

        String role = jwt.getClaim("role").isNull() ? "" : jwt.getClaim("role").asString();
        if (!"ADMIN".equalsIgnoreCase(role) && !classSQL.isClassOwnedByTeacher(classId, teacherId)) {
            HttpUtil.json(ex, 403, Map.of("error", "Forbidden: not your class"));
            return;
        }

        // optional start/end times from UI
        LocalTime start = parseTime(body.get("startTime"));
        LocalTime end = parseTime(body.get("endTime"));

        if (start == null) start = LocalTime.now().withSecond(0).withNano(0);
        if (end == null) end = start.plusHours(1);

        if (!end.isAfter(start)) {
            HttpUtil.json(ex, 400, Map.of("error", "endTime must be after startTime"));
            return;
        }

        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), start, end, code);

        HttpUtil.json(ex, 201, Map.of(
                "sessionId", sessionId,
                "classId", classId,
                "date", LocalDate.now().toString(),
                "startTime", start.toString(),
                "endTime", end.toString(),
                "code", code
        ));
    }

    private static LocalTime parseTime(Object raw) {
        if (raw == null) return null;
        String s = String.valueOf(raw).trim();
        if (s.isBlank()) return null;
        return LocalTime.parse(s); // "HH:mm" or "HH:mm:ss"
    }
}