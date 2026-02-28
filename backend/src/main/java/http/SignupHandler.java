package http;

import repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.util.Map;

public class SignupHandler implements HttpHandler {

    private final UserRepository users;
    private final ObjectMapper om = new ObjectMapper();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public SignupHandler(UserRepository users) {
        this.users = users;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.send(ex, 405, "Method Not Allowed");
            return;
        }

        Map body = om.readValue(ex.getRequestBody(), Map.class);

        String email = body.get("email").toString();
        String password = body.get("password").toString();
        String firstName = body.get("firstName").toString();
        String lastName = body.get("lastName").toString();
        String role = body.get("role").toString(); // "STUDENT" / "TEACHER"
        String studentCode = body.get("studentCode").toString(); // required if STUDENT

        if (users.existsByEmail(email)) {
            HttpUtil.json(ex, 400, Map.of("error", "Email already exists"));
            return;
        }

        if ("ADMIN".equals(role)) {
            HttpUtil.json(ex, 403, Map.of("error", "Admin cannot self-register"));
            return;
        }

        if ("STUDENT".equals(role) && (studentCode == null || studentCode.isBlank())) {
            HttpUtil.json(ex, 400, Map.of("error", "Student code required"));
            return;
        }
        if (!"STUDENT".equals(role)) {
            studentCode = null; //  DB CHECK constraint
        }

        String hash = encoder.encode(password);
        users.insert(email, hash, firstName, lastName, role, studentCode);

        HttpUtil.json(ex, 201, Map.of("status", "created"));
    }
}