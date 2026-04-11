package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.ClassSQL;
import security.JwtService;

import java.io.IOException;
import java.util.Map;

public class TeacherStudentsHandler extends BaseHandler implements HttpHandler {

    private final ClassSQL classSQL;

    public TeacherStudentsHandler(JwtService jwtService, ClassSQL classSQL) {
        super(jwtService);
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
        Long classId = ctx.getLongQuery("classId");
        if (classId == null) {
            throw new ApiException(400, "classId is required");
        }

        Long teacherId = ctx.getUserId();
        DecodedJWT jwt = ctx.getJwt();

        String role = jwt.getClaim("role").isNull() ? "" : jwt.getClaim("role").asString();
        if (!"ADMIN".equalsIgnoreCase(role) && !classSQL.isClassOwnedByTeacher(classId, teacherId)) {
            throw new ApiException(403, "Forbidden: not your class");
        }

        var students = classSQL.listStudentsForClass(classId);

        HttpUtil.json(ex, 200, Map.of("data", students));
    }
}