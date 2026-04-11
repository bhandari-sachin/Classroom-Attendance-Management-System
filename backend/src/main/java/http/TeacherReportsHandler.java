package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import config.AttendanceSQL;
import config.ClassSQL;
import security.JwtService;

import java.io.IOException;
import java.util.Map;

public class TeacherReportsHandler extends BaseHandler {

    private final AttendanceSQL attendanceSQL;
    private final ClassSQL classSQL;

    public TeacherReportsHandler(
            JwtService jwtService,
            AttendanceSQL attendanceSQL,
            ClassSQL classSQL
    ) {
        super(jwtService);
        this.attendanceSQL = attendanceSQL;
        this.classSQL = classSQL;
    }

    @Override
    protected boolean supportsMethod(String method) {
        return method.equalsIgnoreCase("GET");
    }

    @Override
    protected String[] roles() {
        return new String[]{"TEACHER", "ADMIN"};
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {

        Long classId = ctx.getClassId();

        if (classId == null) {
            throw new ApiException(400, "classId query param is required");
        }

        Long teacherId = ctx.getUserId();
        DecodedJWT jwt = ctx.getJwt();

        String role = jwt.getClaim("role").isNull()
                ? ""
                : jwt.getClaim("role").asString();

        if (!"ADMIN".equalsIgnoreCase(role)
                && !classSQL.isClassOwnedByTeacher(classId, teacherId)) {
            throw new ApiException(403, "Forbidden: not your class");
        }

        var reportRows = attendanceSQL.reportByClass(classId);

        HttpUtil.json(ex, 200, Map.of(
                "classId", classId,
                "data", reportRows
        ));
    }
}