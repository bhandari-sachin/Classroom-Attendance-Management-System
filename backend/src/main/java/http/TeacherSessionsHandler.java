package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.ClassSQL;
import config.SessionSQL;
import exception.ApiException;
import security.JwtService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

public class TeacherSessionsHandler extends BaseHandler implements HttpHandler {

    private final ClassSQL classSQL;
    private final SessionSQL sessionSQL;
    private final ObjectMapper om = new ObjectMapper();
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String CLASS_ID = "classId";

    public TeacherSessionsHandler(JwtService jwtService, ClassSQL classSQL, SessionSQL sessionSQL) {
        super(jwtService, "GET", "POST");
        this.classSQL = classSQL;
        this.sessionSQL = sessionSQL;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        requireTeacherOrAdmin(ex, ctx);
        String method = ex.getRequestMethod();

        if ("GET".equalsIgnoreCase(method)) {
            handleList(ex, ctx);
            return;
        }

        if ("POST".equalsIgnoreCase(method)) {
            handleCreate(ex, ctx);
            return;
        }

        throw new ApiException(405, "Method Not Allowed");
    }


    private void handleList(HttpExchange ex, RequestContext ctx) throws IOException {

        Long classId = ctx.getLongQuery(CLASS_ID);
        if (classId == null) {
            throw new ApiException(400, "classId is required");
        }

        Long teacherId = ctx.getUserId();
        DecodedJWT jwt = ctx.getJwt();

        String role = jwt.getClaim("role").isNull()
                ? ""
                : jwt.getClaim("role").asString();

        if (!ADMIN_ROLE.equalsIgnoreCase(role)
                && !classSQL.isClassOwnedByTeacher(classId, teacherId)) {
            throw new ApiException(403, "Forbidden: not your class");
        }

        var sessions = sessionSQL.listForClass(classId);

        HttpUtil.json(ex, 200, Map.of("data", sessions));
    }

    private void handleCreate(HttpExchange ex, RequestContext ctx) throws IOException {
        DecodedJWT jwt = ctx.getJwt();

        Map<String, Object> body =
                om.readValue(ex.getRequestBody(), new TypeReference<>() {});

        Object classIdRaw = body.get(CLASS_ID);
        if (classIdRaw == null) {
            throw new ApiException(400, "classId is required");
        }

        long classId = (classIdRaw instanceof Number n)
                ? n.longValue()
                : Long.parseLong(String.valueOf(classIdRaw));

        long teacherId = ctx.getUserId();

        String role = jwt.getClaim("role").isNull() ? "" : jwt.getClaim("role").asString();
        if (!ADMIN_ROLE.equalsIgnoreCase(role) && !classSQL.isClassOwnedByTeacher(classId, teacherId)) {
            throw new ApiException(403, "Forbidden: not your class");
        }

        // optional start/end times from UI
        LocalTime start = parseTime(body.get("startTime"));
        LocalTime end = parseTime(body.get("endTime"));

        if (start == null) start = LocalTime.now().withSecond(0).withNano(0);
        if (end == null) end = start.plusHours(1);

        if (!end.isAfter(start)) {
            throw new ApiException(400, "endTime must be after startTime");
        }

        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        long sessionId = sessionSQL.createSession(classId, LocalDate.now(), start, end, code);

        HttpUtil.json(ex, 201, Map.of(
                "sessionId", sessionId,
                CLASS_ID, classId,
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