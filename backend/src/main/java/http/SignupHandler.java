package http;

import backend.user.UserRepository;
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

        Map<String, String> body = om.readValue(ex.getRequestBody(), Map.class);

        String email = body.get("email");
        String password = body.get("password");
        String firstName = body.get("firstName");
        String lastName = body.get("lastName");
        String role = body.get("role"); // "STUDENT" / "TEACHER"
        String studentCode = body.get("studentCode"); // required if STUDENT

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