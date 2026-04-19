package http;

import repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
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
    private static final String ERROR = "error";

    public SignupHandler(UserRepository users) {
        this.users = users;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.send(ex, 405, "Method Not Allowed");
            return;
        }

        Map<String, Object> body;
        try {
            body = om.readValue(ex.getRequestBody(), new TypeReference<>() {});
        } catch (Exception e) {
            HttpUtil.json(ex, 400, Map.of(ERROR, "Invalid JSON"));
            return;
        }

        String email = asTrimmed(body.get("email"));
        String password = asTrimmed(body.get("password"));
        String firstName = asTrimmed(body.get("firstName"));
        String lastName = asTrimmed(body.get("lastName"));
        String role = asTrimmed(body.get("role")); // STUDENT / TEACHER
        String studentCode = asTrimmed(body.get("studentCode")); // only for STUDENT

        // Basic required fields
        if (email == null || password == null || firstName == null || lastName == null || role == null) {
            HttpUtil.json(ex, 400, Map.of(ERROR, "email, password, firstName, lastName, role are required"));
            return;
        }

        // Normalize role to match DB CHECK constraint
        role = role.toUpperCase();

        // Role validation
        if (!role.equals("STUDENT") && !role.equals("TEACHER") && !role.equals("ADMIN")) {
            HttpUtil.json(ex, 400, Map.of(ERROR, "role must be STUDENT or TEACHER"));
            return;
        }

        if ("ADMIN".equals(role)) {
            HttpUtil.json(ex, 403, Map.of(ERROR, "Admin cannot self-register"));
            return;
        }

        // Enforce your DB chk_student_code constraint
        if ("STUDENT".equals(role)) {
            if (studentCode == null) {
                HttpUtil.json(ex, 400, Map.of(ERROR, "Student code required"));
                return;
            }
        } else {
            // must be NULL for non-students (not empty string)
            studentCode = null;
        }

        if (users.existsByEmail(email)) {
            HttpUtil.json(ex, 400, Map.of(ERROR, "Email already exists"));
            return;
        }

        String hash = encoder.encode(password);

        // This MUST insert into: users(email, password_hash, first_name, last_name, user_type, student_code)
        users.insert(email, hash, firstName, lastName, role, studentCode);

        HttpUtil.json(ex, 201, Map.of("status", "created"));
    }

    private static String asTrimmed(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }
}