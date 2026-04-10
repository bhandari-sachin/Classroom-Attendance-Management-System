package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import model.Session;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class TeacherSessionReportHandler implements HttpHandler {

    private final JwtService jwtService;
    private final ClassSQL classSQL;
    private final SessionSQL sessionSQL;
    private final AttendanceSQL attendanceSQL;
    private static final String ERROR = "error";

    public TeacherSessionReportHandler(JwtService jwtService, ClassSQL classSQL, SessionSQL sessionSQL, AttendanceSQL attendanceSQL) {
        this.jwtService = jwtService;
        this.classSQL = classSQL;
        this.sessionSQL = sessionSQL;
        this.attendanceSQL = attendanceSQL;
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
            String qs = uri.getQuery();
            Long sessionId = Query.getLong(qs, "sessionId");
            if (sessionId == null) {
                HttpUtil.json(ex, 400, Map.of(ERROR, "sessionId is required"));
                return;
            }

            Session session = sessionSQL.findById(sessionId);
            if (session == null) {
                HttpUtil.json(ex, 404, Map.of(ERROR, "Session not found"));
                return;
            }

            long teacherId = jwt.getClaim("id").isNull()
                    ? Long.parseLong(jwt.getSubject())
                    : jwt.getClaim("id").asLong();

            String role = jwt.getClaim("role").isNull() ? "" : jwt.getClaim("role").asString();
            if (!"ADMIN".equalsIgnoreCase(role) && !classSQL.isClassOwnedByTeacher(session.getClassId(), teacherId)) {
                HttpUtil.json(ex, 403, Map.of(ERROR, "Forbidden: not your session"));
                return;
            }

            String languageCode = Query.get(qs, "languageCode");
            if (languageCode == null) {
                languageCode = "en";
            }
            var report = attendanceSQL.getSessionReport(sessionId, languageCode);

            HttpUtil.json(ex, 200, Map.of(
                    "sessionId", sessionId,
                    "classId", session.getClassId(),
                    "date", session.getSessionDate().toString(),
                    "report", report,
                    "lang", languageCode
            ));

        } catch (SecurityException se) {
            HttpUtil.json(ex, 401, Map.of(ERROR, se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.json(ex, 500, Map.of(ERROR, "Server error"));
        }
    }
}