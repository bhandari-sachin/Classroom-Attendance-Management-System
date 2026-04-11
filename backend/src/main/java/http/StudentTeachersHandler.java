package http;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import repository.UserRepository;
import security.Auth;
import security.JwtService;

import java.io.IOException;
import java.util.Map;

public class StudentTeachersHandler extends BaseHandler implements HttpHandler {

    private final UserRepository userRepository;

    public StudentTeachersHandler(JwtService jwtService, UserRepository userRepository) {
        super(jwtService);
        this.userRepository = userRepository;
    }

    @Override
    protected boolean supportsMethod(String method) {
        return method.equalsIgnoreCase("GET");
    }

    @Override
    protected String[] roles() {
        return new String[]{"ADMIN", "STUDENT", "TEACHER"};
    }

    @Override
    protected void handleRequest(HttpExchange ex, RequestContext ctx) throws IOException {
        var teachers = userRepository.findAllTeachers();

        HttpUtil.json(ex, 200, teachers);
    }
}