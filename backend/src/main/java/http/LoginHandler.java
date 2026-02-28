package http;

import backend.security.JwtService;
import backend.user.User;
import backend.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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

        Map<String, String> body = om.readValue(ex.getRequestBody(), Map.class);
        String email = body.get("email");
        String password = body.get("password");

        User user = users.findByEmail(email).orElse(null);
        if (user == null || !encoder.matches(password, user.passwordHash())) {
            HttpUtil.json(ex, 401, Map.of("error", "Invalid credentials"));
            return;
        }

        String token = jwtService.issueToken(user.id(), user.email(), user.userType());

        HttpUtil.json(ex, 200, Map.of(
                "token", token,
                "userId", user.id(),
                "email", user.email(),
                "role", user.userType()
        ));
    }
}