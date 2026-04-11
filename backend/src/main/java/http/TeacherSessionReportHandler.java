package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import model.Session;
import security.JwtService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class TeacherSessionReportHandler extends BaseHandler {

    private final ClassSQL classSQL;
    private final SessionSQL sessionSQL;
    private final AttendanceSQL attendanceSQL;

    public TeacherSessionReportHandler(
            JwtService jwtService,
            ClassSQL classSQL,
            SessionSQL sessionSQL,
            AttendanceSQL attendanceSQL
    ) {
        super(jwtService, "GET");
        this.classSQL = classSQL;
        this.sessionSQL = sessionSQL;
        this.attendanceSQL = attendanceSQL;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {

        requireTeacherOrAdmin(ex, ctx);
        Long sessionId = ctx.getLongQuery("sessionId");
        if (sessionId == null) {
            throw new ApiException(400, "sessionId is required");
        }

        Session session = null;
        try {
            session = sessionSQL.findById(sessionId);
        } catch (SQLException e) {
            throw new ApiException(500, "Database error");
        }
        if (session == null) {
            throw new ApiException(404, "Session not found");
        }

        Long teacherId = ctx.getUserId();
        DecodedJWT jwt = ctx.getJwt();

        String role = jwt.getClaim("role").isNull()
                ? ""
                : jwt.getClaim("role").asString();

        if (!"ADMIN".equalsIgnoreCase(role)
                && !classSQL.isClassOwnedByTeacher(session.getClassId(), teacherId)) {
            throw new ApiException(403, "Forbidden: not your session");
        }

        String languageCode = ctx.getQuery("languageCode", "en");

        var report = attendanceSQL.getSessionReport(sessionId, languageCode);

        HttpUtil.json(ex, 200, Map.of(
                "sessionId", sessionId,
                "classId", session.getClassId(),
                "date", session.getSessionDate().toString(),
                "report", report,
                "lang", languageCode
        ));
    }
}