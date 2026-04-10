package http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import repository.UserRepository;
import security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.util.Map;

public class LoginHandler implements HttpHandler {

    private final UserRepository users;
    private final JwtService jwtService;
    private final ObjectMapper om = new ObjectMapper();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static  final String ERROR = "error";

    public LoginHandler(UserRepository users, JwtService jwtService) {
        this.users = users;
        this.jwtService = jwtService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.send(ex, 405, "Method Not Allowed");
            return;
        }

        Map<String, String> body;
        try {
            body = om.readValue(ex.getRequestBody(), new TypeReference<>() {});
        } catch (Exception parseErr) {
            HttpUtil.json(ex, 400, Map.of(ERROR, "Invalid JSON"));
            return;
        }

        String email = body.get("email");
        String password = body.get("password");

        // Normalize inputs to avoid mismatch due to whitespace/case
        if (email != null) email = email.trim().toLowerCase();
        if (password != null) password = password.trim();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            HttpUtil.json(ex, 400, Map.of(ERROR, "Email and password are required"));
            return;
        }

        User user = users.findByEmail(email).orElse(null);

        if (user == null || !encoder.matches(password, user.getPasswordHash())) {
            HttpUtil.json(ex, 401, Map.of(ERROR, "Invalid credentials"));
            return;
        }

        String role = user.getUserType().name();
        String token = jwtService.issueToken(user.getId(), user.getEmail(), role);
        String name = (user.getFirstName() + " " + user.getLastName()).trim();

        HttpUtil.json(ex, 200, Map.of(
                "token", token,
                "userId", user.getId(),
                "email", user.getEmail(),
                "role", role,
                "name", name
        ));
    }
}