package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.ClassSQL;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.Map;

public class TeacherClassesHandler extends BaseHandler implements HttpHandler {

    private final ClassSQL classSQL;

    public TeacherClassesHandler(JwtService jwtService, ClassSQL classSQL) {
        super(jwtService, "GET");
        this.classSQL = classSQL;
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        requireTeacherOrAdmin(ex, ctx);
        Long teacherId = ctx.getUserId();

        var list = classSQL.listForTeacher(teacherId);

        HttpUtil.json(ex, 200, list);
    }
}