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
            HttpUtil.json(ex, 400, Map.of("error", "Invalid JSON"));
            return;
        }

        String email = body.get("email");
        String password = body.get("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            HttpUtil.json(ex, 400, Map.of("error", "Email and password are required"));
            return;
        }

        User user = users.findByEmail(email).orElse(null);


        if (user == null || !encoder.matches(password, user.getPasswordHash())) {
            HttpUtil.json(ex, 401, Map.of("error", "Invalid credentials"));
            return;
        }

        String role = user.getUserType().name();
        String token = jwtService.issueToken(user.getId(), user.getEmail(), role);

        HttpUtil.json(ex, 200, Map.of(
                "token", token,
                "userId", user.getId(),
                "email", user.getEmail(),
                "role", role
        ));
    }
}