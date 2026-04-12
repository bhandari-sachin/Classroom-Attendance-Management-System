package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import config.ClassSQL;
import config.SessionSQL;
import backend.exception.DatabaseException;
import model.Session;
import security.JwtService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class TeacherSessionsListHandler extends BaseHandler {

    private final ClassSQL classSQL;
    private final SessionSQL sessionSQL;

    public TeacherSessionsListHandler(JwtService jwtService, ClassSQL classSQL, SessionSQL sessionSQL) {
        super(jwtService, "GET");
        this.classSQL = classSQL;
        this.sessionSQL = sessionSQL;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {

        requireTeacherOrAdmin(ex, ctx);
        DecodedJWT jwt = ctx.getJwt();

        String query = ex.getRequestURI().getQuery(); // classId=123
        Long classId = parseLongQueryParam(query, "classId");

        if (classId == null) {
            HttpUtil.json(ex, 400, Map.of("error", "classId is required"));
            return;
        }

        long teacherId = jwt.getClaim("id").isNull()
                ? Long.parseLong(jwt.getSubject())
                : jwt.getClaim("id").asLong();

        String role = jwt.getClaim("role").isNull()
                ? ""
                : jwt.getClaim("role").asString();

        if (!"ADMIN".equalsIgnoreCase(role)
                && !classSQL.isClassOwnedByTeacher(classId, teacherId)) {
            HttpUtil.json(ex, 403, Map.of("error", "Forbidden: not your class"));
            return;
        }

        List<Session> sessions =
                null;
        try {
            sessions = Collections.singletonList(sessionSQL.findById(classId));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch session by classId: " + classId, e);
        }

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
    }

    private static Long parseLongQueryParam(String query, String key) {
        if (query == null || query.isBlank()) return null;

        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                try {
                    return Long.parseLong(kv[1]);
                } catch (Exception ignored) {
                    // If the query parameter exists but is not a valid number,
                    // Returning null is handled by the caller as "missing/invalid input".
                }
            }
        }
        return null;
    }
}